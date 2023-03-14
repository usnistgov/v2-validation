package hl7.v2.validation.content

import java.io.InputStream
import java.util.{List => JList}
import expression.Expression
import gov.nist.hit.hl7.v2.schemas.utils.{HL7v2Schema, HL7v2SchemaResourceResolver}
import hl7.v2.instance._
import hl7.v2.profile.{Datatype, Group => GM, Segment => SM}
import nist.xml.util.{ClassPathResourceResolver, XOMDocumentBuilder}

import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.{Failure, Success, Try}

class VMap[T](
    val groupByID: Map[String, List[T]],
    val groupByName: Map[String, List[T]],
    val segmentByID: Map[String, List[T]],
    val segmentByName: Map[String, List[T]],
    val datatypeByID: Map[String, List[T]],
    val datatypeByName: Map[String, List[T]],
    val messageByID: Map[String, List[T]],
    val messageByName: Map[String, List[T]]) {

  def merge(map: VMap[T]) = new VMap[T](
    VMap.merge[T](groupByID, map.groupByID),
    VMap.merge[T](groupByName, map.groupByName),
    VMap.merge[T](segmentByID, map.segmentByID),
    VMap.merge[T](segmentByName, map.segmentByName),
    VMap.merge[T](datatypeByID, map.datatypeByID),
    VMap.merge[T](datatypeByName, map.datatypeByName),
    VMap.merge[T](messageByID, map.messageByID),
    VMap.merge[T](messageByName, map.messageByName))
}

object VMap {

  def empty[T] = new VMap[T](
    Map[String, List[T]](),
    Map[String, List[T]](),
    Map[String, List[T]](),
    Map[String, List[T]](),
    Map[String, List[T]](),
    Map[String, List[T]](),
    Map[String, List[T]](),
    Map[String, List[T]]())

  def merge[T](map1: Map[String, List[T]], map2: Map[String, List[T]]) = {
    map1.foldLeft(map2) { (acc, x) =>
      acc.get(x._1) match {
        case Some(xs) => acc + (x._1 -> (xs ++ x._2))
        case None => acc + x
      }
    }
  }
}

class DefaultConformanceContext(
    val constraints: VMap[Constraint],
    val predicates: VMap[Predicate],
    val orderIndifferent: List[Context],
    val coConstraints: VMap[CoConstraint]) extends ConformanceContext {
  /**
   * Returns the list of constraints defined for the specified element.
   */
  def constraintsFor(e: Element): List[Constraint] = e match {
    case c: Component => datatypeSpecs(c.datatype, constraints)
    case f: Field => datatypeSpecs(f.datatype, constraints)
    case s: Segment => segmentSpecs(s.model.ref, constraints)
    case g: Group => groupSpecs(g.model, constraints)
  }

  /**
   * Returns the list of predicates defined for the specified element.
   */
  def predicatesFor(e: Element): List[Predicate] = e match {
    case c: Component => datatypeSpecs(c.datatype, predicates)
    case f: Field => datatypeSpecs(f.datatype, predicates)
    case s: Segment => segmentSpecs(s.model.ref, predicates)
    case g: Group => groupSpecs(g.model, predicates)
  }

  /**
   * Returns the list of coConstraints defined for the specified element.
   */
  def coConstraintsF(): VMap[CoConstraint] = coConstraints
  
  def coConstraintsFor(e: Element): List[CoConstraint] = e match {
    case c: Component => datatypeSpecs(c.datatype, coConstraints)
    case f: Field => datatypeSpecs(f.datatype, coConstraints)
    case s: Segment => segmentSpecs(s.model.ref, coConstraints)
    case g: Group => groupSpecs(g.model, coConstraints)
  }

  def orderIndifferentConstraints(): List[Context] = orderIndifferent

  private def datatypeSpecs[T](d: Datatype, map: VMap[T]): List[T] =
    map.datatypeByName.getOrElse(d.name, Nil) :::
      map.datatypeByID.getOrElse(d.id, Nil)

  private def segmentSpecs[T](s: SM, map: VMap[T]): List[T] =
    map.segmentByName.getOrElse(s.name, Nil) :::
      map.segmentByID.getOrElse(s.id, Nil)

  private def groupSpecs[T](g: GM, map: VMap[T]): List[T] =
    map.groupByName.getOrElse(g.name, Nil) :::
      map.groupByID.getOrElse(g.id, Nil) :::
      map.messageByName.getOrElse(g.name, Nil) :::
      map.messageByID.getOrElse(g.id, Nil)

}

/**
 * Companion object
 */
object DefaultConformanceContext {

  import nist.xml.util.XOMExtensions._

  /**
   * The resource resolver
   */
  private val resourceResolver = Some(new HL7v2SchemaResourceResolver())

  def apply(contexts: JList[InputStream]): Try[ConformanceContext] = {
    val params = contexts.asScala.toList
    apply(params: _*)
  }

