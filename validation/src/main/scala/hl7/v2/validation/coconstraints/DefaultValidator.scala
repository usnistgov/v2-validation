package hl7.v2.validation.coconstraints

import expression.EvalResult.Trace
import expression.{EvalResult, Evaluator}
import hl7.v2.instance.{Element, EscapeSeqHandler, Group, Location, Message, Segment, Separators, Simple, TimeZone}
import hl7.v2.profile
import hl7.v2.validation.report.ConfigurableDetections
import hl7.v2.instance.Query._
import hl7.v2.validation.vs.{Validator, ValueSetBinding, ValueSetLibrary, VsEntry, VsValidator}

import java.util.{List => JList}
import com.typesafe.config.ConfigFactory
import gov.nist.validation.report.{Entry, Trace => GTrace}
import hl7.v2.validation.FeatureFlags

import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.concurrent._
import ExecutionContext.Implicits.global
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

case class GroupInstance(groupName: String, grouperName: String, groupId: String)

case class RequiredGroup(group: CoConstraintGroup) extends CoConstraintValidationCode
case class RequiredCoConstraint(groupInstance: Option[GroupInstance], coConstraint: CoConstraint) extends CoConstraintValidationCode
case class CardinalityCoConstraint(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, found: Int) extends CoConstraintValidationCode
case class CardinalityGroup(group: CoConstraintGroup, found: Int, grouper: String, values: String) extends CoConstraintValidationCode
case class ElementIsNotSegment(path: String, element: Element) extends CoConstraintValidationCode
case class CoConstraintPlainTextFailure(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, element: Element, constraint: PlainText, value: String) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintPlainTextSuccess(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, element: Element, constraint: PlainText) extends CoConstraintValidationCode with CoConstraintSelectorMatch
case class CoConstraintCodeFailureNotFound(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, element: Simple, constraint: Code) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintCodeFailureInvalidCs(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, element: Element, constraint: Code, cs: String) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintCodeFailureNotFoundCs(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, element: Element, constraint: Code, location: CoConstraintBindingLocation) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintCodeSuccess(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, element: Element, constraint: Code) extends CoConstraintValidationCode with CoConstraintSelectorMatch
case class CoConstraintVsBindingFailure(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, element: Element, constraint: ValueSet, vsEntry: VsEntry, binding: ValueSetBinding) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class CoConstraintVsBindingSuccess(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, element: Element, constraint: ValueSet, bindingFound: ValueSetBinding, vsEntry: VsEntry) extends CoConstraintValidationCode with CoConstraintSelectorMatch
case class InconclusiveCondition(context: Element, condition: String, trace: JList[GTrace]) extends CoConstraintValidationCode
case class InconclusiveAssertion(cell: CoConstraintCell, location: Location, m: String) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class InconclusiveVsBinding(cell: CoConstraintCell, binding: ValueSetBinding, location: Location, vsEntry: VsEntry) extends CoConstraintValidationCode with CoConstraintSelectorNoMatch
case class NotDistinctGrouper(coConstraint: CoConstraint, groupId: CCGroupId) extends CoConstraintValidationCode
case class InconclusiveGrouper(context: Element, path: String, message: String) extends CoConstraintValidationCode
case class InconclusiveTarget(context: Element, path: String, message: String) extends CoConstraintValidationCode
case class CCSegment(segment: Segment, groupId: Option[CCGroupId])
case class CCGroupId(grouper: GroupId, element: Element, value: String)

trait DefaultValidator extends CoConstraintValidator with Evaluator with VsValidator with EscapeSeqHandler {

  private val conf = ConfigFactory.load


  def checkCoConstraint(m: Message)
                       (implicit Detections : ConfigurableDetections, VSValidator : Validator, featureFlags: FeatureFlags): Future[Seq[Entry]] = Future {
    implicit val separators: Separators = m.separators
    implicit val dtz: Option[TimeZone] = m.defaultTimeZone
    implicit val model: profile.Message = m.model
    checkMessage(m)
  }

  /*
  * Evaluate each binding on the message
  */
  def checkMessage(m: Message)
                  (implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator, featureFlags: FeatureFlags): List[Entry]  = {
    val bindings = coConstraintValidationContext.coConstraintBindingsFor(m)
    bindings.foldLeft(List[Entry]()) {
      (acc, binding) => acc ++ checkBindingContext(m.asGroup, binding)
    }
  }

