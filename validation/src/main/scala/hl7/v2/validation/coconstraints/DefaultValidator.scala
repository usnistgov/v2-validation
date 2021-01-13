package hl7.v2.validation.coconstraints

import expression.EvalResult.Trace
import expression.{EvalResult, Evaluator}
import hl7.v2.instance.{Element, EscapeSeqHandler, Group, Location, Message, Segment, Separators, Simple, TimeZone}
import hl7.v2.profile
import hl7.v2.validation.report.ConfigurableDetections
import hl7.v2.instance.Query._
import hl7.v2.validation.vs.{Validator, ValueSetBinding, ValueSetLibrary, VsEntry, VsValidator}
import java.util.{List => JList}

import com.typesafe.config.{Config, ConfigFactory}
import gov.nist.validation.report.{Entry, Trace => GTrace}
import hl7.v2.validation.coconstraints

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.collection.JavaConverters.seqAsJavaList
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
case class CoConstraintCodeFailureNotFoundCs(coConstraint: CoConstraint, element: Element, constraint: Code) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintCodeSuccess(coConstraint: CoConstraint, element: Element, constraint: Code) extends CoConstraintValidationCode with CoConstraintSelectorMatch

case class CoConstraintVsBindingFailure(coConstraint: CoConstraint, element: Element, constraint: ValueSet, vsEntry: VsEntry) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintVsBindingSuccess(coConstraint: CoConstraint, element: Element, constraint: ValueSet, bindingFound: ValueSetBinding, vsEntry: VsEntry) extends CoConstraintValidationCode with CoConstraintSelectorMatch

case class InconclusiveCondition(context: Element, condition: String, trace: JList[GTrace]) extends CoConstraintValidationCode
case class InconclusiveAssertion(cell: CoConstraintCell, location: Location, m: String) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class InconclusiveVsBinding(cell: CoConstraintCell, binding: ValueSetBinding, location: Location, vsEntry: VsEntry) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch


trait DefaultValidator extends CoConstraintValidator with Evaluator with VsValidator with EscapeSeqHandler {

  private val conf = ConfigFactory.load

  def checkCoConstraint(m: Message)(implicit Detections : ConfigurableDetections, VSValidator : Validator): Future[Seq[Entry]] = Future {
    implicit val separators: Separators = m.separators
    implicit val dtz: Option[TimeZone] = m.defaultTimeZone
    implicit val model: profile.Message = m.model
    checkGroup(m.asGroup)
  }

  def checkGroup(group: Group)(implicit Detections : ConfigurableDetections, s: Separators, t: Option[TimeZone], VSValidator : Validator): List[Entry] = {
    group.children.foldLeft(List[Entry]()) {
      (acc, child) => child match {
        case group: Group => acc ++ checkGroupAndBindings(group, coConstraintValidationContext.coConstraintBindingsFor(group))
        case _ => acc
      }
    }
  }

  def checkGroupAndBindings(group: Group, bindings: List[CoConstraintBinding])(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): List[Entry] = {
    bindings.foldLeft(List[Entry]()) {
      (acc, binding) => acc ++ checkGroupAndBinding(group, binding)
    }
  }

