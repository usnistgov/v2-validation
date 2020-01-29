package hl7.v2.validation.vs
import hl7.v2.instance._
import hl7.v2.validation.report.ConfigurableDetections
import scala.concurrent.Future
import gov.nist.validation.report.Entry
import scala.concurrent.ExecutionContext.Implicits.global
import hl7.v2.instance._
import scala.util.{Failure, Try, Success}
import hl7.v2.profile.BindingStrength
import scala.collection.immutable.Map
import hl7.v2.validation.vs.VSValidationCode.CodeNotFound
import gov.nist.validation.report.impl.EntryImpl
import hl7.v2.validation.vs.VSValidationCode.RVS

trait VSValidationCode {
  
}
object VSValidationCode {
  case class RVS(code : Code, vs : String) extends VSValidationCode {}
  case class PVS(code : Code, vs : String) extends VSValidationCode {}
  case class EVS(code : Code, vs : String) extends VSValidationCode {}
  case class MultipleCodesFound(value : String, vs : String) extends VSValidationCode {}
  case class CodeNotFound(code : String, bindings : List[String]) extends VSValidationCode {}
  case class InvalidCodeSystem(code : String, expected : String, found : String, vs : String) extends VSValidationCode {}
  def codeToInt(code : VSValidationCode) : Int = {
    code match {
      case RVS(_,_) => 1
      case PVS(_,_) => 2
      case MultipleCodesFound(_,_) => 3
      case EVS(_,_) => 4
      case _ => 5
    }
  }
}

trait DefaultValueSetValidator extends VsValidator with EscapeSeqHandler {
  
    type CodeValidation = (VSValidationCode, Option[VSValidationCode])
    
    def checkValueSets(m: Message)(implicit Detections : ConfigurableDetections): Future[Seq[Entry]] = Future {
      implicit val separators = m.separators
      implicit val dtz = m.defaultTimeZone
      implicit val model = m.model
      check(m.asGroup);
    }
    
    // Checks if specification is available on element and checks it, then dives into complex elements
    def check(e : Element)(implicit Detections : ConfigurableDetections) : List[Entry] = {

      val entries = vsSpecification.vsSpecificationFor(e) match {
        case Some(ls) =>   ls.foldRight(List[Entry]()){
          (x, acc) => check(e, x)
        }
        case None => Nil
      }
      
      
      e match {
        case c : Complex => c.children.foldLeft(entries) { (acc, cc) => acc ::: check(cc) }
        case _ => entries
      }
    }
   
    // Resolve complex binding target as complex elements, fails on simple elements, and check complex binding 
    def check(e : Element, spec : ValueSetBinding)(implicit Detections : ConfigurableDetections) : List[Entry] = {
      Query.query(e, spec.target) match {
        case Success(x) => {
          val (entries, payloads) = resolveValidationPayload(x, spec)
          val y = payloads.map( p => validate(p))
          entries ::: y.flatten 
        }
        case _ => Nil
      }
    }
    
    def resolveValidationPayload(targets : List[Element], spec : ValueSetBinding)(implicit Detections : ConfigurableDetections) : (List[Entry], List[ValueSetValidationPayload]) = {
      val (valuesets, entries) = resolveBindings(targets, spec.bindings).values.toList.unzip;
      
      val payloads = for(target <- targets) yield {
        // Resolver Code Holders
        val resolveCodesResult = for(location <- spec.bindingLocations) yield {
          codeHolder(target, location)
        }
        (resolveCodesResult.unzip._2.flatten, ValueSetValidationPayload(target, resolveCodesResult.unzip._1.flatten, valuesets.flatten, spec.uniqCodeSys, spec.strength))
      }
      
      val (entryList, payload) = payloads.unzip
      (entries.flatten.flatten ::: entryList.flatten, payload)
    }
    
    def codeHolder(context : Element, location : BindingLocation)(implicit Detections : ConfigurableDetections) : (Option[CodeHolder], Option[Entry]) = {
       Query.queryAsSimple(context, location.codeLocation) match {
            case Success(x) => if (x.size > 1)  (None, Some(bindingLocationReturnedMoreThanOneElement(context, location.codeLocation))) else 
                               if (x.size == 0) (None, None) else
                               location.codeSystemLocation match {
                                 case Some(codeSysLoc) => Query.queryAsSimple(context, codeSysLoc) match {
                                   case Success(y) => if (y.size > 1)  (Some(CodeHolder(x.head, None, location)), Some(bindingLocationReturnedMoreThanOneElement(context, codeSysLoc))) else 
                                                      if (x.size == 0) (Some(CodeHolder(x.head, None, location)), None) else
                                                      (Some(CodeHolder(x.head, Some(y.head), location)), None)
                                   case Failure(f) => (Some(CodeHolder(x.head, None, location)),  Some(specErrorUnresolvableBindingLocation(context, codeSysLoc)))
                                 }
                                 case None => (Some(CodeHolder(x.head, None, location)), None)
                               }
            case Failure(f) => (None,  Some(specErrorUnresolvableBindingLocation(context, location.codeLocation)))
          }
    }
    
