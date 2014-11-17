package hl7.v2.validation.content

import java.io.InputStream

import expression.Expression
import hl7.v2.instance._
import hl7.v2.profile.{Datatype, Group => GM, Segment => SM}
import nist.xml.util.{ClassPathResourceResolver, XOMDocumentBuilder}

import scala.util.Try

class DefaultConstraintManager (
    val groupByID:      Map[String, List[Constraint]],
    val groupByName:    Map[String, List[Constraint]],
    val segmentByID:    Map[String, List[Constraint]],
    val segmentByName:  Map[String, List[Constraint]],
    val datatypeByID:   Map[String, List[Constraint]],
    val datatypeByName: Map[String, List[Constraint]]
  ) extends ConstraintManager {

  /**
    * Returns the list of constraints defined for
    * the specified element.
    */
  def constraintsFor(e: Element): List[Constraint] = e match {
    case c: Component => datatypeConstraints( c.model.datatype )
    case f: Field     => datatypeConstraints( f.model.datatype )
    case s: Segment   => segmentConstraints( s.model.ref )
    case g: Group     => groupConstraints( g.model )
  }

  private def datatypeConstraints(d: Datatype): List[Constraint] =
    datatypeByName.getOrElse( d.id, Nil) ::: datatypeByID.getOrElse( d.name, Nil)

  private def segmentConstraints( s: SM ): List[Constraint] =
    segmentByName.getOrElse( s.id, Nil) ::: segmentByID.getOrElse( s.name, Nil)

  private def groupConstraints( g: GM ): List[Constraint] =
    groupByName.getOrElse( g.name, Nil )
}

/**
  * Companion object
  */
object DefaultConstraintManager {

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
  def apply( confContext: InputStream ): Try[DefaultConstraintManager] =
    XOMDocumentBuilder.build( confContext, xsd, resourceResolver ) map { doc =>
      val (gByID, gByName) = init( extractNodes(doc, "Group") )
      val (sByID, sByName) = init( extractNodes(doc, "Segment") )
      val (dtByID, dtByName)   = init( extractNodes(doc, "Datatype") )
      new DefaultConstraintManager(gByID, gByName, sByID, sByName, dtByID, dtByName)
    }

  /**
    * Initialize the map for a specific type (Datatype, Segment or Group)
    * @param es - The nu.xom.Elements
    * @return The constraint maps  for a specific type
    */
  private def init( es: nu.xom.Elements ) = {
    val i = (Map[String, List[Constraint]](), Map[String, List[Constraint]]())
    es.foldLeft( i ) { (acc, e) =>
      if ("ByID" == e.getLocalName)
        (acc._1 + byIDEntries(e), acc._2)
      else
        (acc._1, acc._2 + byNameEntries(e))
    }
  }

  /**
    * Creates the list of constraint for the specified
    * id from a nu.xom.Element
    * @param e - The nu.xom.Element
    * @return A tuple containing the id and the list of constraints
    */
  private def byIDEntries(e: nu.xom.Element): (String, List[Constraint]) =
    (e.attribute("ID"), constraints(e))

  /**
    * Creates the list of constraint for the specified
    * name from a nu.xom.Element
    * @param e - The nu.xom.Element
    * @return A tuple containing the name and the list of constraints
    */
  private def byNameEntries(e: nu.xom.Element): (String, List[Constraint]) =
    (e.attribute("Name"), constraints(e))

  /**
    * Creates a list of constraints from a nu.xom.Element
    * @param e - The nu.xom.Element
    * @return A list of constraints
    */
  private def constraints(e: nu.xom.Element): List[Constraint] =
    (e.getChildElements("Constraint") map constraint).toList

  /**
    * Creates a constraint from a nu.xom.Element
    * @param e - The nu.xom.Element
    * @return An instance of constraint
    */
  private def constraint( e: nu.xom.Element ): Constraint = {
    val id          = e.attribute("ID")
    val reference   = None //FIXME
    val description = e.getFirstChildElement("Description").getValue
    Constraint(id, constraintTag(e), reference, description, assertion(e) )
  }

  /**
    * Creates a constraint tag from a nu.xom.Element
    * @param e - The nu.xom.Element
    * @return An instance of constraint tag
    */
  private def constraintTag( e: nu.xom.Element ): ConstraintTag =
    e.attribute("Tag") match {
      case ""              => ConstraintTag.ConfStmt
      case "ConfStatement" => ConstraintTag.ConfStmt
      case "Predicate"     => ConstraintTag.Predicate
      case x => throw new Error(s"[Error] Unknown constraint tag '$x'")
    }

  /**
    * Create an expression object from a nu.xom.Element.
    * @param e - The no.xom.Element representing the assertion
    * @return An instance of an expression
    */
  private def assertion( e: nu.xom.Element ): Expression = {
    val assertionNode  = e.getFirstChildElement("Assertion")
    val expressionNode = assertionNode.getChildElements.get(0)
    expression.XMLDeserializer.expression( expressionNode )
  }

  private def extractNodes( doc: nu.xom.Document, name: String ) =
    doc.getRootElement.getFirstChildElement( name ).getChildElements

}

/*object MainCM extends App {

  val r = getClass.getResourceAsStream("/rules/ConfContextSample.xml")

  DefaultConstraintManager(r) match {
    case scala.util.Success(cm) =>
      println( cm.datatypeByID )
      println( cm.datatypeByName )
    case scala.util.Failure(e)  => e.printStackTrace()
  }
}
*/