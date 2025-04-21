package hl7.v2.validation.vs

import hl7.v2.validation.report.ConfigurableDetections

import scala.concurrent.Future
import gov.nist.validation.report.Entry

import scala.jdk.CollectionConverters.{ListHasAsScala, SeqHasAsJava}
import scala.concurrent.ExecutionContext.Implicits.global
import hl7.v2.instance._

import scala.util.{Failure, Success}
import hl7.v2.profile.{BindingStrength, Usage}
import hl7.v2.validation.FeatureFlags
import hl7.v2.validation.vs.external.client.ExternalCodeValidationException
import hl7.v2.validation.vs.factory.ValueSetFacade

sealed trait VSValidationCode {
  val inconclusive: Boolean = false
  val isSpecError: Boolean = false
  val codeIsFound: Boolean = false
}

sealed trait VSValidationCodeSpecError extends VSValidationCode {
  override val isSpecError = true
}

sealed trait VSValidationInconclusive extends VSValidationCode {
  override val inconclusive = true
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
  def get: T = throw new NoSuchElementException("Detection.get")
  def detection: VSValidationCode = code
}

case class Empty[T]() extends Resolve[T] {
  def isValue = false
  def get: T = throw new NoSuchElementException("Empty.get")
  def detection: VSValidationCode = throw new NoSuchElementException("Empty.get")
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
  case class DynamicValueSet(vs: ValueSet) extends VSValidationCode
  // Single Code Detections
  case class SingleCodeSuccess(context: Element, code: Simple, sg: SingleCodeBinding) extends VSValidationCode
  case class SingleCodeNotFound(e: Simple, code: String, sg: SingleCodeBinding) extends VSValidationCode
  case class SingleCodeCodeSystemNotFound(context: Element, code: Simple, location: String, sg: SingleCodeBinding) extends VSValidationCode
  case class SingleCodeInvalidCodeSystem(context: Element, code: Simple, codeSystem: Simple, sg: SingleCodeBinding) extends VSValidationCode
  case class SingleCodeMultiLocationXOR(context: Element, e: List[Element], sg: SingleCodeBinding) extends VSValidationCode
  // External Value Set Detections
  case class ExternalValueSetValidationSpecError(code: String, message: String, vs: ExternalValueSet) extends VSValidationInconclusive
}

case class Bindings(parentLocationId: String, parentType: EType, valueSetBindings: List[ValueSetBinding], singleCodeBindings: List[SingleCodeBinding])
class BindingOverrideMap {
  private val overrides = scala.collection.mutable.Map[String, Bindings]()
  def partition(elements: List[Element], parent: Location): (List[Element], List[Element]) = {
    elements.partition(e => {
      val key = e.location.instancePositionPath
      overrides.contains(key) && !(overrides(key).parentLocationId.equals(parent.instancePositionPath) && overrides(key).parentType.equals(parent.eType))
    })
  }
  def addValueSetBinding(elements: List[Element], parent: Location, valueSetBinding: ValueSetBinding): Unit = {
    for(element <- elements) {
      val key = element.location.instancePositionPath
      val existing = overrides.getOrElse(key, Bindings(parent.instancePositionPath, parent.eType, List(), List()))
      if(existing.parentLocationId.equals(parent.instancePositionPath)) {
        overrides.put(key, Bindings(parent.instancePositionPath, parent.eType, valueSetBinding::existing.valueSetBindings, existing.singleCodeBindings))
      }
    }
  }
  def addSingleCodeBinding(elements: List[Element], parent: Location, singleCodeBinding: SingleCodeBinding): Unit = {
    for(element <- elements) {
      val key = element.location.instancePositionPath
      val existing = overrides.getOrElse(key, Bindings(parent.instancePositionPath, parent.eType, List(), List()))
      if(existing.parentLocationId.equals(parent.instancePositionPath)) {
        overrides.put(key, Bindings(parent.instancePositionPath, parent.eType, existing.valueSetBindings, singleCodeBinding::existing.singleCodeBindings))
      }
    }
  }
}


