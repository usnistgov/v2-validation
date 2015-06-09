package hl7.v2.validation.content

import java.io.InputStream

import expression.Expression
import hl7.v2.instance._
import hl7.v2.profile.{Group => GM, Segment => SM, Datatype}
import nist.xml.util.{XOMDocumentBuilder, ClassPathResourceResolver}

import scala.util.Try

class VMap[T] (
    val groupByID:      Map[String, List[T]],
    val groupByName:    Map[String, List[T]],
    val segmentByID:    Map[String, List[T]],
    val segmentByName:  Map[String, List[T]],
    val datatypeByID:   Map[String, List[T]],
    val datatypeByName: Map[String, List[T]]
)

class DefaultConformanceContext(
    val constraints: VMap[Constraint],
    val predicates: VMap[Predicate]
  ) extends ConformanceContext {

  /**
    * Returns the list of constraints defined for the specified element.
    */
  def constraintsFor(e: Element): List[Constraint] = e match {
    case c: Component => datatypeSpecs( c.datatype, constraints )
    case f: Field     => datatypeSpecs( f.datatype, constraints )
    case s: Segment   => segmentSpecs( s.model.ref, constraints )
    case g: Group     => groupSpecs( g.model, constraints )
  }

  /**
    * Returns the list of predicates defined for the specified element.
    */
  def predicatesFor(e: Element): List[Predicate] = e match {
    case c: Component => datatypeSpecs( c.datatype, predicates )
    case f: Field     => datatypeSpecs( f.datatype, predicates )
    case s: Segment   => segmentSpecs( s.model.ref, predicates )
    case g: Group     => groupSpecs( g.model, predicates )
  }

  private def datatypeSpecs[T](d: Datatype, map: VMap[T]): List[T] =
    map.datatypeByName.getOrElse( d.name, Nil) :::
    map.datatypeByID.getOrElse( d.id, Nil)

  private def segmentSpecs[T](s: SM, map: VMap[T]): List[T] =
    map.segmentByName.getOrElse( s.name, Nil) :::
    map.segmentByID.getOrElse( s.id, Nil)

  private def groupSpecs[T](g: GM, map: VMap[T]): List[T] =
    map.groupByName.getOrElse( g.name, Nil ) :::
    map.groupByID.getOrElse( g.id, Nil)
}

/**
  * Companion object
  */
object DefaultConformanceContext {

  import nist.xml.util.XOMExtensions._

  /**
    * The resource resolver
    */
  private val resourceResolver = Some( new ClassPathResourceResolver("/rules") )

  /**
    * The XSD schema
    */
  private def xsd = getClass.getResourceAsStream("/rules/ConformanceContext.xsd")

  /**
    * Build a constraint manager from the conformance context XML files
    * @param confContext - The conformance context XML file
    * @return A success containing the constraint manager or a failure
    */
  def apply( confContext: InputStream ): Try[ConformanceContext] =
    XOMDocumentBuilder.build( confContext, xsd, resourceResolver ) map { doc =>
      new DefaultConformanceContext( constraints(doc), predicates(doc) )
    }

  private def constraints(doc: nu.xom.Document): VMap[Constraint] = {
    val e = doc.getRootElement.getFirstChildElement("Constraints") //FIXME Can throw is missing
    val (gByID, gByName)   = init( extractNodes(e, "Group"), constraint )
    val (sByID, sByName)   = init( extractNodes(e, "Segment"), constraint )
    val (dtByID, dtByName) = init( extractNodes(e, "Datatype"), constraint )
    new VMap(gByID, gByName, sByID, sByName, dtByID, dtByName)
  }

  private def predicates(doc: nu.xom.Document): VMap[Predicate] = {
    val e = doc.getRootElement.getFirstChildElement("Predicates") //FIXME Can throw is missing
    val (gByID, gByName)   = init( extractNodes(e, "Group"), predicate )
    val (sByID, sByName)   = init( extractNodes(e, "Segment"), predicate )
    val (dtByID, dtByName) = init( extractNodes(e, "Datatype"), predicate )
    new VMap(gByID, gByName, sByID, sByName, dtByID, dtByName)
  }

  /**
    * Initialize the map for a specific type (Datatype, Segment or Group)
    * @param es - The nu.xom.Elements
    * @return The constraint maps  for a specific type
    */
  private def init[T]( es: nu.xom.Elements, f: nu.xom.Element => T) = {
    val i = (Map[String, List[T]](), Map[String, List[T]]())
    if( es == null)
      i
    else
      es.foldLeft( i ) { (acc, e) =>
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
    (e.attribute("ID"), ( e.getChildElements map f).toList )

  /**
    * Returns a tuple containing a name and the list of constraints/predicates
    */
  private def byNameEntries[T](e: nu.xom.Element, f: nu.xom.Element => T) =
    (e.attribute("Name"), ( e.getChildElements map f).toList  )

  /**
    * Creates a constraint from a nu.xom.Element
    */
  private def constraint( e: nu.xom.Element ): Constraint = {
    val id   = e.attribute("ID")// match { case "" => None case x => Some(x) }
    val ref  = reference( e.getFirstChildElement("Reference") )
    val desc = description( e.getFirstChildElement("Description") )
    Constraint(id, ref, desc, assertion(e) )
  }

  /**
    * Creates a predicate from a nu.xom.Element
    */
  private def predicate( e: nu.xom.Element ): Predicate = {
    val target = e.attribute("Target")
    val tusage = usage( e.attribute("TrueUsage") )
    val fusage = usage( e.attribute("FalseUsage") )
    val ref    = reference( e.getFirstChildElement("Reference") )
    val desc   = e.getFirstChildElement("Description").getValue
    Predicate(target, tusage, fusage, ref, desc, condition(e))
  }

  private def description(e: nu.xom.Element): String =
    if( e != null ) e.getValue else "Description is missing ... "

  private def reference(e: nu.xom.Element): Option[Reference] =
    if( e == null ) None
    else {
      val chapter = e.attribute("Chapter")
      val section = e.attribute("Section")
      val page    = e.attribute("Page")
      val url     = e.attribute("URL")
      val source  = e.attribute("Source")
      val genBy   = e.attribute("GeneratedBy")
      val refPath = e.attribute("ReferencePath")
      val dataCat = e.attribute("TestDataCategorization")
      Some(Reference(chapter, section, page, url, source, genBy,refPath,dataCat))
    }

  /**
    * Create an expression from a nu.xom.Element representing an Assertion
    */
  private def assertion( e: nu.xom.Element ): Expression = {
    val assertionNode  = e.getFirstChildElement("Assertion")
    val expressionNode = assertionNode.getChildElements.get(0)
    expression.XMLDeserializer.expression( expressionNode )
  }

  /**
    * Create an expression from a nu.xom.Element representing a Condition
    */
  private def condition(e: nu.xom.Element): Expression = {
    val assertionNode  = e.getFirstChildElement("Condition")
    val expressionNode = assertionNode.getChildElements.get(0)
    expression.XMLDeserializer.expression( expressionNode )
  }

  private def usage(s: String): PredicateUsage = s match {
    case "R"  => PredicateUsage.R
    case "RE" => PredicateUsage.RE
    case "X"  => PredicateUsage.X
    case "O"  => PredicateUsage.O
    case x    => throw new Error(s"Invalid predicate usage '$x'")
  }

  private def extractNodes( e: nu.xom.Element, name: String ) =
    if( e == null) null
    else {
      val ee = e.getFirstChildElement(name)
      if( ee != null) e.getFirstChildElement(name).getChildElements else null
    }
}