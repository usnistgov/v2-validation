package hl7.v2.validation.vs

import hl7.v2.validation.report.ConfigurableDetections

import scala.concurrent.Future
import gov.nist.validation.report.Entry

import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.concurrent.ExecutionContext.Implicits.global
import hl7.v2.instance._
import hl7.v2.profile

import scala.util.{Failure, Success}
import hl7.v2.profile.{BindingStrength, Usage}
import hl7.v2.validation.FeatureFlags

sealed trait VSValidationCode {
  val isSpecError: Boolean = false
  val codeIsFound: Boolean = false
}

sealed trait VSValidationCodeSpecError extends VSValidationCode {
  override val isSpecError = true
}

sealed trait VSValidationCodeFound extends VSValidationCode {
  override val codeIsFound = true
  def getValueSets: List[ValueSet]
}

sealed trait VSValidationCodeFoundInMultiVS extends VSValidationCodeFound {
  override val codeIsFound = true
  val vsList: List[ValueSet]
  def getValueSets: List[ValueSet] = vsList
}

sealed trait VSValidationCodeFoundInSingleVS extends VSValidationCodeFound {
  override val codeIsFound = true
  val vs: ValueSet
  def getValueSets: List[ValueSet] = List(vs)
}

trait Resolve[T] {
  def isValue: Boolean
  def get: T
  def detection: VSValidationCode
}

case class Payload[T](A: T) extends Resolve[T] {
  def isValue = true
  def get: T = A
  def detection: VSValidationCode = null
}

case class PayloadAndDetection[T](A: T, code: VSValidationCode) extends Resolve[T] {
  def isValue = true
  def get: T = A
  def detection: VSValidationCode = code
}

case class Detection[T](code: VSValidationCode) extends Resolve[T] {
  def isValue = false
  def get = throw new NoSuchElementException("Detection.get")
  def detection: VSValidationCode = code
}

case class Empty[T]() extends Resolve[T] {
  def isValue = false
  def get = throw new NoSuchElementException("Empty.get")
  def detection = throw new NoSuchElementException("Empty.get")
}

case class VsEntry(target: Element, code: VSValidationCode, strength: Option[BindingStrength])

object VSValidationCode {

  case class VSNotFound(vs: String) extends VSValidationCode
  case class EmptyVS(vs: ValueSet) extends VSValidationCode
  case class ExcludedVS(vs: String) extends VSValidationCode
  case class UBS(binding: ValueSetBinding) extends VSValidationCode
  case class MultipleVSForPrimitive(binding: ValueSetBinding) extends VSValidationCodeSpecError
  case class InvalidCodeBindingLocation(location: String, multiple: Boolean) extends VSValidationCodeSpecError
  case class InvalidCodeSystemBindingLocation(location: String, multiple: Boolean) extends VSValidationCodeSpecError
  case class MultipleCodesFoundInValueSet(value: String, vs: ValueSet, codes: List[Code]) extends VSValidationCodeSpecError with VSValidationCodeFoundInSingleVS
  case class MultipleCodeAndCodeSystemFound(value: String, cs: String, vs: ValueSet, codes: List[Code]) extends VSValidationCodeSpecError with VSValidationCodeFoundInSingleVS
  case class FoundInMultipleValueSets(code: String, codeSystem: Option[String], vsList: List[ValueSet]) extends VSValidationCodeSpecError with VSValidationCodeFoundInMultiVS
  case class RVS(code: Code, found: String, vs: ValueSet) extends VSValidationCodeFoundInSingleVS
  case class PVS(code: Code, found: String, vs: ValueSet) extends VSValidationCodeFoundInSingleVS
  case class EVS(code: Code, found: String, vs: ValueSet) extends VSValidationCodeFoundInSingleVS
  case class NoUsage(code: Code, found: String, vs: ValueSet) extends VSValidationCodeFoundInSingleVS
  case class CodedElementXOR(element: List[CodeHolder], vs: ValueSet) extends VSValidationCodeFoundInSingleVS
  case class SimpleCodeNotFound(code: String, valueSet: String) extends VSValidationCode
  case class CodeAndCodeSystemNotFound(code: String, codeSystem: String, valueSet: String) extends VSValidationCode
  case class SimpleCodeFoundInvalidCodeSystem(code: String, expected: String, found: String, vs: ValueSet) extends VSValidationCode
  case class SimpleCodeFoundCodeSystemNotPopulated(code: String, cs: String, location: String, vs: ValueSet) extends VSValidationCode
  case class CodeNotFound(code: String, codeSystem: Option[String], bindings: List[ValueSet]) extends VSValidationCode
  case class UsageAndExtensibilityNotCompatible(code: Code, vs: ValueSet) extends VSValidationCodeFoundInSingleVS with VSValidationCodeSpecError
  //TODO
  case class DynamicValueSet(vs: ValueSet) extends VSValidationCodeFoundInSingleVS