trait DefaultValueSetValidator extends VsValidator with EscapeSeqHandler {


  def checkValueSets(m: Message, additionalVsBindings: Option[ValueSetSpecification])
                    (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags): Future[Seq[Entry]] = Future {
    implicit val overridesMap: BindingOverrideMap = new BindingOverrideMap()
    checkMsgElement(m.asGroup, additionalVsBindings)
  }

  // Only check bindings when element should not be skipped
  def checkMsgElement(e: Element, additionalVsBindings: Option[ValueSetSpecification])
                     (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags, overridesMap: BindingOverrideMap): List[Entry] = {
    if (skipElementCheck(e)) {
      Nil
    } else {
      check(e, additionalVsBindings)
    }
  }

  // skip element validation when Usage is X or W
  private def skipElementCheck(element: Element): Boolean = {
    element.req.usage match {
      case Usage.X | Usage.W => true
      case _ => false
    }
  }

  // Checks if specification is available on element and checks it, then dives into complex elements
  def check(e: Element, additionalVsBindings: Option[ValueSetSpecification])
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags, overridesMap: BindingOverrideMap): List[Entry] = {
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

  private def getValueSetBindingsForElement(e: Element, additionalVsBindings: Option[ValueSetSpecification] = None): List[ValueSetBinding] = {
    val fromVsBindingsSpecification = vsSpecification.vsSpecificationFor(e)
    val fromAdditionalVsBindings = additionalVsBindings.flatMap(_.vsSpecificationFor(e))
    fromVsBindingsSpecification.getOrElse(Nil) ::: fromAdditionalVsBindings.getOrElse(Nil)
  }

  private def getSingleCodeBindingsForElement(e: Element, additionalVsBindings: Option[ValueSetSpecification] = None): List[SingleCodeBinding] = {
    val fromVsBindingsSpecification = vsSpecification.singleCodeSpecificationFor(e)
    val fromAdditionalVsBindings = additionalVsBindings.flatMap(_.singleCodeSpecificationFor(e))
    fromVsBindingsSpecification.getOrElse(Nil) ::: fromAdditionalVsBindings.getOrElse(Nil)
  }

  private def resolveTargetAndSkipAndDo(e: Element, target: String, fn: List[Element] => List[Entry]): List[Entry] = {
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
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags, overridesMap: BindingOverrideMap): List[Entry] = {
    resolveTargetAndSkipAndDo(
      e,
      spec.target,
      checkable => {
        checkIfNoHigherLevelOverrides(checkable, e, spec).map(entry => createEntry(entry.target, entry.code, entry.strength))
      }
    )
  }

  /* Resolve binding target from context.
   Validates binding if target is not skipped (skip on target Usage X or W)
 */
  def check(e: Element, spec: SingleCodeBinding)
           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags, overridesMap: BindingOverrideMap): List[Entry] = {
    resolveTargetAndSkipAndDo(
      e,
      spec.target,
      checkable => {
        checkIfNoHigherLevelOverrides(checkable, e, spec).map(entry => createEntry(entry.target, entry.code, entry.strength))
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

  private def checkIfNoHigherLevelOverrides(elements: List[Element], parent: Element, spec: ValueSetBinding)
                                           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags, overridesMap: BindingOverrideMap): List[VsEntry] = {
    if(featureFlags.overrideLowerLevelBindings) {
      val (_, elementsToBeChecked) = overridesMap.partition(elements, parent.location)
      overridesMap.addValueSetBinding(elementsToBeChecked, parent.location, spec)
      check(elementsToBeChecked, spec)
    } else {
      check(elements, spec)
    }
  }

  private def checkIfNoHigherLevelOverrides(elements: List[Element], parent: Element, spec: SingleCodeBinding)
                                           (implicit Detections: ConfigurableDetections, featureFlags: FeatureFlags, overridesMap: BindingOverrideMap): List[VsEntry] = {
    if(featureFlags.overrideLowerLevelBindings) {
      val (_, elementsToBeChecked) = overridesMap.partition(elements, parent.location)
      overridesMap.addSingleCodeBinding(elementsToBeChecked, parent.location, spec)
      check(elementsToBeChecked, spec)
    } else {
      check(elements, spec)
    }
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

  private def collectValues[T](resolve: List[Resolve[T]]): List[T] = {
    resolve.collect {
      case Payload(value) => value
      case PayloadAndDetection(value, _) => value
    }
  }

  private def collectIssues[T](resolve: List[Resolve[T]]): List[VSValidationCode] = {
    resolve.collect {
      case Detection(issue) => issue
      case PayloadAndDetection(_, issue) => issue
    }
  }

  private def resolveValidationPayload(targets: List[Element], spec: ValueSetBinding): List[ValueSetValidationPayload] = {
    val rValueSets = resolveBindings(spec.bindings)
    val vsIssues = collectIssues(rValueSets)
    val valueSets = collectValues(rValueSets)

    for (target <- targets) yield {
      // Resolve Code Holders
      val rCodeHolders = for (location <- spec.bindingLocations) yield {
        codeHolder(target, location)
      }
      val codeHolders = collectValues(rCodeHolders)
      val chIssues = collectIssues(rCodeHolders)

      ValueSetValidationPayload(target, codeHolders, valueSets, spec.strength, vsIssues:::chIssues)
    }
  }

  private def codeHolder(context: Element, location: BindingLocation): Resolve[CodeHolder] = {
    getBindingLocationValue(context, location.codeLocation, VSValidationCode.InvalidCodeBindingLocation) match {
      case Empty() => Empty[CodeHolder]()
      case Detection(issue: VSValidationCode) => Detection[CodeHolder](issue)
      case Payload(code: Simple) => location.codeSystemLocation match {
        case None => Payload[CodeHolder](CodeHolder(code, None, location))
        case Some(codeSysLocation) => getBindingLocationValue(context, codeSysLocation, VSValidationCode.InvalidCodeSystemBindingLocation) match {
          case Empty() => Payload[CodeHolder](CodeHolder(code, None, location))
          case Detection(issue: VSValidationCode) => Detection[CodeHolder](issue)
          case Payload(codeSystem: Simple) => Payload[CodeHolder](CodeHolder(code, Some(codeSystem), location))
        }
      }
    }
  }

  private def getBindingLocationValue(context: Element, location: String, detectionFactory: (String, Boolean) => VSValidationCodeSpecError): Resolve[Simple] = {
    Query.queryAsSimple(context, location) match {
      case Success(x) =>
        if (x.size > 1) Detection[Simple](detectionFactory(location, true))
        else if (x.isEmpty) Empty[Simple]()
        else Payload[Simple](x.head)
      case Failure(_) => Detection[Simple](detectionFactory(location, false))
    }
  }

  private def resolveBindings(bindings: List[String]): List[Resolve[ValueSetFacade]] = {
    for (id <- bindings) yield {
      try {
        if (valueSetFactory.isExcludedFromValidation(id)) {
          Detection[ValueSetFacade](VSValidationCode.ExcludedVS(id))
        }
        else {
          val vs = valueSetFactory.getValueSetById(id)
          if(vs.getValueSet.isEmpty) Detection[ValueSetFacade](VSValidationCode.EmptyVS(vs.getValueSet))
          else Payload(vs)
        }
      }
      catch {
        case _: ValueSetNotFoundException => Detection[ValueSetFacade](VSValidationCode.VSNotFound(id))
        case _: Throwable => Detection[ValueSetFacade](VSValidationCode.VSNotFound(id))
      }
    }
  }

  def validate(target: Element, values: List[CodeHolder], spec: SingleCodeBinding): List[VSValidationCode] = {
    val detections = values.map(codeHolder => {
      codeHolder.code.value.raw match {
        // When code matches
        case spec.code => codeHolder.codeSystem match {
          // When code system exists
          case Some(cs) => cs.value.raw match {
            // When code system matches
            case spec.codeSystem => VSValidationCode.SingleCodeSuccess(target, codeHolder.code, spec)
            // When code system doesn't match
            case _ => VSValidationCode.SingleCodeInvalidCodeSystem(target, codeHolder.code, cs, spec)
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

    // collect success, notfound, code system errors and inconclusive
    val (success, notfound, cs, not_found_cs) = (
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
    else notfound ::: cs ::: not_found_cs
  }

  def validate(validationPayload: ValueSetValidationPayload)(implicit featureFlags: FeatureFlags): List[VSValidationCode] = {
    // Check All Collected Codes Against All Bindings
    val vCodeBucket: List[(CodeHolder, ValueSetFacade, VSValidationCode)] = validationPayload.values.flatMap(code => {
      for (vs <- validationPayload.valueSets) yield {
        (code, vs, checkCode(code, vs))
      }
    })

    val vsCodes: Iterable[(CodeHolder, List[VSValidationCode], List[ValueSet])] = for(codeDetection <- vCodeBucket.groupBy(_._1)) yield {
      val (codeHolder, validationResultList) = codeDetection
      val (codeFoundResults, codeNotFoundResults) = validationResultList.partition(tuple => {
        val (_, _, detectionCode) = tuple
        detectionCode.codeIsFound
      })
      val foundInMultipleValueSetsDetection: Option[VSValidationCode] = checkFoundInMultipleValueSets(codeHolder, codeFoundResults)
      val codesFoundDetections: List[VSValidationCode] = codeFoundResults.map(tuple => {
        val (_, _, detectionCode) = tuple
        detectionCode
      })
      val specErrorDetections = codeNotFoundResults.filter(_._3.isSpecError).map(_._3)
      val invalidCodeSystem: List[VSValidationCode] = if(codesFoundDetections.nonEmpty) Nil else codeNotFoundResults.collect({
        case (_, _, value: VSValidationCode.SimpleCodeFoundInvalidCodeSystem) => value
      })
      val csNotPopulated: List[VSValidationCode] = if(codesFoundDetections.nonEmpty) Nil else codeNotFoundResults.collect({
        case (_, _, value: VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated) => value
      })
      val inconclusiveDetections = codeNotFoundResults.filter(_._3.inconclusive).map(_._3)
      val codeIsNotFound: Boolean = codesFoundDetections.isEmpty && csNotPopulated.isEmpty && inconclusiveDetections.isEmpty
      val codeNotFound: Option[VSValidationCode] = if(codeIsNotFound) {
        Some(VSValidationCode.CodeNotFound(codeHolder.code.value.raw, codeHolder.codeSystem.map(_.value.raw), validationResultList.map(_._2.getValueSet)))
      } else {
        None
      }
      val detections = {
          codesFoundDetections:::
          specErrorDetections:::
          invalidCodeSystem:::
          csNotPopulated:::
          inconclusiveDetections:::
          codeNotFound.map(_ :: Nil).getOrElse(Nil):::
          foundInMultipleValueSetsDetection.map(_ :: Nil).getOrElse(Nil)
      }
      val matches = codesFoundDetections.map(_.asInstanceOf[VSValidationCodeFound]).flatMap(_.getValueSets)
      (codeHolder, detections.flatMap(expandVSValidationCode), matches)
    }

    detectAndClearCodeNotFoundIfMatch(vsCodes) ::: detectXOR(vsCodes.map(elm => (elm._1, elm._3)).toList)
  }

  private def detectXOR(matches : List[(CodeHolder, List[ValueSet])]): List[VSValidationCode] = {
    val vsMatchMap: Map[ValueSet, List[CodeHolder]] = matches.flatMap(x => x._2.map(y => (x._1, y))).distinct.groupBy(_._2).map(tuple => tuple._1 -> tuple._2.map(_._1))
    val detections = for(vs: (ValueSet, List[CodeHolder]) <- vsMatchMap) yield {
      if(vs._2.size > 1) Some(VSValidationCode.CodedElementXOR(vs._2, vs._1))
      else None
    }

    detections.flatten.toList
  }

  private def detectAndClearCodeNotFoundIfMatch(vsCodes : Iterable[(CodeHolder, List[VSValidationCode], List[ValueSet])]): List[VSValidationCode] = {
    val matchFound = vsCodes.flatMap(_._3).nonEmpty
    val detections = vsCodes.flatMap(_._2).toList
    if(matchFound) detections.filter(d => d.codeIsFound || d.isSpecError || d.inconclusive)
    else detections
  }

  private def checkFoundInMultipleValueSets(code: CodeHolder, vsCodeFound: List[(CodeHolder, ValueSetFacade, VSValidationCode)]): Option[VSValidationCode.FoundInMultipleValueSets] = {
    if(vsCodeFound.size > 1) Some(VSValidationCode.FoundInMultipleValueSets(code.code.value.raw, code.codeSystem.map(_.value.raw), vsCodeFound.map(_._2.getValueSet).distinct))
    else None
  }

  private def checkCode(holder: CodeHolder, vs: ValueSetFacade)
                       (implicit featureFlags: FeatureFlags): VSValidationCode = {
    holder match {
      case CodeHolder(code, None, _) => holder.location.codeSystemLocation match {
        case Some(csLocation) => checkValue(code.value.raw, vs) match {
          case Detection(detection) => detection
          case PayloadAndDetection(code, _) => VSValidationCode.SimpleCodeFoundCodeSystemNotPopulated(code.value, code.codeSys, csLocation, vs.getValueSet)
        }
        case None => checkValue(code.value.raw, vs).detection
      }
      case CodeHolder(code, Some(codeSys), _) => checkTriplet(code.value.raw, codeSys.value.raw, vs)
    }
  }

  def checkValue(code: String, vs: ValueSetFacade)(implicit featureFlags: FeatureFlags): Resolve[Code] = {
    try {
      val codes = getCodes(code, vs)
      if (codes.isEmpty) Detection[Code](VSValidationCode.SimpleCodeNotFound(code, vs.getValueSet.id))
      else if (codes.length > 1) Detection[Code](VSValidationCode.MultipleCodesFoundInValueSet(code, vs.getValueSet, codes))
      else PayloadAndDetection[Code](codes.head, codeToVsValidation(codes.head, code, vs.getValueSet))
    }
    catch {
      case e: ExternalCodeValidationException => Detection[Code](VSValidationCode.ExternalValueSetValidationSpecError(code, e.getMessage, e.getValueSet))
    }
  }

  def getCodes(code: String, vs: ValueSetFacade)(implicit featureFlags: FeatureFlags): List[Code] = {
    if(featureFlags.legacy0396 && vs.getValueSet.isInstanceOf[InternalValueSet] && vs.getValueSet.id.matches(".*0396.*")) {
      vs.getCodeList.asScala.toList.filter(c => codeMatchLegacy0396(c, code))
    } else {
      vs.getCodeMatches(code).asScala.toList
    }
  }

  private def codeMatchLegacy0396(code: Code, value: String): Boolean = {
    code.pattern match {
      case Some(pattern) => value.matches(pattern)
      case None =>
        if(code.value.equals("HL7nnnn")) value.matches("HL7[0-9]{4}")
        else if(code.value.equals("99zzz")) value.matches("99[a-zA-Z0-9]{3}")
        else code.value.equals(value)
    }
  }

  private def codeToVsValidation(code: Code, value: String, vs: ValueSet): VSValidationCode = {
    code.usage match {
      case CodeUsage.P => VSValidationCode.PVS(code, value, vs)
      case CodeUsage.E => VSValidationCode.EVS(code, value, vs)
      case CodeUsage.R => VSValidationCode.RVS(code, value, vs)
      case _ => VSValidationCode.NoUsage(code, value, vs)
    }
  }

  private def expandVSValidationCode(vsCode: VSValidationCode): List[VSValidationCode] = {
    vsCode match {
      case detection : VSValidationCode.MultipleCodesFoundInValueSet => detection :: detection.codes.map(code => codeToVsValidation(code, detection.value, detection.vs))
      case detection : VSValidationCode.MultipleCodeAndCodeSystemFound => detection :: detection.codes.map(code => codeToVsValidation(code, detection.value, detection.vs))
      case detection : VSValidationCode.CodeNotFound => detection::detection.bindings.filter(_.stability.contains(Stability.Dynamic)).map(VSValidationCode.DynamicValueSet)
      case x => List(x)
    }
  }

  private def checkTriplet(value: String, codeSys: String, vs: ValueSetFacade)(implicit featureFlags: FeatureFlags): VSValidationCode = {
    checkValue(value, vs) match {
      case PayloadAndDetection(code, issue) => if (code.codeSys == codeSys) issue else VSValidationCode.SimpleCodeFoundInvalidCodeSystem(value, code.codeSys, codeSys, vs.getValueSet)
      case Detection(detection : VSValidationCode.MultipleCodesFoundInValueSet) =>
        val codeAndCodeSys = detection.codes.filter(code => code.codeSys.equals(codeSys))
        if(codeAndCodeSys.size > 1) VSValidationCode.MultipleCodeAndCodeSystemFound(value, codeSys, vs.getValueSet, codeAndCodeSys)
        else if(codeAndCodeSys.isEmpty) VSValidationCode.CodeAndCodeSystemNotFound(value, codeSys, vs.getValueSet.id)
        else codeToVsValidation(codeAndCodeSys.head, value, vs.getValueSet)
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
        if(multiple) Detections.bindingLocation(element.location, s"Resolving Code's path $location from target element ${element.location.prettyString} returned more than one element")
        else Detections.bindingLocation(element.location, s"Unresolvable Code's path $location from target element ${element.location.prettyString}")
      case VSValidationCode.InvalidCodeSystemBindingLocation(location, multiple) =>
        if(multiple) Detections.bindingLocation(element.location, s"Resolving Code System's path $location from target element ${element.location.prettyString} returned more than one element")
        else Detections.bindingLocation(element.location, s"Unresolvable Code System's path $location from target element ${element.location.prettyString}")
      case VSValidationCode.DynamicValueSet(vs) => Detections.dynamicVs(element.location, vs.id)
      case VSValidationCode.SingleCodeMultiLocationXOR(context, elms, sg) => Detections.singleCodeMultiLocationXOR(sg.code, sg.codeSystem, elms.asJava, context)
      case VSValidationCode.SingleCodeInvalidCodeSystem(target, code, codeSystem, sg) => Detections.singleCodeInvalidCodeSystem(sg.code, sg.codeSystem, code, codeSystem, target)
      case VSValidationCode.SingleCodeCodeSystemNotFound(context, code, location, sg) => Detections.singleCodeCSNotFound(sg.code, sg.codeSystem, code, s"""'$location'""", context)
      case VSValidationCode.SingleCodeNotFound(e, _, sg) => Detections.singleCodeCodeNotFound(sg.code, sg.codeSystem, e, e)
      case VSValidationCode.SingleCodeSuccess(target, code, sg) => Detections.singleCodeSuccess(sg.code, sg.codeSystem, code, target)
      case VSValidationCode.ExternalValueSetValidationSpecError(code, message, vs) => Detections.externalVsSpecError(element.location, code, message, vs)
      case _ => null
    }
  }

  private def getStability(vsList: List[ValueSet]): Option[Stability] = {
    val containsDynamic = vsList.exists(_.stability.contains(Stability.Dynamic))
    val containsStatic = vsList.exists(_.stability.contains(Stability.Static))

    if(containsDynamic) Some(Stability.Dynamic)
    else if(containsStatic) Some(Stability.Static)
    else None
  }

  private def getExtensibility(vsList: List[ValueSet]): Option[Extensibility] = {
    val containsOpen = vsList.exists(_.extensibility.contains(Extensibility.Open))
    val containsClosed = vsList.exists(_.extensibility.contains(Extensibility.Closed))

    if(containsOpen) Some(Extensibility.Open)
    else if(containsClosed) Some(Extensibility.Closed)
    else None
  }

}