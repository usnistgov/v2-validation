package hl7.v2.validation.coconstraints

import gov.nist.hit.hl7.v2.schemas.utils.{HL7v2Schema, HL7v2SchemaResourceResolver}

import java.io.InputStream
import hl7.v2.instance.Message
import hl7.v2.profile.Range
import hl7.v2.validation.vs.DefaultValueSetSpecification
import nist.xml.util.{ClassPathResourceResolver, XOMDocumentBuilder}

import scala.util.{Failure, Try}

class DefaultCoConstraintValidationContext(val coConstraintsMap: Map[String, List[CoConstraintBindingContext]]) extends CoConstraintValidationContext {

  override def coConstraintBindingsFor(message: Message): List[CoConstraintBindingContext] = {
    coConstraintsMap.getOrElse(message.model.id, Nil)
  }

}

object DefaultCoConstraintValidationContext {

  import nist.xml.util.XOMExtensions._

  /**
    * The resource resolver
    */
  private val resourceResolver = Some(new HL7v2SchemaResourceResolver())

  def apply(context: InputStream): Try[CoConstraintValidationContext] = {
    Failure(new Exception())
    XOMDocumentBuilder.build(context, HL7v2Schema.getCoConstraints, resourceResolver) map { doc =>
      new DefaultCoConstraintValidationContext(parse(doc))
    }
  }

  def parse(doc: nu.xom.Document) : Map[String, List[CoConstraintBindingContext]] = {
    val ccd = doc.getRootElement
    if(ccd != null) {
      val bindings = ccd.getChildElements("ByMessage").map(message => {
        val messageId = message.getAttributeValue("ID")
        val bindings = byMessageBinding(message)
        messageId -> bindings
      }) groupBy(_._1)
      bindings.view.mapValues(_.flatMap(_._2).toList).toMap
    }  else {
      Map()
    }
  }

  def byMessageBinding(message: nu.xom.Element): List[CoConstraintBindingContext] = {
    message.getChildElements("Context").map(context => {
      val path = context.getAttributeValue("Path")
      val name = context.getAttributeValue("Name")
      val bindings = messageContextBinding(context)
      CoConstraintBindingContext(name, path, bindings)
    }).toList
  }

  def messageContextBinding(context: nu.xom.Element): List[CoConstraintBindingSegment] = {
    context.getChildElements("Segment").map(segment => {
      val path = segment.getAttributeValue("Path")
      val name = segment.getAttributeValue("Name")
      val bindings = coConstraintTables(segment)
      CoConstraintBindingSegment(name, path, bindings)
    }).toList
  }

  def coConstraintTables(e: nu.xom.Element): List[CoConstraintTable] = {
    val simple = e.getChildElements("SimpleTable").map(t => simpleTable(t))
    val conditional = e.getChildElements("ConditionalTable").map(t => conditionalTable(t))
    (simple ++ conditional).toList
  }

  def groupId(e: nu.xom.Element): List[GroupId] = {
    (e.getChildElements("IdPath").map(id => {
      GroupId(id.getAttributeValue("Priority").toInt, id.getAttributeValue("Path"), id.getAttributeValue("Name"))
    }) sortBy(_.priority)).toList
  }

  def simpleTable(e: nu.xom.Element): SimpleCoConstraintTable = {
    val grouper = e.getFirstChildElement("CoConstraintGroupId")
    val cc = e.getChildElements("CoConstraint").map(t => coConstraint(t))
    val grp = e.getChildElements("CoConstraintGroup").map(t => group(t))
    SimpleCoConstraintTable(if(grouper == null) Nil else groupId(grouper), cc.toList, grp.toList)
  }

  def conditionalTable(e: nu.xom.Element): ConditionalCoConstraintTable = {
    val grouper = e.getFirstChildElement("CoConstraintGroupId")
    val cc = e.getChildElements("CoConstraint").map(t => coConstraint(t))
    val cond = condition(e.getFirstChildElement("Condition"))
    val grp = e.getChildElements("CoConstraintGroup").map(t => group(t))
    ConditionalCoConstraintTable(cond, if(grouper == null) Nil else groupId(grouper), cc.toList, grp.toList)
  }

  def condition(e: nu.xom.Element): Condition = {
    val desc = e.getFirstChildElement("Description").getValue
    val assertion = e.getFirstChildElement("Assertion")
    Condition(desc, expression.XMLDeserializer.expression(assertion.getChildElements().get(0)))
  }

  def coConstraint(e: nu.xom.Element): CoConstraint = {
    val selectors = cells(e.getFirstChildElement("Selectors"))
    val constraints = cells(e.getFirstChildElement("Constraints"))
    val req = requirement(e)
    CoConstraint(req, selectors, constraints)
  }

  def group(e: nu.xom.Element): CoConstraintGroup = {
    val name = e.getAttributeValue("Name")
    val req = requirement(e)
    val primary = coConstraint(e.getFirstChildElement("Primary"))
    val cc = e.getChildElements("CoConstraint").map(t => coConstraint(t))
    CoConstraintGroup(name, req, primary, cc.toList)
  }

  def requirement(e: nu.xom.Element): CoConstraintRequirement = {
    val u = CoConstraintUsage.fromString(e.getAttributeValue("Usage"))
    val min = e.getAttributeValue("Min")
    val max = e.getAttributeValue("Max")
    CoConstraintRequirement(u, Range(min.toInt, max))
  }

  def cells(e: nu.xom.Element): List[CoConstraintCell] = {
    val pt = e.getChildElements("PlainText").map(t => plainText(t))
    val cd = e.getChildElements("Code").map(t => code(t))
    val vs = e.getChildElements("ValueSet").map(t => valueSet(t))
    (pt ++ cd ++ vs).toList
  }

  def plainText(e: nu.xom.Element): PlainText = {
    val element = e.getAttributeValue("Name")
    val path = e.getAttributeValue("Path")
    val value = e.getAttributeValue("Value")
    PlainText(element, path, value)
  }

  def code(e: nu.xom.Element): Code = {
    val element = e.getAttributeValue("Name")
    val path = e.getAttributeValue("Path")
    val code = e.getAttributeValue("Code")
    val codeSys = e.getAttributeValue("CodeSystem")
    val locations = e.getChildElements("BindingLocation").map(bl => {
      val position = bl.getAttributeValue("Position").toInt
      val code = bl.getAttributeValue("CodePath")
      val codeSystem = bl.getAttributeValue("CodeSystemPath")
      CoConstraintBindingLocation(position, code, codeSystem)
    })
    Code(element, path, code, codeSys, locations.toList)
  }

  def valueSet(e: nu.xom.Element): ValueSet = {
    val element = e.getAttributeValue("Name")
    val path = e.getAttributeValue("Path")
    val bindings = e.getChildElements("ValueSetBinding").map(vb => DefaultValueSetSpecification.vBinding(vb))
    ValueSet(element, path, bindings.toList)
  }
}