  /**
   * Build the conformance context from a list of XML files.
   * An empty conformance context will be created if the list is empty.
   * @param contexts - The list of XML files
   * @return A success containing the constraint manager or a failure
   */
  def apply(contexts: InputStream*): Try[ConformanceContext] =
    try {
      val z = (VMap.empty[Constraint], VMap.empty[Predicate], List[Context](), VMap.empty[CoConstraint])
      val l = contexts map { x => vMaps(x).get }
      val r = l.foldLeft(z) { (acc, x) =>
        (acc._1 merge x._1, acc._2 merge x._2, acc._3 ++ x._3, acc._4 merge x._4)
      }
      Success(new DefaultConformanceContext(r._1, r._2, r._3, r._4))
    } catch { case e: Throwable => Failure(e) }

  private def vMaps(confContext: InputStream): Try[(VMap[Constraint], VMap[Predicate], List[Context], VMap[CoConstraint])] =
    XOMDocumentBuilder.build(confContext, HL7v2Schema.getConformanceContext, resourceResolver) map { doc =>
      (constraints(doc), predicates(doc), orderIndifferent(doc), coConstraints(doc))
    }

  private def orderIndifferent(doc: nu.xom.Document): List[Context] = {
    val e = doc.getRootElement.getFirstChildElement("OrderIndifferent")
    if (e != null) {
      val child = e.getChildElements
      if (child != null) (e.getChildElements map context).toList
      else Nil
    } else Nil
  }

  private def pattern(e: nu.xom.Element): Pattern = {
    val t = trigger(e.getFirstChildElement("Trigger"))
    val cstr = e.getFirstChildElement("Constraints")
    val constraints = if (cstr != null) (cstr.getChildElements map constraint).toList else Nil
    val ctx = e.getFirstChildElement("Contexts")
    val contexts = if (ctx != null) (ctx.getChildElements map context).toList else Nil
    val nb = if (e.attribute("Cardinality") != "") e.attribute("Cardinality").toInt else 1
    Pattern(t, constraints, contexts, nb)
  }

  private def context(e: nu.xom.Element): Context = {
    val path = e.attribute("List")
    val patterns = (e.getChildElements map pattern).toList
    Context(path, patterns)
  }

  private def trigger(e: nu.xom.Element): Trigger = {
    val message = e.getFirstChildElement("ErrorMessage").getValue
    val assert = assertion(e)
    Trigger(message, assert)
  }

  private def coConstraints(doc: nu.xom.Document): VMap[CoConstraint] = {
    val e = doc.getRootElement.getFirstChildElement("CoConstraints") //FIXME Can throw is missing
    val (gByID, gByName) = init(extractNodes(e, "Group"), coconstraint)
    val (sByID, sByName) = init(extractNodes(e, "Segment"), coconstraint)
    val (dtByID, dtByName) = init(extractNodes(e, "Datatype"), coconstraint)
    val (msByID, msByName) = init(extractNodes(e, "Message"), coconstraint)
    new VMap(gByID, gByName, sByID, sByName, dtByID, dtByName, msByID, msByName)
  }

  private def constraints(doc: nu.xom.Document): VMap[Constraint] = {
    val e = doc.getRootElement.getFirstChildElement("Constraints") //FIXME Can throw is missing
    val (gByID, gByName) = init(extractNodes(e, "Group"), constraint)
    val (sByID, sByName) = init(extractNodes(e, "Segment"), constraint)
    val (dtByID, dtByName) = init(extractNodes(e, "Datatype"), constraint)
    val (msByID, msByName) = init(extractNodes(e, "Message"), constraint)
    new VMap(gByID, gByName, sByID, sByName, dtByID, dtByName, msByID, msByName)
  }

  private def predicates(doc: nu.xom.Document): VMap[Predicate] = {
    val e = doc.getRootElement.getFirstChildElement("Predicates") //FIXME Can throw is missing
    val (gByID, gByName) = init(extractNodes(e, "Group"), predicate)
    val (sByID, sByName) = init(extractNodes(e, "Segment"), predicate)
    val (dtByID, dtByName) = init(extractNodes(e, "Datatype"), predicate)
    val (msByID, msByName) = init(extractNodes(e, "Message"), predicate)
    new VMap(gByID, gByName, sByID, sByName, dtByID, dtByName, msByID, msByName)
  }

  /**
   * Initialize the map for a specific type (Datatype, Segment or Group)
   * @param es - The nu.xom.Elements
   * @return The constraint maps  for a specific type
   */
  private def init[T](es: nu.xom.Elements, f: nu.xom.Element => T) = {
    val i = (Map[String, List[T]](), Map[String, List[T]]())
    if (es == null)
      i
    else
      es.foldLeft(i) { (acc, e) =>
        if ("ByID" == e.getLocalName)
          (acc._1 + byIDEntries(e, f), acc._2)
        else
          (acc._1, acc._2 + byNameEntries(e, f))
      }
  }

