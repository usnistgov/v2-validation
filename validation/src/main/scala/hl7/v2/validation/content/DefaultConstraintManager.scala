package hl7.v2.validation.content

import hl7.v2.instance.Group
import hl7.v2.instance.Segment
import hl7.v2.instance.Field
import hl7.v2.instance.Component
import nist.xml.util.ClassPathResourceResolver
import java.io.InputStream
import scala.util.Try
import nist.xml.util.XOMDocumentBuilder
import expression.Expression

class DefaultConstraintManager (
    val groupByID:      Map[String, List[Constraint]],
    val groupByName:    Map[String, List[Constraint]],
    val segmentByID:    Map[String, List[Constraint]],
    val segmentByName:  Map[String, List[Constraint]],
    val datatypeByID:   Map[String, List[Constraint]],
    val datatypeByName: Map[String, List[Constraint]]
  ) extends ConstraintManager {

  def constraintsFor( g: Group ): List[Constraint] = 
    groupByName.getOrElse( g.model.name, Nil )

  def constraintsFor( s: Segment ): List[Constraint] = 
    segmentByName.getOrElse( s.model.ref.name, Nil) ::: 
    segmentByID.getOrElse( s.model.ref.id, Nil)

  def constraintsFor( f: Field ): List[Constraint] = 
    datatypeByName.getOrElse( f.model.datatype.name, Nil) ::: 
    datatypeByID.getOrElse( f.model.datatype.id, Nil)

  def constraintsFor( c: Component ): List[Constraint] = 
    datatypeByName.getOrElse( c.model.datatype.name, Nil) ::: 
    datatypeByID.getOrElse( c.model.datatype.id, Nil)
}


object DefaultConstraintManager {

  import nist.xml.util.XOMExtensions._

  private val resourceResolver = Some( new ClassPathResourceResolver("/rules") )

  private def xsd = getClass.getResourceAsStream("/rules/ConformanceContext.xsd")

  /**
    * Build a constraint manager from the conformance context XML files
    * @param confContext - The conformance context XML file
    * @return A success containing the constraint manager or a failure
    */
  def apply( confContext: InputStream ): Try[DefaultConstraintManager] = 
    XOMDocumentBuilder.build( confContext, xsd, resourceResolver ) map { doc =>
      val (groupByID, groupByName)       = init( doc.getRootElement.getFirstChildElement("Group").getChildElements )
      val (segmentByID, segmentByName)   = init( doc.getRootElement.getFirstChildElement("Segment").getChildElements )
      val (datatypeByID, datatypeByName) = init( doc.getRootElement.getFirstChildElement("Datatype").getChildElements )
      new DefaultConstraintManager(groupByID, groupByName, segmentByID, segmentByName, datatypeByID, datatypeByName)
    }

  private def init( es: nu.xom.Elements ) = 
    es.foldLeft( (Map[String, List[Constraint]](), Map[String, List[Constraint]]()) ) { (acc, e) =>
      if( "ByID" == e.getLocalName ) ( acc._1 + byIDEntries(e), acc._2) else ( acc._1, acc._2 + byNameEntries(e) )
    }

  private def byIDEntries(e: nu.xom.Element) = ( e.attribute("ID"), constraints(e) )

  private def byNameEntries(e: nu.xom.Element) = ( e.attribute("Name"), constraints(e) )

  private def constraints(e: nu.xom.Element) = (e.getChildElements("Constraint") map constraint).toList

  private def constraint( e: nu.xom.Element ): Constraint = {
    val id          = e.attribute("ID")
    val reference   = None
    val description = e.getFirstChildElement("Description").getValue
    Constraint(id, constraintTag(e), reference, description, assertion(e) )
  }

  private def constraintTag( e: nu.xom.Element ) = e.attribute("Tag") match {
    case ""              => ConstraintTag.ConfStmt
    case "ConfStatement" => ConstraintTag.ConfStmt
    case "Predicate"     => ConstraintTag.Predicate
    case x               => throw new Error(s"[Error] Unknown constraint tag '$x'")
  }

  private def assertion( e: nu.xom.Element ): Expression = {
    val assertionNode  = e.getFirstChildElement("Assertion")
    val expressionNode = assertionNode.getChildElements.get(0)
    expression.XMLDeserializer.expression( expressionNode )
  }
}

/*object MainCM extends App {
  
  val r = getClass.getResourceAsStream("/rules/ConfContextSample.xml")

  DefaultConstraintManager(r) match {
    case scala.util.Success(cm) =>
      println( cm.datatypeByID )
      println( cm.datatypeByName )
    case scala.util.Failure(e)  => e.printStackTrace()
  }
}*/