  def checkGroupAndBinding(group: Group, binding: CoConstraintBinding)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): List[Entry] = {
    implicit val segmentName: String = binding.segmentName
    implicit val context: Group = group
    query(group, binding.target) match {
      case Success(elements) =>
        val (segments, issues) = sanitizeElementList(binding.target, elements)
        val detections: List[CoConstraintValidationCode] = issues ::: checkCoConstraints(segments, binding)
        detections.flatMap(createEntry)
      case Failure(exception) => Nil ///TODO Could Not Resolve Segment
    }
  }

  def checkCoConstraints(segments: List[Segment], binding: CoConstraintBinding)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element): List[CoConstraintValidationCode] = {
    binding.bindings.foldLeft(List[CoConstraintValidationCode]()) {
      (acc, table) => table match {
        case simple: SimpleCoConstraintTable => acc ++ checkCoConstraintTable(segments, simple.coConstraints, simple.coConstraintGroups)
        case conditional: ConditionalCoConstraintTable => acc ++ checkConditionalCoConstraintTable(segments, conditional)
      }
    }
  }

  def checkCoConstraintTable(segments: List[Segment], coConstraints: List[CoConstraint], coConstraintGroups: List[CoConstraintGroup])(implicit Detections : ConfigurableDetections, l: ValueSetLibrary, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element): List[CoConstraintValidationCode] = {
    val (remainingSegments, entries) = checkCoConstraintList(segments, coConstraints)
    val (_, entryList) = coConstraintGroups.foldLeft((remainingSegments, entries)) {
      (acc, group) => {
        val (segmentsLeft, entries) = checkCoConstraintGroup(acc._1, group)
        (segmentsLeft, acc._2 ++ entries)
      }
    }

    entryList
  }

  def checkConditionalCoConstraintTable(segments: List[Segment], binding: ConditionalCoConstraintTable)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element): List[CoConstraintValidationCode] = {
    eval(binding.condition.assertion, context) match {
      case EvalResult.Pass => checkCoConstraintTable(segments, binding.coConstraints, binding.coConstraintGroups)
      case EvalResult.Inconclusive(trace) => InconclusiveCondition(context, binding.condition.description, stackTrace(context, trace :: Nil)) :: Nil
      case _ => Nil
    }
  }


  def checkCoConstraintList(segments: List[Segment], coConstraints: List[CoConstraint])(implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator): (List[Segment], List[CoConstraintValidationCode]) = {
    coConstraints match {
      case coConstraint :: list =>
        val (remainingSegments, detectionsOfCoConstraint) = checkCoConstraint(segments, coConstraint)
        val (leftSegments, detectionsOfRemaining) = checkCoConstraintList(remainingSegments, list)
        (leftSegments, detectionsOfCoConstraint ++ detectionsOfRemaining)
      case Nil => (segments, Nil)
    }
  }

  def checkCoConstraintGroup(segments: List[Segment], group: CoConstraintGroup)(implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator): (List[Segment], List[CoConstraintValidationCode]) = {
    val primary = group.primary
    val coConstraints = group.coConstraints

    // Get all primary matches
    val (matchedPrimary, others) = segments.partition(getPartitionMatch(_, primary))

    // Split primary matches into group occurrences
    val groups = if(primary.requirement.cardinality.max.equals("*")) {
      if(matchedPrimary.isEmpty) Nil else List(matchedPrimary)
    } else {
      matchedPrimary.grouped(primary.requirement.cardinality.max.toInt)
    } toList

    // Check group requirements
    val reqIssues = checkGroupRequirement(group, groups.size)

    val (leftOverSegments, groupsIssues) = groups.foldLeft((others, List[CoConstraintValidationCode]())) {
      (acc, primarySegments) => {
        val primaryIssues = checkCoConstraintRequirement(primary, primarySegments) :::  checkConstraints(primarySegments, primary)
        val (remainingSegments, othersIssues) = checkCoConstraintList(acc._1, coConstraints)
        (remainingSegments, primaryIssues ++ othersIssues)
      }
    }
    (leftOverSegments, reqIssues ++ groupsIssues)

  }

  def checkCoConstraint(segments: List[Segment], coConstraint: CoConstraint) (implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator): (List[Segment], List[CoConstraintValidationCode]) = {
    val (matches, noMatches) = segments.partition(getPartitionMatch(_, coConstraint))
    val requirementIssues = checkCoConstraintRequirement(coConstraint, matches)
    val constraintIssues = checkConstraints(matches, coConstraint)
    (noMatches, requirementIssues ++ constraintIssues)
  }

  def getPartitionMatch(segment: Segment, cc: CoConstraint)(implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator): Boolean = {
    val selectorsEval = cc.selectors.map {
      case plainText: PlainText => validatePlainText(segment, plainText, cc)
      case code: Code => validateCode(segment, code, cc)
      case valueSet: ValueSet => validateValueSetBinding(segment, valueSet, cc)
    }

    !selectorsEval.contains(None) && selectorsEval.flatten.flatten.forall(_._match)
  }

  def isMatch(code: CoConstraintValidationCode with CoConstraintSelector): Boolean = code._match

  def checkCoConstraintRequirement(coConstraint: CoConstraint, segments: List[Segment]): List[CoConstraintValidationCode] = {
    if (segments.isEmpty && coConstraint.requirement.usage.equals(CoConstraintUsage.R))  RequiredCoConstraint(coConstraint)::Nil
    else if (!coConstraint.requirement.cardinality.includes(segments.size)) CardinalityCoConstraint(coConstraint, segments.size)::Nil
    else Nil
  }

  def checkGroupRequirement(group: CoConstraintGroup, n: Int): List[CoConstraintValidationCode] = {
    if (n == 0 && group.requirement.usage.equals(CoConstraintUsage.R))  RequiredGroup(group)::Nil
    else if (!group.requirement.cardinality.includes(n)) CardinalityGroup(group, n)::Nil
    else Nil
  }

  def checkConstraints(segments: List[Segment], cc: CoConstraint)(implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator): List[CoConstraintValidationCode] = {
    segments.flatMap {
      segment => cc.constraints.flatMap {
        case plainText: PlainText => validatePlainText(segment, plainText, cc)
        case code: Code => validateCode(segment, code, cc)
        case valueSet: ValueSet => validateValueSetBinding(segment, valueSet, cc)
      }
    }.flatten
  }

  def validatePlainText(segment: Segment, plainText: PlainText, cc: CoConstraint)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): Option[List[CoConstraintValidationCode with CoConstraintSelector]] =
    queryAsSimple(segment, plainText.path) match {
      case Success(elements) => elements match {
        case Nil => None
        case list =>
          val (matches, nonMatches) = list.partition(!notEqual(_, plainText.value))

          if(matches.nonEmpty) {
            Some(matches.map(element => CoConstraintPlainTextSuccess(cc, element, plainText)))
          } else if(nonMatches.nonEmpty) {
            Some(nonMatches.map(element => CoConstraintPlainTextFailure(cc, element, plainText, element.value.raw)))
          }
          else None
      }
      case Failure(e) => Some(InconclusiveAssertion(plainText, segment.location, e.getMessage)::Nil)
    }


  def validateCode(segment: Segment, code: Code, cc: CoConstraint)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): Option[List[CoConstraintValidationCode with CoConstraintSelector]] =
    query(segment, code.path) match {
      case Success(elements) =>

        // Detection per binding location
        val detections: List[(CoConstraintBindingLocation, Option[CoConstraintValidationCode with CoConstraintSelector])] = elements.flatMap(repetition => {
          code.bindingLocation.map(bl => {
            (bl, validateCodeBindingLocation(repetition, code, cc, bl))
          })
        })

        // collect success, notfound, code system errors and inconclusives
        val (success, notfound, cs, notfoundcs, inconclusive) = (
          detections.collect{ case (_, Some(ss: CoConstraintCodeSuccess)) => ss },
          detections.collect{ case (_, Some(ss: CoConstraintCodeFailureNotFound)) => ss },
          detections.collect{ case (_, Some(ss: CoConstraintCodeFailureInvalidCs)) => ss },
          detections.collect{ case (_, Some(ss: CoConstraintCodeFailureNotFoundCs)) => ss },
          detections.collect{ case (_, Some(ss: InconclusiveAssertion)) => ss }
        )

        // If one location has success, then return success, else if one inconclusive then unable to determine if fail otherwise return list of failures
        if(success.nonEmpty) {
          Some(success)
        } else if(inconclusive.nonEmpty) {
          Some(inconclusive)
        } else {
          val errs = cs ::: notfound ::: notfoundcs
          if(errs.nonEmpty) Some(errs) else None
        }
      case Failure(f) => Some(InconclusiveAssertion(code, segment.location, f.getMessage)::Nil)
    }

  def validateCodeBindingLocation(elm: Element, code: Code, cc: CoConstraint, location: CoConstraintBindingLocation)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): Option[CoConstraintValidationCode with CoConstraintSelector] = {
    def checkValue(path: String, value: String, typ: String, success: (Simple) => CoConstraintValidationCode with CoConstraintSelector, fail: (Simple) => CoConstraintValidationCode with CoConstraintSelector, nil: () => CoConstraintValidationCode with CoConstraintSelector) =
      queryAsSimple(elm, path) match {
      case Success(_code::Nil) =>
        if(!notEqual(_code, value)) {
          success(_code)
        } else {
          fail(_code)
        }
      case Success(Nil) => nil()
      case Success(_) => InconclusiveAssertion(code, elm.location, s"${typ} path returned multiple elements")
      case Failure(exception) => InconclusiveAssertion(code, elm.location, exception.getMessage)
    }

    val detection = checkValue(location.code, code.code, "code",
      // Success
      (_) => {
        checkValue(location.codeSystem, code.codeSystem, "codeSystem",
          // Success
          (_) => CoConstraintCodeSuccess(cc, elm, code),
          // Failure
          (elm) => CoConstraintCodeFailureInvalidCs(cc, elm, code, elm.value.raw),
          // Not found
          () => CoConstraintCodeFailureNotFoundCs(cc, elm, code)
        )
      },
      // Failure
      (elm) => CoConstraintCodeFailureNotFound(cc, elm, code),
      // Not found
      () => null)

    Option(detection)
  }

  def validateValueSetBinding(segment: Segment, vs: ValueSet, coConstraint: CoConstraint)(implicit Detections : ConfigurableDetections): Option[List[CoConstraintValidationCode with CoConstraintSelector]] = {
    query(segment, vs.path) match {
      case Success(list) => list match {
        case Nil => None
        case elements =>
          val entries: List[(ValueSetBinding, VsEntry)] = vs.bindings.map(b => (b, check(elements, b))).flatMap(entry => entry._2.map((entry._1, _)))

          // Split bindings where it's found and bindings where it's not
          val (codeFound, others) = entries.partition(_._2.code.codeIsFound)
          // Split bindings where it's not found and it's a spec error and where it's not found
          val (specError, notFound) = others.partition(_._2.code.isSpecError)

          // If found in one or many bindings return success
          // else if there was one or more spec errors return spec errors
          // else if there are not founds return all not founds
          if(codeFound.nonEmpty) {
            Some(codeFound.map(elm => CoConstraintVsBindingSuccess(coConstraint, elm._2.target, vs, elm._1, elm._2)))
          } else if(specError.nonEmpty) {
            Some(specError.map(elm => InconclusiveVsBinding(vs, elm._1, segment.location, elm._2)))
          } else if(notFound.nonEmpty) {
            Some(notFound.map(elm => CoConstraintVsBindingFailure(coConstraint, segment, vs, elm._2)))
          } else {
            None
          }
      }
      case Failure(f) => Some(InconclusiveAssertion(vs, segment.location, f.getMessage)::Nil)
    }
  }

  def sanitizeElementList(path: String, elmList: List[Element])(implicit Detections : ConfigurableDetections): (List[Segment], List[CoConstraintValidationCode]) = {
    elmList.foldLeft((List[Segment](), List[CoConstraintValidationCode]())) {
      (acc, elm) =>
        try {
          (elm.asInstanceOf[Segment] :: acc._1, acc._2)
        } catch {
          case e: Exception => (acc._1, ElementIsNotSegment(path, elm) :: acc._2)
        }
    }
  }

  private def stackTrace(context: Element, stack: List[Trace]): JList[GTrace] =
    seqAsJavaList(
      stack map { t =>
        val assertion = expression.AsString.expression(t.expression, context)
        val reasons = t.reasons map { r =>
          s"[${r.location.line}, ${r.location.column}] ${r.message}"
        }
        new GTrace(assertion, seqAsJavaList(reasons))
    })

  def describeCoConstraint(segment: String, cc: CoConstraint): String = {
    val selectors = cc.selectors.map {
      case PlainText(element, _, value) =>
        String.format(conf.getString("co-constraint.plain-text.selector"), element, value)
      case ValueSet(element, path, bindings) =>
        String.format(conf.getString("co-constraint.value-set.selector"), element, describeVsBindings(bindings))
      case Code(element, _, code, codeSystem, bindingLocation) =>
        String.format(conf.getString("co-constraint.code.selector"), element, code, codeSystem, makeList(bindingLocation.map(_.position.toString)))
    }.mkString(s" ${conf.getString("co-constraint.connector")} ")
    String.format(conf.getString("co-constraint.description"), segment, selectors)
  }

  def describeConstraint(constraint: CoConstraintCell): String = {
    constraint match {
      case PlainText(element, _, value) =>
        String.format(conf.getString("co-constraint.plain-text.constraint"), element, value)
      case ValueSet(element, path, bindings) =>
        String.format(conf.getString("co-constraint.value-set.constraint"), element, describeVsBindings(bindings))
      case Code(element, _, code, codeSystem, bindingLocation) =>
        String.format(conf.getString("co-constraint.code.constraint"), element, code, codeSystem, makeList(bindingLocation.map(_.position.toString)))
    }
  }

  def describeVsBindings(bindings: List[ValueSetBinding]): String = {
    bindings.map(b => {
      String.format(conf.getString("co-constraint.value-set.binding"), makeList(b.bindings), makeList(b.bindingLocations.map(_.codeLocation)))
    }).mkString(s" ${conf.getString("co-constraint.value-set.connector")} ")
  }

  def makeList(values: List[String]): String = values.mkString("[", ", ","]")


  def createEntry(detection: CoConstraintValidationCode)(implicit Detections : ConfigurableDetections, context: Element, segment: String): List[Entry] = {
    detection match {
        // Usage
      case RequiredGroup(group) => Detections.ccRequiredGroup(context, segment, group.name)::Nil
      case RequiredCoConstraint(coConstraint) => Detections.ccRequiredCoConstraint(context, describeCoConstraint(segment, coConstraint))::Nil
        // Cardinality
      case CardinalityCoConstraint(coConstraint, found) => Detections.ccCardinalityCoConstraint(context, describeCoConstraint(segment, coConstraint), coConstraint.requirement.cardinality, found)::Nil
      case CardinalityGroup(group, found) => Detections.ccCardinalityGroup(context, segment, group.name, group.requirement.cardinality, found)::Nil
        // PlainText
      case CoConstraintPlainTextFailure(coConstraint, element, constraint, value) => Detections.ccPlainTextFailure(element, describeCoConstraint(segment, coConstraint), describeConstraint(constraint), value)::Nil
      case CoConstraintPlainTextSuccess(coConstraint, element, constraint) => Detections.ccPlainTextSuccess(element, describeCoConstraint(segment, coConstraint), describeConstraint(constraint))::Nil
        // Code
      case CoConstraintCodeFailureNotFound(coConstraint, element, constraint) => Detections.ccCodeFailure(element, describeCoConstraint(segment, coConstraint), describeConstraint(constraint))::Nil
      case CoConstraintCodeFailureInvalidCs(coConstraint, element, constraint, cs) => Detections.ccCodeFailureInvalidCs(element, describeCoConstraint(segment, coConstraint), describeConstraint(constraint), cs)::Nil
      case CoConstraintCodeFailureNotFoundCs(coConstraint, element, constraint) => Detections.ccCodeFailureNotFoundCs(element, describeCoConstraint(segment, coConstraint), describeConstraint(constraint) )::Nil
      case CoConstraintCodeSuccess(coConstraint, element, constraint) => Detections.ccCodeSuccess(element, describeCoConstraint(segment, coConstraint), describeConstraint(constraint))::Nil
        // ValueSet
      case CoConstraintVsBindingFailure(coConstraint, element, constraint, vsEntry) => List(Detections.ccVsFailure(element, describeCoConstraint(segment, coConstraint), describeConstraint(constraint)), createEntry(vsEntry.target, vsEntry.code, vsEntry.strength))
      case CoConstraintVsBindingSuccess(coConstraint, element, constraint, _, vsEntry) => List(Detections.ccVsSuccess(element,describeCoConstraint(segment, coConstraint), describeConstraint(constraint)), createEntry(vsEntry.target, vsEntry.code, vsEntry.strength))
        // Spec Errors
      case ElementIsNotSegment(path, element) => Detections.ccElementIsNotSegment(element, path, context)::Nil
      case InconclusiveCondition(context, condition, trace) => Detections.ccConditionSpecError(context.location, condition, trace)::Nil
      case InconclusiveAssertion(cell, location, m) => Detections.ccSpecError(location, describeConstraint(cell), m)::Nil
      case InconclusiveVsBinding(cell, _, location, vsEntry) => List(Detections.ccBindingSpecError(location, describeConstraint(cell)),  createEntry(vsEntry.target, vsEntry.code, vsEntry.strength))
    }
  }

  private def notEqual(s: Simple, text: String)(implicit separators: Separators): Boolean = unescape(s.value.raw) != text

}