  /**
   * Returns a tuple containing an id and the list of constraints/predicates
   */
  private def byIDEntries[T](e: nu.xom.Element, f: nu.xom.Element => T) =
    (e.attribute("ID"), (e.getChildElements map f).toList)

  /**
   * Returns a tuple containing a name and the list of constraints/predicates
   */
  private def byNameEntries[T](e: nu.xom.Element, f: nu.xom.Element => T) =
    (e.attribute("Name"), (e.getChildElements map f).toList)

  /**
   * Creates a constraint from a nu.xom.Element
   */
  private def constraint(e: nu.xom.Element): Constraint = {
    val id = e.attribute("ID") // match { case "" => None case x => Some(x) }
    val ref = reference(e.getFirstChildElement("Reference"))
    val desc = description(e.getFirstChildElement("Description"))
    val clas = classification(e.attribute("Classification"))
    val st = strength(e.attribute("Strength"))
    Constraint(id, ref, clas, st, desc, assertion(e))
  }
  /**
   * Creates a constraint from a nu.xom.Element
   */
  private def coconstraint(e: nu.xom.Element): CoConstraint = {
    val desc = description(e.getFirstChildElement("Description"))
    val comments = e.attribute("Comments")
    CoConstraint(desc, comments, plainCoConstraints(e.getFirstChildElement("Assertion")))
  }

  /**
   * Creates a predicate from a nu.xom.Element
   */
  private def predicate(e: nu.xom.Element): Predicate = {
    val target = e.attribute("Target")
    val tusage = usage(e.attribute("TrueUsage"))
    val fusage = usage(e.attribute("FalseUsage"))
    val ref = reference(e.getFirstChildElement("Reference"))
    val desc = e.getFirstChildElement("Description").getValue
    Predicate(target, tusage, fusage, ref, desc, condition(e))
  }

  private def description(e: nu.xom.Element): String =
    if (e != null) e.getValue else "Description is missing ... "

  private def classification(e: String): Option[Classification] =
    e match {
      case "W" => Some(Classification.W())
      case "A" => Some(Classification.A())
      case _ => None
    }

  private def strength(e: String): Option[ConstraintStrength] =
    e match {
      case "SHALL" => Some(ConstraintStrength.SHALL())
      case "SHOULD" => Some(ConstraintStrength.SHOULD())
      case _ => None
    }

  private def reference(e: nu.xom.Element): Option[Reference] =
    if (e == null) None
    else {
      val chapter = e.attribute("Chapter")
      val section = e.attribute("Section")
      val page = e.attribute("Page")
      val url = e.attribute("URL")
      val source = e.attribute("Source")
      val genBy = e.attribute("GeneratedBy")
      val refPath = e.attribute("ReferencePath")
      val dataCat = e.attribute("TestDataCategorization")
      Some(Reference(chapter, section, page, url, source, genBy, refPath, dataCat))
    }

  /**
   * Create an expression from a nu.xom.Element representing an Assertion
   */
  private def assertion(e: nu.xom.Element): Expression = {
    val assertionNode = e.getFirstChildElement("Assertion")
    val expressionNode = assertionNode.getChildElements.get(0)
    expression.XMLDeserializer.expression(expressionNode)
  }

  private def plainCoConstraints(e: nu.xom.Element): List[PlainCoConstraint] = {
    e.getChildElements("PlainCoConstraint").foldLeft(List[PlainCoConstraint]()){
      (acc, x) => plainCoConstraint(x) :: acc 
    }
  }

  private def plainCoConstraint(e: nu.xom.Element): PlainCoConstraint = {
    val key = expression.XMLDeserializer.expression(e)
    val constraints = e.getChildElements.foldLeft(List[Expression]()) { (acc, x) =>
      expression.XMLDeserializer.expression(x) :: acc
    }
    PlainCoConstraint(key, constraints)
  }

  /**
   * Create an expression from a nu.xom.Element representing a Condition
   */
  private def condition(e: nu.xom.Element): Expression = {
    val assertionNode = e.getFirstChildElement("Condition")
    val expressionNode = assertionNode.getChildElements.get(0)
    expression.XMLDeserializer.expression(expressionNode)
  }

  private def usage(s: String): PredicateUsage = s match {
    case "R" => PredicateUsage.R
    case "RE" => PredicateUsage.RE
    case "X" => PredicateUsage.X
    case "O" => PredicateUsage.O
    case x => throw new Error(s"Invalid predicate usage '$x'")
  }

  private def extractNodes(e: nu.xom.Element, name: String) =
    if (e == null) null
    else {
      val ee = e.getFirstChildElement(name)
      if (ee != null) e.getFirstChildElement(name).getChildElements else null
    }
}