  /*
  * Evaluate a binding on a message
  * Check if context is group or message
  */
  def checkBindingContext(e: Group, context: CoConstraintBindingContext)
                         (implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator, featureFlags: FeatureFlags): List[Entry] = {
    query(e, context.path) match {
      case Failure(exception) => Detections.ccContextSpecError(e, context.path, exception.getMessage) :: Nil
      case Success(elements) => elements.foldLeft(List[Entry]()) {
        (acc, element) => {
          element match {
            case group: Group => acc ++ context.segments.foldLeft(List[Entry]()) {
              (acc, segmentBinding) => acc ++ checkBindingSegment(group, segmentBinding)
            }
            case message: Message => acc ++ context.segments.foldLeft(List[Entry]()) {
              (acc, segmentBinding) => acc ++ checkBindingSegment(message.asGroup, segmentBinding)
            }
            case fail => Detections.ccElementIsNotGroupOrMessage(fail, context.path, e) :: Nil
          }
        }
      }
    }
  }

  /*
  * Evaluate segment binding from context
  * Check if elements are segments
  */
  def checkBindingSegment(group: Group, segment: CoConstraintBindingSegment)
                         (implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator, featureFlags: FeatureFlags): List[Entry] = {
    implicit val segmentName: String = segment.name
    implicit val context: Group = group
    query(group, segment.path) match {
      case Failure(exception) => createEntry(InconclusiveTarget(group, segment.path, exception.getMessage))
      case Success(elements) => {
        val (segments, issues) = sanitizeElementList(segment.path, elements)
        val detections: List[CoConstraintValidationCode] = issues ::: checkCoConstraints(segments, segment.bindings)
        detections.flatMap(createEntry)
      }
    }
  }

  /*
    For each segment in the list, tries to find its group ID value
    If the groupId is not populated then the segment is considered as not having a cc-groupId
    If the groupId is not found and one or more paths failed then returns failures
   */
  def qualifySegments(segments: List[Segment], groupers: List[GroupId]): (List[CCSegment], List[CoConstraintValidationCode]) = {
    val qResults = for(segment <- segments) yield {
      val groupId = findGroupId(segment, groupers, Nil)
      (CCSegment(segment, groupId._1), groupId._2)
    }

    (qResults.map(_._1), qResults.flatMap(_._2))
  }

  def findGroupId(segment: Segment, groupers: List[GroupId], issues: List[CoConstraintValidationCode]): (Option[CCGroupId], List[CoConstraintValidationCode]) = {
    groupers match {
      case Nil => (None, issues)
      case x::ls => getGroupId(segment, x) match {
        case (Some((gId, v, element)), _) => (Some(CCGroupId(gId, element, v)), Nil)
        case (None, issue) => findGroupId(segment, ls, issue.map(_::issues).getOrElse(issues))
      }
    }
  }

  def getGroupId(segment: Segment, grouper: GroupId): (Option[(GroupId, String, Element)], Option[CoConstraintValidationCode]) = {
    queryAsSimple(segment, grouper.path) match {
      case Success(v::Nil) => (Some((grouper, v.value.raw, v)), None)
      case Success(_) => (None, None)
      case Failure(ex) => (None, Some(InconclusiveGrouper(segment, grouper.path, ex.getMessage)))
    }
  }

  /*
    Checks all the co-constraint tables defined on the list of target segments (Conditional and Simple Tables)
   */
  def checkCoConstraints(ls: List[Segment], tables: List[CoConstraintTable])
                        (implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element, featureFlags: FeatureFlags): List[CoConstraintValidationCode] = {
    tables.foldLeft(List[CoConstraintValidationCode]()) {
      (acc, table) =>
        val (segments, groupIdIssues) = qualifySegments(ls, table.grouper)
        table match {
          case simple: SimpleCoConstraintTable => acc ++ checkCoConstraintTable(segments, simple.coConstraints, simple.coConstraintGroups) ++ groupIdIssues
          case conditional: ConditionalCoConstraintTable => acc ++ checkConditionalCoConstraintTable(segments, conditional) ++ groupIdIssues
        }
    }
  }

