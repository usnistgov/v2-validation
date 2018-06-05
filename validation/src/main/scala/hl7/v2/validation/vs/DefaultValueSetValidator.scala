package hl7.v2.validation.vs
import hl7.v2.instance._
import hl7.v2.validation.report.ConfigurableDetections
import scala.concurrent.Future
import gov.nist.validation.report.Entry
import scala.concurrent.ExecutionContext.Implicits.global
import hl7.v2.instance._
import scala.util.{Failure, Try, Success}
import hl7.v2.profile.BindingStrength


trait DefaultValueSetValidator extends ValueSetValidator with EscapeSeqHandler {
  
    def checkValueSets(m: Message)(implicit Detections : ConfigurableDetections): Future[Seq[Entry]] = Future {
      implicit val separators = m.separators
      implicit val dtz = m.defaultTimeZone
      implicit val model = m.model
      check(m.asGroup);
    }
    
    def check(e : Element) : List[Entry] = {
      val specification = vsSpecification.vsSpecificationFor(e);
      
      val entries = specification.foldRight(List[Entry]()){
        (x, acc) => check(e, x)
      }
      
      e match {
        case c : Complex => c.children.foldLeft(entries) { (acc, cc) => acc ::: check(cc) }
        case _ => entries
      }
    }
    
    def check(e : Element, spec : ValueSetBinding) : List[Entry] = {
      spec match {
        case s : SimpleValueSetBinding => Nil
        case c : ComplexValueSetBinding => Nil
      }
    }
    
    def check(e : Element, spec : SimpleValueSetBinding) : List[Entry] = {
      Query.queryAsSimple(e, spec.target) match {
        case Success(x) => checkSimple(x,  spec)
        case _ => specErrorSimpleBindingOnComplexElement() :: Nil
      }
    }
    
    def check(e : Element, spec : ComplexValueSetBinding) : List[Entry] = {
      Query.query(e, spec.target) match {
        case Success(x) => val (other, complex) = x.foldRight((List[Element](), List[Complex]())){
          (elm, acc) => elm match {
            case c : Complex => (acc._1, c :: acc._2)
            case o => (o :: acc._1, acc._2)
          }
        }       
        specErrorComplexBindingOnSimpleElement(other) ::: checkComplex(complex.asInstanceOf[List[Complex]], spec)
        case _ => Nil
      }
    }
    
    def checkSimple(elms : List[Simple],  spec : SimpleValueSetBinding) : List[Entry] = {
      elms.foldRight(List[Entry]()){
        (elm, acc) => checkBindings(elm, spec.bindings, spec.strength) ::: acc
      }
    }
    
    def checkComplex(elms : List[Complex],  spec : ComplexValueSetBinding) : List[Entry] = {
      elms.foldRight(List[Entry]()){
        (elm, acc1) => spec.locations.foldRight(List[Entry]()){
          (location, acc2) => Query.queryAsSimple(elm, location.asString) match {
            case Success(x) => checkSimple(x, SimpleValueSetBinding(spec.target, spec.strength, spec.bindings)) ::: acc2
            case _ => Nil
          }
        } ::: acc1
      }
    }
    
    
    def checkBindings(elm : Simple, bindings : List[String], strength : BindingStrength) : List[Entry] = {
      val entries = for(id <- bindings) yield {
        val vs = valueSetLibrary.get(id)
        if(id != null){
          if(!vs.contains(elm.value.raw)){
            codeNotFound(elm.value.raw)
          }
          else {
            null
          }
        }
        else {
          vsNotFound(id)
        }
      }
      if (entries.contains(null)) return Nil else return entries
    }
    
    
    
    
    def specErrorSimpleBindingOnComplexElement() : Entry;
    def specErrorComplexBindingOnSimpleElement(x : List[Element]) : List[Entry];
    def vsNotFound(s : String) : Entry;
    def codeNotFound(s : String) : Entry;
}