  // Single Code Detections
  case class SingleCodeSuccess(context: Element, code: Simple, sg: SingleCodeBinding) extends VSValidationCode
  case class SingleCodeNotFound(e: Simple, code: String, sg: SingleCodeBinding) extends VSValidationCode
  case class SingleCodeCodeSystemNotFound(context: Element, code: Simple, location: String, sg: SingleCodeBinding) extends VSValidationCode
  case class SingleCodeInvalidCodeSystem(context: Element, code: Simple, codeSystem: Simple, sg: SingleCodeBinding) extends VSValidationCode
  case class SingleCodeMultiLocationXOR(context: Element, e: List[Element], sg: SingleCodeBinding) extends VSValidationCode
}

trait DefaultValueSetValidator extends VsValidator with EscapeSeqHandler {


  def checkValueSets(m: Message, additionalVsBindings: Option[ValueSetSpecification])
                    (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): Future[Seq[Entry]] = Future {
    implicit val separators: Separators = m.separators
    implicit val dtz: Option[TimeZone] = m.defaultTimeZone
    implicit val model: profile.Message = m.model
    checkMsgElement(m.asGroup, additionalVsBindings)
  }

  // Only check bindings when element should not be skipped
  def checkMsgElement(e: Element, additionalVsBindings: Option[ValueSetSpecification])
                     (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[Entry] = {
    skipElementCheck(e) match {
      case true => Nil
      case false => check(e, additionalVsBindings)
    }
  }

  // skip element validation when Usage is X or W
  def skipElementCheck(element: Element): Boolean = {
    element.req.usage match {
      case Usage.X | Usage.W => true
      case _ => false
    }
  }

  // Checks if specification is available on element and checks it, then dives into complex elements
  def check(e: Element, additionalVsBindings: Option[ValueSetSpecification])
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[Entry] = {
    val vsEntries = getValueSetBindingsForElement(e, additionalVsBindings) match {
      case Nil => Nil
      case ls => ls.foldRight(List[Entry]()) {
        (x, acc) => acc ::: check(e, x)
      }
    }

    val singleCodeEntries = getSingleCodeBindingsForElement(e, additionalVsBindings) match {
      case Nil => Nil
      case ls => ls.foldRight(List[Entry]()) {
        (x, acc) => acc ::: check(e, x)
      }
    }

    val entries = vsEntries ::: singleCodeEntries

    e match {
      case c: Complex => c.children.foldLeft(entries) { (acc, cc) => acc ::: checkMsgElement(cc, additionalVsBindings) }
      case _ => vsEntries
    }
  }

  def getValueSetBindingsForElement(e: Element, additionalVsBindings: Option[ValueSetSpecification] = None): List[ValueSetBinding] = {
    val fromVsBindingsSpecification = vsSpecification.vsSpecificationFor(e)
    val fromAdditionalVsBindings = additionalVsBindings.flatMap(_.vsSpecificationFor(e))
    fromVsBindingsSpecification.getOrElse(Nil) ::: fromAdditionalVsBindings.getOrElse(Nil)
  }

  def getSingleCodeBindingsForElement(e: Element, additionalVsBindings: Option[ValueSetSpecification] = None): List[SingleCodeBinding] = {
    val fromVsBindingsSpecification = vsSpecification.singleCodeSpecificationFor(e)
    val fromAdditionalVsBindings = additionalVsBindings.flatMap(_.singleCodeSpecificationFor(e))
    fromVsBindingsSpecification.getOrElse(Nil) ::: fromAdditionalVsBindings.getOrElse(Nil)
  }

  def resolveTargetAndSkipAndDo(e: Element, target: String, fn: (List[Element]) => List[Entry]): List[Entry] = {
    Query.query(e, target) match {
      case Success(targets) =>
        targets.filterNot(skipElementCheck) match {
          case Nil => Nil
          case checkable => fn(checkable)
        }
      case _ => Nil
    }
  }

  /* Resolve binding target from context.
     Validates binding if target is not skipped (skip on target Usage X or W)
   */
  def check(e: Element, spec: ValueSetBinding)
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[Entry] = {
    resolveTargetAndSkipAndDo(
      e,
      spec.target,
      checkable => {
        check(checkable, spec).map(entry => createEntry(entry.target, entry.code, entry.strength))
      }
    )
  }

  /* Resolve binding target from context.
   Validates binding if target is not skipped (skip on target Usage X or W)
 */
  def check(e: Element, spec: SingleCodeBinding)
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[Entry] = {
    resolveTargetAndSkipAndDo(
      e,
      spec.target,
      checkable => {
        check(checkable, spec).map(entry => createEntry(entry.target, entry.code, entry.strength))
      }
    )
  }

  def check(elements: List[Element], spec: SingleCodeBinding)
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[VsEntry] = {
    val detections = for (target <- elements) yield {
      val rCodeHolders = for (location <- spec.bindingLocations) yield {
        codeHolder(target, location)
      }
      val codeHolders = collectValues(rCodeHolders)
      val chIssues = collectIssues(rCodeHolders)

      (validate(target, codeHolders, spec) ::: chIssues).map(VsEntry(target, _, None))
    }

    detections.flatten
  }


  def check(elements: List[Element], spec: ValueSetBinding)
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[VsEntry] = {
    val UBS = spec.strength.contains(BindingStrength.U)
    val MultipleVSForPrimitive = spec.bindings.size > 1 && elements.exists(_.isInstanceOf[Simple])

    // If BindingStrength is not Undetermined and no primitive element has multiple value sets
    if(!UBS && !MultipleVSForPrimitive) {
      val valueSetValidationPayloads: List[ValueSetValidationPayload] = resolveValidationPayload(elements, spec)
      valueSetValidationPayloads.flatMap(p => {
        (validate(p) ::: p.detections).map(issue => VsEntry(p.target, issue, spec.strength))
      })
    } else {
      val ubsEntries = if(UBS) elements.map(elm => VsEntry(elm, VSValidationCode.UBS(spec), spec.strength)) else Nil
      val multiVsEntries = if(MultipleVSForPrimitive) elements.map(elm => VsEntry(elm, VSValidationCode.MultipleVSForPrimitive(spec), spec.strength)) else Nil

      ubsEntries:::multiVsEntries
    }
  }

  def collectValues[T](resolve: List[Resolve[T]]): List[T] = {
    resolve.collect {
      case Payload(value) => value
      case PayloadAndDetection(value, _) => value
    }
  }

  def collectIssues[T](resolve: List[Resolve[T]]): List[VSValidationCode] = {
    resolve.collect {
      case Detection(issue) => issue
      case PayloadAndDetection(_, issue) => issue
    }
  }

  def resolveValidationPayload(targets: List[Element], spec: ValueSetBinding)
                              (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[ValueSetValidationPayload] = {
    val rValueSets = resolveBindings(spec.bindings)
    val vsIssues = collectIssues(rValueSets)
    val valueSets = collectValues(rValueSets)

    for (target <- targets) yield {
      // Resolver Code Holders
      val rCodeHolders = for (location <- spec.bindingLocations) yield {
        codeHolder(target, location)
      }
      val codeHolders = collectValues(rCodeHolders)
      val chIssues = collectIssues(rCodeHolders)

      ValueSetValidationPayload(target, codeHolders, valueSets, spec.strength, vsIssues:::chIssues)
    }
  }

  def codeHolder(context: Element, location: BindingLocation)
                (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): Resolve[CodeHolder] = {
    getBindingLocationValue(context, location.codeLocation, VSValidationCode.InvalidCodeBindingLocation) match {
      case Empty() => Empty[CodeHolder]()
      case Detection(issue: VSValidationCode) => Detection[CodeHolder](issue)
      case Payload(code: Simple) => location.codeSystemLocation match {
        case None => Payload[CodeHolder](CodeHolder(code, None, location))
        case Some(codeSysLocation) => getBindingLocationValue(context, codeSysLocation, VSValidationCode.InvalidCodeSystemBindingLocation) match {
          /// TODO Handle case where code system is not populated
          case Empty() => Payload[CodeHolder](CodeHolder(code, None, location))
          case Detection(issue: VSValidationCode) => Detection[CodeHolder](issue)
          case Payload(codeSystem: Simple) => Payload[CodeHolder](CodeHolder(code, Some(codeSystem), location))
        }
      }
    }
  }

  def getBindingLocationValue(context: Element, location: String, detectionFactory: (String, Boolean) => VSValidationCodeSpecError): Resolve[Simple] = {
    Query.queryAsSimple(context, location) match {
      case Success(x) =>
        if (x.size > 1) Detection[Simple](detectionFactory(location, true))
        else if (x.isEmpty) Empty[Simple]()
        else Payload[Simple](x.head)
      case Failure(f) => Detection[Simple](detectionFactory(location, false))
    }
  }

  def resolveBindings(bindings: List[String])
                     (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[Resolve[ValueSet]] = {
    for (id <- bindings) yield {
      try {
        if (valueSetLibrary.isExcludedFromTheValidation(id)) {
          Detection[ValueSet](VSValidationCode.ExcludedVS(id))
        }
        else {
          val vs = valueSetLibrary.get(id)
          if(vs.isEmpty) Detection[ValueSet](VSValidationCode.EmptyVS(vs))
          else Payload(vs)
        }
      }
      catch {
        case vs: ValueSetNotFoundException => {
          Detection[ValueSet](VSValidationCode.VSNotFound(id))
        }
      }
    }
  }

  def validate(target: Element, values: List[CodeHolder], spec: SingleCodeBinding)
              (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[VSValidationCode] = {
    val detections = values.map(codeHolder => {
      codeHolder.code.value.raw match {
        // When code matches
        case spec.code => codeHolder.codeSystem match {
          // When code system exists
          case Some(cs) => cs.value.raw match {
            // When code system matches
            case spec.codeSystem => VSValidationCode.SingleCodeSuccess(target, codeHolder.code, spec)
            // When code system doesn't match
            case v => VSValidationCode.SingleCodeInvalidCodeSystem(target, codeHolder.code, cs, spec)
          }
          // When code system doesn't exist
          case None => codeHolder.location.codeSystemLocation match {
            // When code system should exist
            case Some(l) => VSValidationCode.SingleCodeCodeSystemNotFound(target, codeHolder.code, l, spec)
            // When code system location not specified
            case None => VSValidationCode.SingleCodeSuccess(target, codeHolder.code, spec)
          }
        }
        // When code doesn't match
        case v => VSValidationCode.SingleCodeNotFound(codeHolder.code, v, spec)
      }
    })

    // collect success, notfound, code system errors and inconclusives
    val (success, notfound, cs, notfoundcs) = (
      detections.collect{ case c: VSValidationCode.SingleCodeSuccess => c},
      detections.collect{ case c: VSValidationCode.SingleCodeNotFound => c},
      detections.collect{ case c: VSValidationCode.SingleCodeInvalidCodeSystem => c},
      detections.collect{ case c: VSValidationCode.SingleCodeCodeSystemNotFound => c},
    )

    // If found in any location return success
    if(success.size == 1) success
    // If found in multiple locations return XOR
    else if(success.size > 1) List(VSValidationCode.SingleCodeMultiLocationXOR(target, success.map(_.code), spec))
    // If not found in any location return all detections
    else notfound ::: cs ::: notfoundcs
  }

  def validate(validationPayload: ValueSetValidationPayload)
              (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[VSValidationCode] = {

    // Check All Collected Codes Against All Bindings
    val vCodeBucket: List[(CodeHolder, ValueSet, VSValidationCode)] = validationPayload.values.flatMap(code => {
      for (vs <- validationPayload.valueSets) yield {
        (code, vs, checkCode(code, vs))
      }
    })

    val vsCodes: Iterable[(CodeHolder, List[VSValidationCode], List[ValueSet])] = for(code <- vCodeBucket.groupBy(_._1)) yield {

      val (codeFoundDetections, codeNotFoundDetections) = code._2.partition(tuple => { tuple._3.codeIsFound })

      val duplicateCodeFound: Option[VSValidationCode] = multipleCodesFound(code._1, codeFoundDetections)
      val codesFound: List[VSValidationCode] = codeFoundDetections.map(_._3)
      val issues = codeNotFoundDetections.partition(_._3.isSpecError)
      val specIssues: List[VSValidationCode] = issues._1.map(_._3)
      val invalidCodeSystem: List[VSValidationCode] = if(codesFound.nonEmpty) Nil else codeNotFoundDetections.collect({
        case (_, _, value: VSValidationCode.SimpleCodeFoundInvalidCodeSystem) => value
      })
      val csNotPopulated: List[VSValidationCode] = if(codesFound.nonEmpty) Nil else codeNotFoundDetections.collect({
        case (_, _, value: VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated) => value
      })
      val codeNotFound: Option[VSValidationCode] = if(codesFound.isEmpty && csNotPopulated.isEmpty) Some(VSValidationCode.CodeNotFound(code._1.code.value.raw, code._1.codeSystem.map(_.value.raw), code._2.map(_._2))) else None

      val detections = codesFound:::specIssues:::invalidCodeSystem:::csNotPopulated:::codeNotFound.map(_ :: Nil).getOrElse(Nil):::duplicateCodeFound.map(_ :: Nil).getOrElse(Nil)
      val matches = codesFound.map(_.asInstanceOf[VSValidationCodeFound]).flatMap(_.getValueSets)
      (code._1, detections.flatMap(expandVSValidationCode), matches)
    }

    detectAndClearCodeNotFoundIfMatch(vsCodes) ::: detectXOR(vsCodes.map(elm => (elm._1, elm._3)).toList)
  }

  def detectXOR(matches : List[(CodeHolder, List[ValueSet])]): List[VSValidationCode] = {
    val vsMatchMap: Map[ValueSet, List[CodeHolder]] = matches.flatMap(x => x._2.map(y => (x._1, y))).distinct.groupBy(_._2).map(tuple => (tuple._1 -> tuple._2.map(_._1)))
    val detections = for(vs: (ValueSet, List[CodeHolder]) <- vsMatchMap) yield {
      if(vs._2.size > 1) Some(VSValidationCode.CodedElementXOR(vs._2, vs._1))
      else None
    }

    detections.flatten.toList
  }

  def detectAndClearCodeNotFoundIfMatch(vsCodes : Iterable[(CodeHolder, List[VSValidationCode], List[ValueSet])]): List[VSValidationCode] = {
    val matchFound = vsCodes.flatMap(_._3).nonEmpty
    val detections = vsCodes.flatMap(_._2).toList
    if(matchFound) detections.filter(d => d.codeIsFound || d.isSpecError)
    else detections
  }



  def multipleCodesFound(code: CodeHolder, vsCodeFound: List[(CodeHolder, ValueSet, VSValidationCode)]): Option[VSValidationCode.FoundInMultipleValueSets] = {
    if(vsCodeFound.size > 1) Some(VSValidationCode.FoundInMultipleValueSets(code.code.value.raw, code.codeSystem.map(_.value.raw), vsCodeFound.map(_._2).distinct))
    else None
  }

  def checkCode(holder: CodeHolder, vs: ValueSet)
               (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): VSValidationCode = {
    holder match {
      case CodeHolder(code, None, _) => holder.location.codeSystemLocation match {
        case Some(csLocation) => checkValue(code.value.raw, vs) match {
          case Detection(detection) => detection
          case PayloadAndDetection(code, _) => VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated(code.value, code.codeSys, csLocation, vs)
        }
        case None => checkValue(code.value.raw, vs).detection
      }
      case CodeHolder(code, Some(codeSys), _) => checkTriplet(code.value.raw, codeSys.value.raw, vs)
    }
  }

  def checkValue(code: String, vs: ValueSet)
                (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): Resolve[Code] = {
    val codes = getCodes(code, vs)
    if (codes.isEmpty) Detection[Code](VSValidationCode.SimpleCodeNotFound(code, vs.id))
    else if (codes.length > 1) Detection[Code](VSValidationCode.MultipleCodesFoundInValueSet(code, vs, codes))
    else PayloadAndDetection[Code](codes.head, codeToVsValidation(codes.head, code, vs))
  }

  def getCodes(code: String, vs: ValueSet)
              (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): List[Code] = {
    if(featureFlags.legacy0396 && vs.id.matches(".*0396.*")) {
      vs.getCodes.filter(c => codeMatchLegacy0396(c, code))
    } else {
      vs.getCodes(code).asScala.toList
    }
  }

  def codeMatchLegacy0396(code: Code, value: String): Boolean = {
    code.pattern match {
      case Some(pattern) => value.matches(pattern)
      case None =>
        if(code.value.equals("HL7nnnn")) value.matches("HL7[0-9]{4}")
        else if(code.value.equals("99zzz")) value.matches("99[a-zA-Z0-9]{3}")
        else code.value.equals(value)
    }
  }

  def codeToVsValidation(code: Code, value: String, vs: ValueSet): VSValidationCode = {
    code.usage match {
      case CodeUsage.P => VSValidationCode.PVS(code, value, vs)
      case CodeUsage.E => VSValidationCode.EVS(code, value, vs)
      case CodeUsage.R => VSValidationCode.RVS(code, value, vs)
      case _ => VSValidationCode.NoUsage(code, value, vs)
    }
  }

  def expandVSValidationCode(vsCode: VSValidationCode): List[VSValidationCode] = {
    vsCode match {
      case detection : VSValidationCode.MultipleCodesFoundInValueSet => detection :: detection.codes.map(code => codeToVsValidation(code, detection.value, detection.vs))
      case detection : VSValidationCode.MultipleCodeAndCodeSystemFound => detection :: detection.codes.map(code => codeToVsValidation(code, detection.value, detection.vs))
      case detection : VSValidationCode.PVS =>
        val extensibility = detection.vs.extensibility match {
          case Some(Extensibility.Closed) => VSValidationCode.UsageAndExtensibilityNotCompatible(detection.code, detection.vs)::Nil
          case _ => Nil
        }

        val stability = detection.vs.stability match {
          case Some(Stability.Dynamic) => VSValidationCode.DynamicValueSet(detection.vs)::Nil
          case _ => Nil
        }
        detection::extensibility:::stability
      case detection : VSValidationCode.CodeNotFound => detection::detection.bindings.filter(_.stability.contains(Stability.Dynamic)).map(VSValidationCode.DynamicValueSet)
      case x => List(x)
    }
  }

  def checkTriplet(value: String, codeSys: String, vs: ValueSet)
                  (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): VSValidationCode = {
    checkValue(value, vs) match {
      case PayloadAndDetection(code, issue) => if (code.codeSys == codeSys) issue else VSValidationCode.SimpleCodeFoundInvalidCodeSystem(value, code.codeSys, codeSys, vs)
      case Detection(detection : VSValidationCode.MultipleCodesFoundInValueSet) => {
        val codeAndCodeSys = detection.codes.filter(code => code.codeSys.equals(codeSys))
        if(codeAndCodeSys.size > 1) VSValidationCode.MultipleCodeAndCodeSystemFound(value, codeSys, vs, codeAndCodeSys)
        else if(codeAndCodeSys.isEmpty) VSValidationCode.CodeAndCodeSystemNotFound(value, codeSys, vs.id)
        else codeToVsValidation(codeAndCodeSys.head, value, vs)
      }
      case Detection(detection) => detection
    }
  }

  def createEntry(element: Element, issue: VSValidationCode, bindingStrength: Option[BindingStrength])
                 (implicit Detections: ConfigurableDetections): Entry = {
    issue match {
      case VSValidationCode.NoUsage(code, value, vs) => Detections.vsCodeFound("no-code-usage", element.location, value, vs.id, vs.stability.orNull, vs.extensibility.orNull, bindingStrength.orNull, code.pattern.isDefined, code.value, code.pattern.getOrElse(""))
      case VSValidationCode.RVS(code, value, vs) => Detections.vsCodeFound("rvs", element.location, value, vs.id, vs.stability.orNull, vs.extensibility.orNull, bindingStrength.orNull, code.pattern.isDefined, code.value, code.pattern.getOrElse(""))
      case VSValidationCode.PVS(code, value, vs) => Detections.vsCodeFound("pvs", element.location, value, vs.id, vs.stability.orNull, vs.extensibility.orNull, bindingStrength.orNull, code.pattern.isDefined, code.value, code.pattern.getOrElse(""))
      case VSValidationCode.EVS(code, value, vs) => Detections.vsCodeFound("evs",element.location, value, vs.id, vs.stability.orNull, vs.extensibility.orNull, bindingStrength.orNull, code.pattern.isDefined, code.value, code.pattern.getOrElse(""))
      case VSValidationCode.MultipleCodesFoundInValueSet(value, vs, _) => Detections.duplicateCode(element.location, value, vs.id)
      case VSValidationCode.MultipleCodeAndCodeSystemFound(value, cs, vs, _) => Detections.duplicateCodeAndCodeSystem(element.location, value, cs, vs.id)
      case VSValidationCode.FoundInMultipleValueSets(value, cs, vs) => cs match {
        case Some(csString) =>  Detections.duplicateCodeAndCodeSystem(element.location, value, csString, vs.map(_.id).mkString("[", " ,", "]"))
        case None => Detections.duplicateCode(element.location, value, vs.map(_.id).mkString("[", " ,", "]"))
      }
      case VSValidationCode.CodedElementXOR(codes, vs) => Detections.codedElementXOR(element.location, codes.map(_.code.location.prettyString).mkString(", "), vs.id, vs.stability.orNull, vs.extensibility.orNull, bindingStrength.orNull)
      case VSValidationCode.CodeNotFound(code, codeSys, vsList) => codeSys match {
        case Some(cs) => Detections.codeNotFoundCodedElement(element.location, code, cs, vsList.map(_.id).mkString("[", " ,", "]"), getStability(vsList).orNull, getExtensibility(vsList).orNull, bindingStrength.orNull)
        case None =>  Detections.codeNotFound(element.location, code, vsList.map(_.id).mkString("[", " ,", "]"), getStability(vsList).orNull, getExtensibility(vsList).orNull, bindingStrength.orNull)
      }
      case VSValidationCode.SimpleCodeFoundInvalidCodeSystem(code, expected, found, vs) => Detections.codeNotFoundInvalidCodeSystem(element.location, code, vs.id, expected, found, vs.stability.orNull, vs.extensibility.orNull, bindingStrength.orNull)
      case VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated(code, cs, location, vs) => Detections.codeNotFoundCsNotPopulated(element.location, code, cs, location, vs.id, vs.stability.orNull, vs.extensibility.orNull, bindingStrength.orNull)
      case VSValidationCode.EmptyVS(vs) => Detections.emptyVS(element.location, vs, null)
      case VSValidationCode.ExcludedVS(vs) => Detections.vsNoVal(element.location, vs)
      case VSValidationCode.VSNotFound(vs) => Detections.vsNotFound(element.location, vs)
      case VSValidationCode.UBS(binding) => Detections.ubs(element.location, binding.bindings.mkString("[", " ,", "]"))
      case VSValidationCode.MultipleVSForPrimitive(binding) => Detections.multiVsSimple(element.location,  binding.bindings.mkString("[", " ,", "]"))
      case VSValidationCode.InvalidCodeBindingLocation(location, multiple) =>
        if(multiple) Detections.bindingLocation(element.location, s"Resolving Code's path ${location} from target element ${element.location.prettyString} returned more than one element")
        else Detections.bindingLocation(element.location, s"Unresolvable Code's path ${location} from target element ${element.location.prettyString}")
      case VSValidationCode.InvalidCodeSystemBindingLocation(location, multiple) =>
        if(multiple) Detections.bindingLocation(element.location, s"Resolving Code System's path ${location} from target element ${element.location.prettyString} returned more than one element")
        else Detections.bindingLocation(element.location, s"Unresolvable Code System's path ${location} from target element ${element.location.prettyString}")
      case VSValidationCode.UsageAndExtensibilityNotCompatible(code, vs) => Detections.incompatibleUsageAndExtensibility(element.location, code.value, vs.id)
      case VSValidationCode.DynamicValueSet(vs) => Detections.dynamicVs(element.location, vs.id)
      case VSValidationCode.SingleCodeMultiLocationXOR(context, elms, sg) => Detections.singleCodeMultiLocationXOR(sg.code, sg.codeSystem, elms.asJava, context)
      case VSValidationCode.SingleCodeInvalidCodeSystem(target, code, codeSystem, sg) => Detections.singleCodeInvalidCodeSystem(sg.code, sg.codeSystem, code, codeSystem, target)
      case VSValidationCode.SingleCodeCodeSystemNotFound(context, code, location, sg) => Detections.singleCodeCSNotFound(sg.code, sg.codeSystem, code, s"""'$location'""", context)
      case VSValidationCode.SingleCodeNotFound(e, code, sg) => Detections.singleCodeCodeNotFound(sg.code, sg.codeSystem, e, e)
      case VSValidationCode.SingleCodeSuccess(target, code, sg) => Detections.singleCodeSuccess(sg.code, sg.codeSystem, code, target)
      case _ => null
    }
  }

  def getStability(vsList: List[ValueSet]): Option[Stability] = {
    val containsDynamic = vsList.exists(_.stability.contains(Stability.Dynamic));
    val containsStatic = vsList.exists(_.stability.contains(Stability.Static));

    if(containsDynamic) Some(Stability.Dynamic)
    else if(containsStatic) Some(Stability.Static)
    else None
  }

  def getExtensibility(vsList: List[ValueSet]): Option[Extensibility] = {
    val containsOpen = vsList.exists(_.extensibility.contains(Extensibility.Open));
    val containsClosed = vsList.exists(_.extensibility.contains(Extensibility.Closed));

    if(containsOpen) Some(Extensibility.Open)
    else if(containsClosed) Some(Extensibility.Closed)
    else None
  }

}