  /*
    Checks Individual Co-Constraint (distinct cc-groupId), then checks Co-Constraint Group on the remaining unmatched segments
   */
  def checkCoConstraintTable(segments: List[CCSegment], coConstraints: List[CoConstraint], coConstraintGroups: List[CoConstraintGroup])
                            (implicit Detections : ConfigurableDetections, l: ValueSetLibrary, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element, featureFlags: FeatureFlags): List[CoConstraintValidationCode] = {
    val (remainingSegments, entries) = checkCoConstraintList(None, segments, coConstraints, distinctGrouper = true)
    val (_, entryList) = coConstraintGroups.foldLeft((remainingSegments, entries)) {
      (acc, group) => {
        val (segmentsLeft, entries) = checkCoConstraintGroup(acc._1, group)
        (segmentsLeft, acc._2 ++ entries)
      }
    }

    entryList
  }

  /*
    Applies the co-constraint table if the condition passes (evaluated from the GROUP context
   */
  def checkConditionalCoConstraintTable(segments: List[CCSegment], binding: ConditionalCoConstraintTable)
                                       (implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator, context: Element, featureFlags: FeatureFlags): List[CoConstraintValidationCode] = {
    eval(binding.condition.assertion, context) match {
      case EvalResult.Pass => checkCoConstraintTable(segments, binding.coConstraints, binding.coConstraintGroups)
      case EvalResult.Inconclusive(trace) => InconclusiveCondition(context, binding.condition.description, stackTrace(context, trace :: Nil)) :: Nil
      case _ => Nil
    }
  }

  /*
    Checks a List of co-constraint on a list of segments
    Recursive, checks first co-constraint on the list against all segment
    then checks the remaining co-constraint on the remaining segments that didn't match the first co-constraint
 */
  def checkCoConstraintList(groupInstance: Option[GroupInstance], segments: List[CCSegment], coConstraints: List[CoConstraint], distinctGrouper: Boolean)
                           (implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator, featureFlags: FeatureFlags): (List[CCSegment], List[CoConstraintValidationCode]) = {
    coConstraints match {
      case coConstraint :: list =>
        val (remainingSegments, detectionsOfCoConstraint) = checkCoConstraint(groupInstance, segments, coConstraint, distinctGrouper)
        val (leftSegments, detectionsOfRemaining) = checkCoConstraintList(groupInstance, remainingSegments, list, distinctGrouper)
        (leftSegments, detectionsOfCoConstraint ++ detectionsOfRemaining)
      case Nil => (segments, Nil)
    }
  }

  /*
    Checks co-constraint group against a list of segment
    First find all the segments that match the primary co-constraint
    Then group segments based on cc-groupId from matched segments by the primary
    Then validate each group of segments against the CoConstraintGroup co-constraints
   */
  def checkCoConstraintGroup(segments: List[CCSegment], group: CoConstraintGroup)
                            (implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator, featureFlags: FeatureFlags): (List[CCSegment], List[CoConstraintValidationCode]) = {
    val primary = group.primary
    val coConstraints = group.coConstraints

    // Get all primary matches
    val (matchedPrimary, others) = segments.partition(ccs => segmentMatchSelector(ccs.segment, primary))
    // Get All distinct groupIds for matched primaries
    val groupIds = matchedPrimary.filter(_.groupId.isDefined).groupBy(_.groupId.get.value)
    // Get all segments with one of the groupIds (except primaries)
    val (ccGroupSegment, leftOverSegments) = others.partition(s => s.groupId.isDefined && groupIds.keySet.contains(s.groupId.get.value))
    // Group segments by groupId
    val groupSegmentsById = ccGroupSegment.groupBy(_.groupId.get.value)
    // Create group buckets (Primaries, Others)
    val groups = groupIds.map(primaryGroup => {
      (primaryGroup._2.head.groupId.get, (primaryGroup._2.map(_.segment), groupSegmentsById.getOrElse(primaryGroup._1, Nil)))
    })

    // Check group requirements
    val reqIssues = checkGroupRequirement(group, groups.keySet.toList)

    val groupsIssues = groups.foldLeft(List[CoConstraintValidationCode]()) {
      (acc, groupSegments) => {

        val groupInstance = Some(GroupInstance(group.name, groupSegments._1.grouper.name, groupSegments._1.value))
        val groupSegmentPrimaries = groupSegments._2
        val primaryIssues = checkCoConstraintRequirement(groupInstance, primary, groupSegmentPrimaries._1) :::  checkConstraints(groupInstance, groupSegmentPrimaries._1, primary)
        val (_, othersIssues) = checkCoConstraintList(groupInstance, groupSegmentPrimaries._2, coConstraints, distinctGrouper = false)
        acc ++ primaryIssues ++ othersIssues
      }
    }
    (leftOverSegments, reqIssues ++ groupsIssues)
  }

