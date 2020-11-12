package hl7.v2.validation.coconstraints

import expression.EvalResult.Trace
import expression.{EvalResult, Evaluator}
import hl7.v2.instance.{Element, Group, Message, Segment, Separators, TimeZone}
import hl7.v2.profile
import hl7.v2.validation.report.ConfigurableDetections
import hl7.v2.instance.Query._
import hl7.v2.validation.vs.{DefaultValueSetValidator, Validator, ValueSetBinding, ValueSetLibrary, VsEntry}
import java.util.{List => JList}

import gov.nist.validation.report.{Entry, Trace => GTrace}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.JavaConversions.seqAsJavaList
import scala.util.{Failure, Success}

sealed trait CoConstraintValidationCode
sealed trait CoConstraintSelector {
  val _match: Boolean
}
trait CoConstraintSelectorMatch extends CoConstraintSelector {
  val _match = true
}
trait CoConstraintSelectorNoMatch extends CoConstraintSelector {
  val _match = false
}

case class RequiredGroup(group: CoConstraintGroup) extends CoConstraintValidationCode
case class RequiredCoConstraint(coConstraint: CoConstraint) extends CoConstraintValidationCode
case class CardinalityCoConstraint(coConstraint: CoConstraint, found: Int) extends CoConstraintValidationCode
case class CardinalityGroup(group: CoConstraintGroup, found: Int) extends CoConstraintValidationCode
case class ElementIsNotSegment(path: String, element: Element) extends CoConstraintValidationCode

case class CoConstraintPlainTextFailure(coConstraint: CoConstraint, element: Element, constraint: PlainText, value: String) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintPlainTextSuccess(coConstraint: CoConstraint, element: Element, constraint: PlainText) extends CoConstraintValidationCode with CoConstraintSelectorMatch

case class CoConstraintCodeFailureNotFound(coConstraint: CoConstraint, element: Element, constraint: Code) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintCodeFailureInvalidCs(coConstraint: CoConstraint, element: Element, constraint: Code, cs: String) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch

case class CoConstraintCodeSuccess(coConstraint: CoConstraint, element: Element, constraint: Code) extends CoConstraintValidationCode with CoConstraintSelectorMatch

case class CoConstraintVsBindingFailure(coConstraint: CoConstraint, element: Element, constraint: ValueSet, vsEntries: List[VsEntry]) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintVsBindingSuccess(coConstraint: CoConstraint, element: Element, constraint: ValueSet, bindingFound: ValueSetBinding, vsEntries: List[VsEntry]) extends CoConstraintValidationCode with CoConstraintSelectorMatch

case class InconclusiveCondition(entry: Entry) extends CoConstraintValidationCode


trait DefaultValidator extends CoConstraintValidator with Evaluator with DefaultValueSetValidator {

  def checkCoConstraint(m: Message)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): Future[Seq[Entry]] = Future {
    implicit val separators: Separators = m.separators
    implicit val dtz: Option[TimeZone] = m.defaultTimeZone
    implicit val model: profile.Message = m.model
    Nil
  }

