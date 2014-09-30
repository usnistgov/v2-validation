package hl7.v2.profile.old

import java.io.InputStream

import hl7.v2.profile._
import nist.xml.util.XOMDocumentBuilder
import nist.xml.util.XOMExtensions.{ExtendedElement, ExtendedElements}
import nu.xom.{Element, Elements}

import scala.util.{Failure, Success, Try}

/**
  * Module to deserialize a profile from XML
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

object XMLDeserializer {

  /**
    * Create a profile from XML
    * @param xml - The XML file
    * @param xsd - The profile XSD schema
    * @return The profile object or a failure
    */
  def deserialize(xml: InputStream, xsd: InputStream): Try[Profile] = 
    XOMDocumentBuilder.build(xml, xsd) match {
      case Success(doc) => Try( profile( doc.getRootElement ) )
      case Failure(e)   => Failure(e)
    }

  private def profile( e: Element ) = {
    val id         = e.attribute("ID")
    val typ        = e.attribute("Type")
    val hl7Ver     = e.attribute("HL7Version")
    val schemaVer  = e.attribute("SchemaVersion")
    implicit val dts = datatypes( e.getChildElements("Datatypes").get(0).getChildElements("Datatype") )
    implicit val sgs = segments( e.getChildElements("Segments").get(0).getChildElements("Segment") )
    val mgs = messages( e.getChildElements("Messages").get(0).getChildElements("Message") )
    Profile( id, typ, hl7Ver, schemaVer, mgs, sgs, dts )
  }

  private def messages( elements: Elements )(implicit map: Map[String, Segment]) = 
    elements.foldLeft(Map[String, Message]()) { (acc, x) => 
      val m = message(x)
      acc + ( (m.id, m) )
    }

  private def message( e: Element )(implicit map: Map[String, Segment]) = {
    val id          = e.attribute("ID")
    val typ         = e.attribute("Type")
    val event       = e.attribute("Event")
    val structID    = e.attribute("StructID")
    val description = e.attribute("Description")
    val children    = mems(e)
    Message(id, typ, event, structID, description, children)
  }

  private def mems( e: Element)(implicit map: Map[String, Segment]): List[Either[SegmentRef, Group]] = {
    var c = scala.collection.mutable.ListBuffer[Either[SegmentRef, Group]]()
    val childrenElms = e.getChildElements
    for(i <- 0 until childrenElms.size) {
      val ee = childrenElms.get(i)
      if( "Segment" == ee.getLocalName ) c += Left(segmentRef(ee, i + 1))
      else if( "Group" == ee.getLocalName ) c += Right(group(ee, i + 1))
      else ??? //FIXME if( "Choice" == ee.getLocalName ) c += choice(ee, i + 1)
    } 
    c.toList
  }

  private def group(e: Element, position: Int)(implicit map: Map[String, Segment]) = {
    val name     = e.attribute("Name")
    val usage    = Usage.fromString( e.attribute("Usage") )
    val card     = cardinality(e)
    val children = mems(e)
    Group(position, name, usage, card, children)
  }

  private def segmentRef(e: Element, position: Int)(implicit map: Map[String, Segment]) = {
    val ref      = e.attribute("Ref")
    val usage    = Usage.fromString( e.attribute("Usage") )
    val card     = cardinality(e)
    SegmentRef(position, ref, usage, card)
  }

  private def segments( elements: Elements )(implicit map: Map[String, Datatype]) = 
    elements.foldLeft( Map[String, Segment]() ){ (acc, e) => 
      val s = segment( e )
      acc + ( (s.id, s) )
    }

  private def segment( e: Element )(implicit map: Map[String, Datatype]) = {
    val id           = e.attribute("ID")
    val name         = e.attribute("Name")
    val description  = e.attribute("Description")
    val dmElements   = e.getChildElements("DynamicMapping")
    val dynaMappings = if( dmElements.size == 0 ) Nil 
                        else dynMappings( dmElements.get(0).getChildElements("Mapping") )
    val fElements    = e.getChildElements("Field")
    val fields       = for ( i <- 0 until fElements.size ) yield field ( fElements.get(i), i + 1 )
    Segment( id, name, description, fields.toList, dynaMappings )
  }

  def dynMappings( elements: Elements )(implicit map: Map[String, Datatype]) =
    (elements map dynMapping).toList

  private def dynMapping( e: Element )( implicit map: Map[String, Datatype] ) = {
    val position  = e.attribute("Position").toInt
    val reference = e.attribute("Reference").toInt
    val cases     = e.getChildElements("Case")
    val mapping = cases map { c => ( c.attribute("Value"), map( c.attribute("Datatype")) ) }
    DynamicMapping(position, reference, mapping.toMap)
  }

  private def field( e: Element, position: Int )(implicit map: Map[String, Datatype]) = {
    val name     = e.attribute("Name")
    val datatype = e.attribute("Datatype")
    val usage    = Usage.fromString( e.attribute("Usage") )
    val card     = cardinality(e)
    val len      = length(e)
    val confLen  = e.attribute("ConfLength")
    val table    = asOption( e.attribute("Table") )
    Field(position, name, datatype, usage, card, len, confLen, table)
  }

  //FIXME Exceptions can occur when l1 and l2 not properly defined.
  private def datatypes( elements: Elements ) = {
    implicit var map = Map[String, Datatype]()
    val (primitives, l1, l2) = categorize( elements )
    primitives foreach { e => val dt = datatype( e ); map = map + ( (dt.id, dt) ) }
    l1 foreach { e => val dt = datatype( e ); map = map + ( (dt.id, dt) ) }
    l2 foreach { e => val dt = datatype( e ); map = map + ( (dt.id, dt) ) }
    map
  }

  private def datatype( e: Element )(implicit map: Map[String, Datatype]) = {
    val id          = e.attribute("ID")
    val name        = e.attribute("Name")
    val description = e.attribute("Description")
    val children    = e.getChildElements
    val components  = for ( i <- 0 until children.size ) yield component ( children.get(i), i + 1 )
    Datatype(id, name, description, components.toList)
  }

  private def component( e: Element, position: Int )(implicit map: Map[String, Datatype]) = {
    val name     = e.attribute("Name")
    val datatype = e.attribute("Datatype")
    val usage    = Usage.fromString( e.attribute("Usage") )
    val len      = length(e)
    val confLen  = e.attribute("ConfLength")
    val table    = asOption( e.attribute("Table") )
    Component( position, name, datatype, usage, len, confLen, table )
  }

  private def length(e: Element): Range = {
    val minLen   = e.attribute("MinLength").toInt
    val maxLen   = e.attribute("MaxLength")
    Range(minLen, maxLen)
  }

  private def cardinality(e: Element): Range = {
    val min   = e.attribute("Min").toInt
    val max   = e.attribute("Max")
    Range(min, max)
  }

  private def asOption(s: String) = if( s == "" ) None else Some( s )

  /**
    * Sorts the data type elements into 3 categories: 
    *   - Primitives
    *   - Level 1: Complex with primitive children
    *   - Level 2: The rest
    */
  private def categorize(elements: Elements) = {
    def isPrimitive(e: Element) = e.getChildElements("Component").size() == 0
    val(primitives, others) = elements.partition( isPrimitive )
    def isL1(e: Element) = e.getChildElements("Component").forall { ee => 
      primitives.exists( _.attribute("ID") == ee.attribute("Datatype") ) 
    }
    val(l1, l2) = others.partition( isL1 )
    (primitives, l1, l2)
  }
}