  /*
    Checks a co-constraint against a list of segments
    Find all segment that match the selectors
    Then validate requirement and constraints
    If the cc-groupId should be distinct (case of individual co-constraint) then check that the cc-groupId is unique in the list of segments
   */
  def checkCoConstraint(groupInstance: Option[GroupInstance], segments: List[CCSegment], coConstraint: CoConstraint, distinctGrouper: Boolean)
                       (implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator, featureFlags: FeatureFlags): (List[CCSegment], List[CoConstraintValidationCode]) = {
    val (matches, noMatches) = segments.partition(ccs => segmentMatchSelector(ccs.segment, coConstraint))
    val requirementIssues = checkCoConstraintRequirement(groupInstance, coConstraint, matches.map(_.segment))
    val constraintIssues = checkConstraints(groupInstance, matches.map(_.segment), coConstraint)
    val groupIdIssues = if(distinctGrouper) matches.foldLeft(List[CoConstraintValidationCode]()) {
      (acc, ccSegment) => {
        ccSegment.groupId match {
          case Some(grouper) =>
            val same = findMatchingGroupId(grouper.value, segments)
            if(same.size > 1) NotDistinctGrouper(coConstraint, grouper):: acc else acc
          case None => acc
        }
      }
    } else Nil
    (noMatches, requirementIssues ++ constraintIssues ++ groupIdIssues)
  }

  def findMatchingGroupId(grpId: String, segments: List[CCSegment]): List[CCSegment] = {
    segments.filter(ccs => {
      ccs.groupId.isDefined && ccs.groupId.get.value.equals(grpId)
    })
  }

  /*
    True if a segment match selectors, false otherwise
   */
  def segmentMatchSelector(segment: Segment, cc: CoConstraint)
                          (implicit Detections : ConfigurableDetections, context: Element, s: Separators, t: Option[TimeZone], VSValidator : Validator, featureFlags: FeatureFlags): Boolean = {
    val selectorsEval = cc.selectors.map {
      case plainText: PlainText => validatePlainText(None, segment, plainText, cc)
      case code: Code => validateCode(None, segment, code, cc)
      case valueSet: ValueSet => validateValueSetBinding(None, segment, valueSet, cc)
    }

    !selectorsEval.contains(None) && selectorsEval.flatten.flatten.forall(_._match)
  }

  /*
    True if a detection is a "MATCH" detection (e.g. PlainTextSuccess)
   */
  def isMatch(code: CoConstraintValidationCode with CoConstraintSelector): Boolean = code._match

  def checkCoConstraintRequirement(groupInstance: Option[GroupInstance], coConstraint: CoConstraint, segments: List[Segment]): List[CoConstraintValidationCode] = {
    if (segments.isEmpty && coConstraint.requirement.usage.equals(CoConstraintUsage.R))  RequiredCoConstraint(groupInstance, coConstraint)::Nil
    else if (!coConstraint.requirement.cardinality.includes(segments.size)) CardinalityCoConstraint(groupInstance, coConstraint, segments.size)::Nil
    else Nil
  }

  def checkGroupRequirement(group: CoConstraintGroup, groupers: List[CCGroupId]): List[CoConstraintValidationCode] = {
    if (groupers.isEmpty && group.requirement.usage.equals(CoConstraintUsage.R))  RequiredGroup(group)::Nil
    else if (!group.requirement.cardinality.includes(groupers.size)) CardinalityGroup(group, groupers.size, groupers.head.grouper.name, groupers.map(_.value).sorted.mkString("[", ",", "]"))::Nil
    else Nil
  }