    def resolveBindings(targets : List[Element], bindings : List[String])(implicit Detections : ConfigurableDetections) : Map[String, (Option[ValueSet], Option[List[Entry]])] = {
      (for(id <- bindings) yield {
        try {
          if(valueSetLibrary.isExcludedFromTheValidation(id)){
            val entries = targets.map { x => exludedFromValidation(x, id) }
            (id -> (None, Some(entries)))
          }
          else {
            val vs = valueSetLibrary.get(id)
            (id -> (Some(vs), None))
          }
        }
        catch{
          case vs : ValueSetNotFoundException =>{
            val entries = targets.map { x => vsNotFound(x, id) }
            (id -> (None, Some(entries)))
          }
        }
      }) toMap
    }
    
    def bindingMetadata(strength : BindingStrength, vsList : List[ValueSet])(implicit Detections : ConfigurableDetections) : BindingMetadata = {
      val bs = Some(strength)
      val stability = if (vsList.map(_.stability).contains(Some(Stability.Dynamic))) Some(Stability.Dynamic) else Some(Stability.Static)
      val extensibility = if (vsList.map(_.extensibility).contains(Some(Extensibility.Open))) Some(Extensibility.Open) else Some(Extensibility.Closed)
      BindingMetadata(bs, stability, extensibility)
    }
    
    def validate(validationPayload : ValueSetValidationPayload)(implicit Detections : ConfigurableDetections) : List[Entry] = {
      
      // Check All Collected Codes Against All Bindings
      val validationResult = for(code <- validationPayload.values) yield {
        val validation = for(vs <- validationPayload.valueSets) yield {
          (vs -> checkCode(code, vs))
        }
        (code -> validation.toMap)
      }
      
      
      
      // Refine result by keeping best match ValueSet result
      val refinedValidationResult = for(tuple <- validationResult) yield {
         val reqMatch = findMatch(tuple)
         reqMatch match {
           case Some(m) => (tuple._1, Some(m._1), BindingMetadata(Some(validationPayload.strength), m._1.stability, m._1.extensibility) , m._2._1, m._2._2) 
           case None => (tuple._1, None, bindingMetadata(validationPayload.strength, tuple._2.map(_._1).toList), VSValidationCode.CodeNotFound(tuple._1.code.value.raw, tuple._2.keys.toList.map(_.id)), None) 
         }
      }
      
      // Create Detections
      val entries = refinedValidationResult map { tuple =>
        
        val ext = if(tuple._2.isDefined) tuple._2.get.extensibility else None
        val stab = if(tuple._2.isDefined) tuple._2.get.stability else None
        
        tuple match {
          case (CodeHolder(code, codeSys, loc), vs, bindingMd, vsc1, vsc2) => Some(createEntry(code, vsc1, bindingMd)) :: {
            (codeSys, vsc2) match {
              case (Some(codeSysElm), Some(vscSys)) => Some(createEntry(codeSysElm, vscSys, bindingMd)) :: Nil
              case _ => None :: Nil
            }
          }
        }
      }
      
      
      // Check additional constraint
      val additionalEntry = if(validationPayload.xor) {
        val found = for(tuple <- validationResult) yield {
          getFound(tuple)
        }
        
        val xorEntries = for(x <- found.flatten.groupBy(_._1).mapValues(_.map(_._2)); if x._2.size > 1 ) yield {
          duplicateTriplet(x._2.map(_.code), x._1)
        }
        
        xorEntries.toList.flatten
      }
      else {
        Nil
      }
       
      additionalEntry ::: entries.flatten.flatten
    }
    
    def getFound(unit : (CodeHolder, Map[ValueSet, CodeValidation])) : Map[String, CodeHolder] =  {
      for(tuple <- unit._2; if VSValidationCode.codeToInt(tuple._2._1) <= 4) yield {
        (tuple._1.id -> unit._1)
      }
    } 