//  def checkGroup(group: Group)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): List[Entry] = {
//    group.children.foldLeft(List[Entry]()) {
//      (acc, child) => child match {
//        case group: Group => acc ++ checkGroupAndBindings(group, coConstraintValidationContext.coConstraintBindingsFor(group))
//        case _ => acc
//      }
//    }
//  }
//
//  def checkGroupAndBindings(group: Group, bindings: List[CoConstraintBinding])(implicit Detections : ConfigurableDetections, l: ValueSetLibrary, s: Separators,  t: Option[TimeZone], VSValidator : Validator): List[Entry] = {
//    bindings.foldLeft(List[Entry]()) {
//      (acc, binding) => acc ++ checkGroupAndBinding(group, binding)
//    }
//  }
//
//  def checkGroupAndBinding(group: Group, binding: CoConstraintBinding)(implicit Detections : ConfigurableDetections, l: ValueSetLibrary, s: Separators,  t: Option[TimeZone], VSValidator : Validator): List[Entry] = {
//    implicit val segmentName: String = binding.segmentName
//    implicit val context: Group = group
//    query(group, binding.target) match {
//      case Success(elements) =>
//        val (segments, issues) = sanitizeElementList(binding.target, elements)
//        val detections: List[CoConstraintValidationCode] = issues :: checkCoConstraints(segments, binding)
//        detections.flatMap(createEntry)
//      case Failure(exception) => Nil ///TODO Could Not Resolve Segment
//    }
//  }
//
//  def checkCoConstraints(segments: List[Segment], binding: CoConstraintBinding)(implicit Detections : ConfigurableDetections, l: ValueSetLibrary, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element): List[CoConstraintValidationCode] = {
//    binding.bindings.foldLeft(List[CoConstraintValidationCode]()) {
//      (acc, table) => table match {
//        case simple: SimpleCoConstraintTable => acc ++ checkCoConstraintTable(segments, simple.coConstraints, simple.coConstraintGroups)
//        case conditional: ConditionalCoConstraintTable => acc ++ checkConditionalCoConstraintTable(segments, conditional)
//      }
//    }
//  }
//
//  def checkCoConstraintTable(segments: List[Segment], coConstraints: List[CoConstraint], coConstraintGroups: List[CoConstraintGroup])(implicit Detections : ConfigurableDetections, l: ValueSetLibrary, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element): List[CoConstraintValidationCode] = {
//    val (remainingSegments, entries) = checkCoConstraintList(segments, coConstraints)
//    val (_, groupEntries) = coConstraintGroups.foldLeft((remainingSegments, entries)) {
//      (acc, group) => {
//        val (segmentsLeft, entries) = checkCoConstraintGroup(acc._1, group)
//        (segmentsLeft, acc._2 ++ entries)
//      }
//    }
//
//    entries ++ groupEntries
//  }
//
//  def checkConditionalCoConstraintTable(segments: List[Segment], binding: ConditionalCoConstraintTable)(implicit Detections : ConfigurableDetections, l: ValueSetLibrary, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element): List[CoConstraintValidationCode] = {
//    eval(binding.condition.assertion, context) match {
//      case EvalResult.Pass => checkCoConstraintTable(segments, binding.coConstraints, binding.coConstraintGroups)
//      case EvalResult.Inconclusive(trace) => InconclusiveCondition(Detections.csEntry("", context.location, context, "", None, None, binding.condition.description, stackTrace(context, trace :: Nil))) :: Nil
//      case _ => Nil
//    }
//  }
//
//
//  def checkCoConstraintList(segments: List[Segment], coConstraints: List[CoConstraint])(implicit Detections : ConfigurableDetections, context: Element): (List[Segment], List[CoConstraintValidationCode]) = {
//    coConstraints match {
//      case coConstraint :: list =>
//        val (remainingSegments, detectionsOfCoConstraint) = checkCoConstraint(segments: List[Segment], coConstraint: CoConstraint)
//        val (leftSegments, detectionsOfRemaining) = checkCoConstraintList(remainingSegments, list)
//        (leftSegments, detectionsOfCoConstraint ++ detectionsOfRemaining)
//      case Nil => (segments, Nil)
//    }
//  }
//
//  def checkCoConstraintGroup(segments: List[Segment], group: CoConstraintGroup)(implicit Detections : ConfigurableDetections, context: Element): (List[Segment], List[CoConstraintValidationCode]) = {
//    val primary = group.coConstraints.find(_.primary).get
//    val (matchedPrimary, others) = segments.partition(getPartitionMatch(_, primary))
//    val reqIssues = checkGroupRequirement(group, matchedPrimary)
//
//    if(matchedPrimary.nonEmpty) {
//      val primaryIssues = checkCoConstraintRequirement(primary, matchedPrimary) :::  checkConstraints(matchedPrimary, primary.constraints)
//      val (remainingSegments, othersIssues) = checkCoConstraintList(others, group.coConstraints.filter(!_.primary))
//      (remainingSegments, primaryIssues ++ othersIssues)
//    } else {
//      (segments, reqIssues)
//    }
//  }
//
//  def checkCoConstraint(segments: List[Segment], coConstraint: CoConstraint) (implicit Detections : ConfigurableDetections, context: Element): (List[Segment], List[CoConstraintValidationCode]) = {
//    val (matches, noMatches) = segments.partition(getPartitionMatch(_, coConstraint))
//    val requirementIssues = checkCoConstraintRequirement(coConstraint, matches)
//    val constraintIssues = checkConstraints(matches, coConstraint.constraints)
//    (noMatches, requirementIssues ++ constraintIssues)
//  }
//
//  def getPartitionMatch(segment: Segment, cc: CoConstraint)(implicit Detections : ConfigurableDetections, context: Element): Boolean = {
//    cc.selectors.flatMap {
//      case plainText: PlainText => validatePlainText(segment, plainText, cc)
//      case code: Code => validateCode(segment, code, cc)
//      case valueSet: ValueSet => validateValueSetBinding(segment, valueSet, cc)
//    }.exists(_._match)
//  }
//
//  def checkCoConstraintRequirement(coConstraint: CoConstraint, segments: List[Segment]): List[CoConstraintValidationCode] = {
//    if (segments.isEmpty && coConstraint.requirement.usage.equals(CoConstraintUsage.R))  RequiredCoConstraint(coConstraint)::Nil
//    else if (!coConstraint.requirement.cardinality.includes(segments.size)) CardinalityCoConstraint(coConstraint, segments.size)::Nil
//    else Nil
//  }
//
//  def checkGroupRequirement(group: CoConstraintGroup, segments: List[Segment]): List[CoConstraintValidationCode] = {
//    if (segments.isEmpty && group.requirement.usage.equals(CoConstraintUsage.R))  RequiredGroup(group)::Nil
//    else if (!group.requirement.cardinality.includes(segments.size)) CardinalityGroup(group, segments.size)::Nil
//    else Nil
//  }
//
//  def checkConstraints(segments: List[Segment], constraints: List[CoConstraintCell])(implicit Detections : ConfigurableDetections): List[CoConstraintValidationCode] = Nil
//
//  def validatePlainText(segment: Segment, plainText: PlainText, cc: CoConstraint)(implicit Detections : ConfigurableDetections): Option[CoConstraintValidationCode with CoConstraintSelector] =
//    queryAsSimple(segment, plainText.path) match {
//      case Success(elements) => elements match {
//        case Nil => None
//        case list => val detection = list.find(_.value.raw.equals(plainText.value)).map(element => CoConstraintPlainTextSuccess(cc, element, plainText))
//        detection match {
//          case None => Some(CoConstraintPlainTextFailure(cc, elements.head, plainText, elements.head.value.raw))
//          case x => x
//        }
//      }
//      ///TODO Handle Inconclusive case
//      case Failure(f) => None
//  }
//
//  def validateCode(segment: Segment, code: Code, cc: CoConstraint)(implicit Detections : ConfigurableDetections, context: Element): Option[CoConstraintValidationCode with CoConstraintSelector] = {
//    query(segment, code.path) match {
//      case Success(elements) =>
//        val detections: List[CoConstraintValidationCode with CoConstraintSelector] = elements.flatMap(repetition => {
//          code.bindingLocation.map(bl => {
//            validateCodeBindingLocation(repetition, code, cc, bl)
//          })
//        }).flatten
//
//        detections.find(_.isInstanceOf[CoConstraintCodeSuccess]).orElse(
//          detections.find(_.isInstanceOf[CoConstraintCodeFailureInvalidCs]).orElse(
//            Some(CoConstraintCodeFailureNotFound(cc, context, code))
//          )
//        )
//      ///TODO Handle Inconclusive case
//      case Failure(f) => None
//    }
//  }
//
//  def validateCodeBindingLocation(elm: Element, code: Code, cc: CoConstraint, location: CoConstraintBindingLocation)(implicit Detections : ConfigurableDetections): Option[CoConstraintValidationCode with CoConstraintSelector] = {
//    (queryAsSimple(elm, location.code), queryAsSimple(elm, location.codeSystem)) match {
//      case (Success(_code::Nil), Success(_codeSystem::Nil)) => if(code.code.equals(_code.value.raw)) {
//        if(code.codeSystem.equals(_codeSystem.value.raw)) {
//          Some(CoConstraintCodeSuccess(cc, _code, code))
//        } else {
//          Some(CoConstraintCodeFailureInvalidCs(cc, _code, code, _codeSystem.value.raw))
//        }
//      } else {
//        Some(CoConstraintCodeFailureNotFound(cc, _code, code))
//      }
//      ///TODO Handle Inconclusive case
//      case (Success(_), Success(_)) => None
//      case (Failure(_), Failure(_)) => None
//    }
//  }
//
//  def validateValueSetBinding(segment: Segment, vs: ValueSet, coConstraint: CoConstraint)(implicit Detections : ConfigurableDetections): Option[CoConstraintValidationCode with CoConstraintSelector] = {
//    query(segment, vs.path) match {
//      case Success(list) => list match {
//        case Nil => None
//        case elements => val entries: List[(ValueSetBinding, List[VsEntry])] = vs.bindings.map(b => (b, check(elements, b)))
//          val bindingSuccess: Option[(ValueSetBinding, List[VsEntry])] = entries.find(_._2.exists(_.code.codeIsFound))
//
//          if(bindingSuccess.isDefined) {
//            val element: Option[Element] = bindingSuccess.get._2.find(_.code.codeIsFound).map(_.target)
//            Some(CoConstraintVsBindingSuccess(coConstraint, element.get, vs, bindingSuccess.get._1, bindingSuccess.get._2))
//          } else {
//            Some(CoConstraintVsBindingFailure(coConstraint, elements.head, vs, bindingSuccess.get._2))
//          }
//      }
//      ///TODO Handle Inconclusive case
//      case Failure(f) => None
//    }
//  }
//
//  def sanitizeElementList(path: String, elmList: List[Element])(implicit Detections : ConfigurableDetections): (List[Segment], List[CoConstraintValidationCode]) = {
//    elmList.foldLeft((List[Segment](), List[CoConstraintValidationCode]())) {
//      (acc, elm) =>
//        try {
//          (elm.asInstanceOf[Segment] :: acc._1, acc._2)
//        } catch {
//          case e: Exception => (acc._1, ElementIsNotSegment(path, elm) :: acc._2)
//        }
//    }
//  }
//
//  private def stackTrace(context: Element, stack: List[Trace]): JList[GTrace] =
//    seqAsJavaList(
//      stack map { t =>
//        val assertion = expression.AsString.expression(t.expression, context)
//        val reasons = t.reasons map { r =>
//          s"[${r.location.line}, ${r.location.column}] ${r.message}"
//        }
//        new GTrace(assertion, seqAsJavaList(reasons))
//    })
//
//  private def selectorToString(cell: CoConstraintCell): String = {
//    cell match {
//      case PlainText(element, path, value) => s"""${element} has value of ${value}"""
//      case Code(element, path, code, codeSystem, bindingLocation)  => s"""${element}  has code "${code}" from code system "${codeSystem}" at one of positions ${bindingLocation.map(_.position).mkString("[", ", ","]")}"""
//      case ValueSet(element, path, bindings) => s"""${element} has a value from ${bindings.map(b => s"""value sets "${b.bindings.mkString("[", ", ","]")}" at positions ${b.bindingLocations.map(_.codeLocation).mkString("[", ", ","]")} """).mkString(" or ")}"""
//    }
//  }
//
//  private def bindingsToString(bindings: List[ValueSetBinding]): String = {
//    bindings.map(b => {
//      s"""value sets "${b.bindings.mkString("[", ", ","]")} with binding strength "${b.strength}" at positions ${b.bindingLocations.map(_.codeLocation).mkString("[", ", ","]")} """
//    }).mkString(" "," or ", "")
//  }
//
//  private def segmentCharacteristics(selectors: List[CoConstraintCell]): String = {
//    selectors.map(selectorToString).mkString("", ", ","")
//  }
//
//  def createEntry(detection: CoConstraintValidationCode)(implicit Detections : ConfigurableDetections, context: Element, segment: String): List[Entry] = {
//    detection match {
//      case RequiredGroup(group) => Detections.ccRequiredGroup(context, segment, group.name)::Nil
//      case RequiredCoConstraint(coConstraint) => Detections.ccRequiredCoConstraint(context, segment, segmentCharacteristics(coConstraint.selectors))::Nil
//      case CardinalityCoConstraint(coConstraint, found) => Detections.ccCardinalityCoConstraint(context, segment, segmentCharacteristics(coConstraint.selectors), coConstraint.requirement.cardinality, found)::Nil
//      case CardinalityGroup(group, found) => Detections.ccCardinalityGroup(context, segment, group.name, group.requirement.cardinality, found)::Nil
//      case ElementIsNotSegment(path, element) => Detections.ccElementIsNotSegment(element, path, context)::Nil
//      case CoConstraintPlainTextFailure(coConstraint, element, constraint, value) => Detections.ccPlainTextFailure(element, segment, segmentCharacteristics(coConstraint.selectors), constraint.value, value)::Nil
//      case CoConstraintPlainTextSuccess(coConstraint, element, constraint) => Detections.ccPlainTextSuccess(element, segment, segmentCharacteristics(coConstraint.selectors), constraint.value)::Nil
//      case CoConstraintCodeFailureNotFound(coConstraint, element, constraint) => Detections.ccCodeFailure(element, segment, segmentCharacteristics(coConstraint.selectors), constraint.code, constraint.codeSystem, constraint.bindingLocation.map(_.position).mkString("[", ", ","]"), "The code was not found")::Nil
//      case CoConstraintCodeFailureInvalidCs(coConstraint, element, constraint, cs) => Detections.ccCodeFailure(element, segment, segmentCharacteristics(coConstraint.selectors), constraint.code, constraint.codeSystem, constraint.bindingLocation.map(_.position).mkString("[", ", ","]"), s"""The code was found with invalid code system "${cs}" """)::Nil
//      case CoConstraintCodeSuccess(coConstraint, element, constraint) => Detections.ccCodeSuccess(element, segment, segmentCharacteristics(coConstraint.selectors), constraint.code, constraint.codeSystem, constraint.bindingLocation.map(_.position).mkString("[", ", ","]"))::Nil
//      case CoConstraintVsBindingFailure(coConstraint, element, constraint, vsEntries) => Detections.ccVsFailure(element, segment, segmentCharacteristics(coConstraint.selectors), bindingsToString(constraint.bindings))::Nil
//      case CoConstraintVsBindingSuccess(coConstraint, element, constraint, bindingFound, vsEntries) => Detections.ccVsSuccess(element, segment, segmentCharacteristics(coConstraint.selectors), bindingsToString(constraint.bindings))::Nil
//      case InconclusiveCondition(entry) => Nil
//    }
//  }

}