  def checkConstraints(groupInstance: Option[GroupInstance], segments: List[Segment], cc: CoConstraint)
                      (implicit Detections : ConfigurableDetections, context: Element, s: Separators,  t: Option[TimeZone], VSValidator : Validator, featureFlags: FeatureFlags): List[CoConstraintValidationCode] = {
    segments.flatMap {
      segment => cc.constraints.flatMap {
        case plainText: PlainText => validatePlainText(groupInstance, segment, plainText, cc)
        case code: Code => validateCode(groupInstance, segment, code, cc)
        case valueSet: ValueSet => validateValueSetBinding(groupInstance, segment, valueSet, cc)
      }
    }.flatten
  }

  def validatePlainText(groupInstance: Option[GroupInstance], segment: Segment, plainText: PlainText, cc: CoConstraint)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): Option[List[CoConstraintValidationCode with CoConstraintSelector]] =
    queryAsSimple(segment, plainText.path) match {
      case Success(elements) => elements match {
        case Nil => None
        case list =>
          val (matches, nonMatches) = list.partition(!notEqual(_, plainText.value))

          if(matches.nonEmpty) {
            Some(matches.map(element => CoConstraintPlainTextSuccess(groupInstance, cc, element, plainText)))
          } else if(nonMatches.nonEmpty) {
            Some(nonMatches.map(element => CoConstraintPlainTextFailure(groupInstance, cc, element, plainText, element.value.raw)))
          }
          else None
      }
      case Failure(e) => Some(InconclusiveAssertion(plainText, segment.location, e.getMessage)::Nil)
    }


  def validateCode(groupInstance: Option[GroupInstance], segment: Segment, code: Code, cc: CoConstraint)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): Option[List[CoConstraintValidationCode with CoConstraintSelector]] =
    query(segment, code.path) match {
      case Success(elements) =>

        // Detection per binding location
        val detections: List[(CoConstraintBindingLocation, Option[CoConstraintValidationCode with CoConstraintSelector])] = elements.flatMap(repetition => {
          code.bindingLocation.map(bl => {
            (bl, validateCodeBindingLocation(groupInstance, repetition, code, cc, bl))
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

  def validateCodeBindingLocation(groupInstance: Option[GroupInstance], elm: Element, code: Code, cc: CoConstraint, location: CoConstraintBindingLocation)(implicit Detections : ConfigurableDetections, s: Separators,  t: Option[TimeZone], VSValidator : Validator): Option[CoConstraintValidationCode with CoConstraintSelector] = {
    def checkValue(path: String, value: String, typ: String, success: Simple => CoConstraintValidationCode with CoConstraintSelector, fail: Simple => CoConstraintValidationCode with CoConstraintSelector, nil: () => CoConstraintValidationCode with CoConstraintSelector) =
      queryAsSimple(elm, path) match {
      case Success(_code::Nil) =>
        if(!notEqual(_code, value)) {
          success(_code)
        } else {
          fail(_code)
        }
      case Success(Nil) => nil()
      case Success(_) => InconclusiveAssertion(code, elm.location, s"$typ path returned multiple elements")
      case Failure(exception) => InconclusiveAssertion(code, elm.location, exception.getMessage)
    }

    val detection = checkValue(location.code, code.code, "code",
      // Success
      _ => {
        checkValue(location.codeSystem, code.codeSystem, "codeSystem",
          // Success
          _ => CoConstraintCodeSuccess(groupInstance, cc, elm, code),
          // Failure
          elm => CoConstraintCodeFailureInvalidCs(groupInstance, cc, elm, code, elm.value.raw),
          // Not found
          () => CoConstraintCodeFailureNotFoundCs(groupInstance, cc, elm, code, location)
        )
      },
      // Failure
      elm => CoConstraintCodeFailureNotFound(groupInstance, cc, elm, code),
      // Not found
      () => null)

    Option(detection)
  }

  def validateValueSetBinding(groupInstance: Option[GroupInstance], segment: Segment, vs: ValueSet, coConstraint: CoConstraint)
                             (implicit Detections : ConfigurableDetections, featureFlags: FeatureFlags): Option[List[CoConstraintValidationCode with CoConstraintSelector]] = {
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
            Some(codeFound.map(elm => CoConstraintVsBindingSuccess(groupInstance, coConstraint, elm._2.target, vs, elm._1, elm._2)))
          } else if(specError.nonEmpty) {
            Some(specError.map(elm => InconclusiveVsBinding(vs, elm._1, segment.location, elm._2)))
          } else if(notFound.nonEmpty) {
            Some(notFound.map(elm => CoConstraintVsBindingFailure(groupInstance, coConstraint, segment, vs, elm._2, elm._1)))
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
          case _: Exception => (acc._1, ElementIsNotSegment(path, elm) :: acc._2)
        }
    }
  }

  private def stackTrace(context: Element, stack: List[Trace]): JList[GTrace] =
    (stack map { t =>
        val assertion = expression.AsString.expression(t.expression, context)
        val reasons = t.reasons map { r =>
          s"[${r.location.line}, ${r.location.column}] ${r.message}"
        }
        new GTrace(assertion, reasons.asJava)
    }).asJava

  def describeCoConstraint(segment: String, cc: CoConstraint): String = {
    val selectors = cc.selectors.map {
      case PlainText(element, _, value) =>
        String.format(conf.getString("co-constraint.plain-text.selector"), element, value)
      case ValueSet(element, path, bindings) =>
        String.format(conf.getString("co-constraint.value-set.selector"), describeBindingLocations(element, bindings.head.bindingLocations.map(_.codeLocation)), makeList(bindings.head.bindings))
      case Code(element, _, code, codeSystem, bindingLocation) =>
        String.format(conf.getString("co-constraint.code.selector"), describeBindingLocations(element, bindingLocation.map(_.position.toString)), code, codeSystem)
    }.mkString(s" ${conf.getString("co-constraint.connector")} ")
    String.format(conf.getString("co-constraint.description"), segment, selectors)
  }

  def describeConstraint(constraint: CoConstraintCell): String = {
    constraint match {
      case PlainText(element, _, value) =>
        String.format(conf.getString("co-constraint.plain-text.constraint"), element, value)
      case ValueSet(element, path, bindings) =>
        String.format(conf.getString("co-constraint.value-set.constraint"), describeBindingLocations(element, bindings.head.bindingLocations.map(_.codeLocation)), makeList(bindings.head.bindings))
      case Code(element, _, code, codeSystem, bindingLocation) =>
        String.format(conf.getString("co-constraint.code.constraint"), describeBindingLocations(element, bindingLocation.map(_.position.toString)), code, codeSystem)
    }
  }

  def describeVsBinding(element: String, binding: ValueSetBinding): String = {
    String.format(conf.getString("co-constraint.value-set.constraint"), describeBindingLocations(element, binding.bindingLocations.map(_.codeLocation)), makeList(binding.bindings))
  }

  def describeBindingLocations(element: String, locations: List[String]): String = {
    locations.map(location => s"${element}.${location}").mkString(" or ")
  }

  def describeVsBindings(bindings: List[ValueSetBinding]): String = {
    bindings.map(b => {
      String.format(conf.getString("co-constraint.value-set.binding"), makeList(b.bindings), makeList(b.bindingLocations.map(_.codeLocation)))
    }).mkString(s" ${conf.getString("co-constraint.value-set.connector")} ")
  }

  def makeList(values: List[String]): String = values.mkString("[", ", ","]")

  def describeGroupInstance(groupInstance: Option[GroupInstance]): String = {
    groupInstance match {
      case None => ""
      case Some(value) =>
        String.format(conf.getString("co-constraint.group-instance"), value.groupName, value.grouperName, value.groupId)
    }
  }

  def createEntry(detection: CoConstraintValidationCode)(implicit Detections : ConfigurableDetections, context: Element, segment: String): List[Entry] = {
    detection match {
        // Usage
      case RequiredGroup(group) => Detections.ccRequiredGroup(context, group.name, describeCoConstraint(segment, group.primary))::Nil
      case RequiredCoConstraint(groupInstance, coConstraint) => Detections.ccRequiredCoConstraint(
        describeGroupInstance(groupInstance),
        context,
        describeCoConstraint(segment, coConstraint)
      )::Nil
        // Cardinality
      case CardinalityCoConstraint(groupInstance, coConstraint, found) => Detections.ccCardinalityCoConstraint(
        describeGroupInstance(groupInstance),
        context,
        describeCoConstraint(segment, coConstraint),
        coConstraint.requirement.cardinality,
        found
      )::Nil
      case CardinalityGroup(group, found, grouper, values) => Detections.ccCardinalityGroup(context, group.name, group.requirement.cardinality, found, grouper, values)::Nil
        // Group Id
      case NotDistinctGrouper(cc, grouper) => List(Detections.ccNotDistinctGrouper(grouper.element, describeCoConstraint(segment, cc), grouper.value, context.location.uidPath))
      // PlainText
      case CoConstraintPlainTextFailure(groupInstance, coConstraint, element, constraint, value) => Detections.ccPlainTextFailure(
        describeGroupInstance(groupInstance),
        element,
        describeCoConstraint(segment, coConstraint),
        describeConstraint(constraint),
        value
      )::Nil
      case CoConstraintPlainTextSuccess(groupInstance, coConstraint, element, constraint) => Detections.ccCellSuccess(
        describeGroupInstance(groupInstance),
        element,
        describeCoConstraint(segment, coConstraint),
        describeConstraint(constraint)
      )::Nil
        // Code
      case CoConstraintCodeFailureNotFound(groupInstance, coConstraint, element, constraint) => Detections.ccCodeFailure(
        describeGroupInstance(groupInstance),
        element,
        describeCoConstraint(segment, coConstraint),
        describeConstraint(constraint),
        element.value.raw,
        constraint.code
      )::Nil
      case CoConstraintCodeFailureInvalidCs(groupInstance, coConstraint, element, constraint, cs) => Detections.ccCodeFailureInvalidCs(
        describeGroupInstance(groupInstance),
        element,
        describeCoConstraint(segment, coConstraint),
        describeConstraint(constraint),
        cs,
        constraint.codeSystem
      )::Nil
      case CoConstraintCodeFailureNotFoundCs(groupInstance, coConstraint, element, constraint, location) => Detections.ccCodeFailureNotFoundCs(
        describeGroupInstance(groupInstance),
        element,
        describeCoConstraint(segment, coConstraint),
        describeConstraint(constraint),
        s"${constraint.element}.${location.codeSystem}"
      )::Nil
      case CoConstraintCodeSuccess(groupInstance, coConstraint, element, constraint) => Detections.ccCellSuccess(
        describeGroupInstance(groupInstance),
        element,
        describeCoConstraint(segment, coConstraint),
        describeConstraint(constraint)
      )::Nil
        // ValueSet
      case CoConstraintVsBindingFailure(groupInstance, coConstraint, element, constraint, vsEntry, binding) => List(
        Detections.ccVsFailure(
          describeGroupInstance(groupInstance),
          element,
          describeCoConstraint(segment, coConstraint),
          describeVsBinding(constraint.element, binding)
        ),
        createEntry(vsEntry.target, vsEntry.code, vsEntry.strength)
      )
      case CoConstraintVsBindingSuccess(groupInstance, coConstraint, element, constraint, binding, vsEntry) => List(
        Detections.ccCellSuccess(
          describeGroupInstance(groupInstance),
          element,
          describeCoConstraint(segment, coConstraint),
          describeVsBinding(constraint.element, binding)
        ),
        createEntry(vsEntry.target, vsEntry.code, vsEntry.strength)
      )
        // Spec Errors
      case ElementIsNotSegment(path, element) => Detections.ccElementIsNotSegment(element, path, context)::Nil
      case InconclusiveCondition(context, condition, trace) => Detections.ccConditionSpecError(context.location, condition, trace)::Nil
      case InconclusiveAssertion(cell, location, m) => Detections.ccSpecError(location, describeConstraint(cell), m)::Nil
      case InconclusiveVsBinding(cell, _, location, vsEntry) => List(Detections.ccBindingSpecError(location, describeConstraint(cell)),  createEntry(vsEntry.target, vsEntry.code, vsEntry.strength))
      case InconclusiveGrouper(context, path, reason) => List(Detections.ccGrouperSpecError(context, path, reason))
      case InconclusiveTarget(context, path, reason) => List(Detections.ccTargetSpecError(context, path, reason))
    }
  }

  private def notEqual(s: Simple, text: String)(implicit separators: Separators): Boolean = unescape(s.value.raw) != text

}