    def findMatch( unit : (CodeHolder, Map[ValueSet, CodeValidation]))(implicit Detections : ConfigurableDetections) : Option[(ValueSet, CodeValidation)] = {
      
      // Main Validation Code => Payload
      val reMap = for(candidate <- unit._2) yield {
        (candidate._2._1, candidate)
      }
      
      // Sort By Validation Code
      val matchSet = reMap.toList.sortWith((a, b) => VSValidationCode.codeToInt(a._1) < VSValidationCode.codeToInt(b._1))
      
      matchSet.head._1 match {
        case CodeNotFound(_,_) => None
        case _ => Some(matchSet.head._2)
      }
      
    }
        
    def checkCode(code : CodeHolder, vs : ValueSet)(implicit Detections : ConfigurableDetections) : CodeValidation = {
      code match {
        case CodeHolder(code, None, loc) => (checkValue(code.value.raw, vs)._2, None)
        case CodeHolder(code, Some(codeSys), loc) => checkTriplet(code.value.raw, codeSys.value.raw, vs)
      }
    }
    
    def checkValue(code : String, vs : ValueSet)(implicit Detections : ConfigurableDetections) : (Option[Code], VSValidationCode) = {
      val codes = vs.getCodes(code)
      if (codes.size() == 0) (None, VSValidationCode.CodeNotFound(code, vs.id::Nil))
      else if (codes.size() > 1) (None, VSValidationCode.MultipleCodesFound(code, vs.id))
      else codes.get(0).usage match {
        case CodeUsage.P => (Some(codes.get(0)), VSValidationCode.PVS(codes.get(0), vs.id))
        case CodeUsage.E => (Some(codes.get(0)), VSValidationCode.EVS(codes.get(0), vs.id))
        case CodeUsage.R => (Some(codes.get(0)), VSValidationCode.RVS(codes.get(0), vs.id)) 
      }
    }
    
    def checkTriplet(vcode : String, codeSys : String, vs : ValueSet)(implicit Detections : ConfigurableDetections) : CodeValidation = {
      checkValue(vcode, vs) match {
        case (Some(code), vsc) => if(code.codeSys == codeSys) (vsc, None) else (vsc, Some(VSValidationCode.InvalidCodeSystem(vcode, code.codeSys, codeSys, vs.id)))
        case (None, vsc) => (vsc, None)
      }
    }
    
    def exludedFromValidation(target : Element, vsId : String)(implicit Detections : ConfigurableDetections) : Entry = Detections.vsNoVal(target.location, vsId)
    
    def vsNotFound(target : Element, vsId : String)(implicit Detections : ConfigurableDetections) : Entry = Detections.vsNotFound(target.location, vsId, null)

    // MOVE TO TEMPLATE
    def specErrorUnresolvableBindingLocation(target : Element, path : String)(implicit Detections : ConfigurableDetections) : Entry = Detections.bindingLocation(target.location, s"Unresolvable path ${path} from target element ${target.location.prettyString}")
    
    def bindingLocationReturnedMoreThanOneElement(target : Element, path : String)(implicit Detections : ConfigurableDetections) : Entry = Detections.bindingLocation(target.location, s"Resolving path ${path} from target element ${target.location.prettyString} returned more than one element")
    
    def createEntry(element : Element, issue : VSValidationCode, metadata : BindingMetadata)(implicit Detections : ConfigurableDetections) : Entry = {
      issue match {
        case VSValidationCode.RVS(code, vs) => Detections.rvs(element.location, code.value, vs)
        case VSValidationCode.PVS(code, vs) => Detections.pvs(element.location, code.value, vs)
        case VSValidationCode.EVS(code, vs) => Detections.evs(element.location, code.value, vs, metadata.stability.getOrElse(null), metadata.extensibility.getOrElse(null), metadata.strength.getOrElse(null))
        case VSValidationCode.MultipleCodesFound(code, vs) => null
        case VSValidationCode.CodeNotFound(code, vs) => Detections.codeNotFound(element.location, code, vs.toString(), metadata.stability.getOrElse(null), metadata.extensibility.getOrElse(null), metadata.strength.getOrElse(null))
        case VSValidationCode.InvalidCodeSystem(code, expected, found, vs) => Detections.codeNotFoundInvalidCodeSystem(element.location, code, vs.toString(), metadata.stability.getOrElse(null), metadata.extensibility.getOrElse(null), metadata.strength.getOrElse(null))
      }
    }
    
    def duplicateTriplet(element : List[Element], vs : String) : List[Entry] = Nil
}