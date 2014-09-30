package hl7.v2.profile

import nist.xml.util.XOMExtensions.{ExtendedElement, ExtendedElements}
import nu.xom.{Element, Elements}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object XMLDeserializerHelper {

  type SOG = Either[String, Group]

  def profile(e: Element): Future[Profile] = {
    val id = e.attribute("ID")
    // Computes the different maps in parallel
    val mmap = messages ( e.getChildElements("Messages" ).get(0) )
    val smap = segments ( e.getChildElements("Segments" ).get(0) )
    val dmap = datatypes( e.getChildElements("Datatypes").get(0) )
    for {
      ms <- mmap
      ss <- smap
      ds <- dmap
    } yield Profile( id, ms, ss, ds )
  }

  def messages(e: Element): Future[Map[String, Message]] =
    Future {
      e.getChildElements("Message").foldLeft( Map[String, Message]() )
        { (acc, x) =>
          val m = message(x)
          acc + (m.id -> m)
        }
    }

  def segments(e: Element): Future[Map[String, Segment]] =
    Future {
      e.getChildElements("Segment").foldLeft( Map[String, Segment]() )
        { (acc, x) =>
          val s = segment(x)
          acc + (s.id -> s)
        }
    }

  def datatypes(e: Element): Future[Map[String, Datatype]] =
    Future {
      e.getChildElements("Datatype").foldLeft( Map[String, Datatype]() )
        { (acc, d) =>
          val dt = datatype(d)
          acc + (dt.id -> dt)
        }
    }

  def message(e: Element): Message = {
    val id     = e.attribute("ID")
    val `type` = e.attribute("Type")
    val event  = e.attribute("Event")
    val desc   = e.attribute("Description") //FIXME
    val structId = e.attribute("StructID")
    val root     = group( structId, e.getChildElements )
    Message(id, structId, event, `type`, desc, root)
  }

  private def group(name: String, es: Elements) = {
    val structure = children( es )
    Group(name, structure)
  }

  def group(e: Element): Group = {
    val name = e.attribute("Name")
    group(name, e.getChildElements)
  }

  private def children(es: Elements): List[(Req, SOG)] =
    (for( i <- 0 until es.size) yield {
      val ee  = es.get(i)
      val req = requirement(i + 1, ee)
      ee.getLocalName match {
        case "Segment" => req -> Left( ee.attribute("Ref") )
        case "Group"   => req -> Right( group(ee))
        case x => throw new Error(s"[Error] Unsupported element '$x' found")
      }
    }).toList

  def segment(e: Element): Segment = {
    val id   = e.attribute("ID")
    val name = e.attribute("Name")
    val desc = e.attribute("Description")
    val fes  = e.getChildElements("Field")
    val fields = for(i <- 0 until fes.size) yield field(i + 1,  fes.get(i))
    val dme = e.getChildElements("DynamicMapping")
    val mappings = if( dme.size == 0 ) Nil
    else dynMappings( dme.get(0).getChildElements("Mapping") )
    Segment(id, name, desc, fields.toList, mappings)
  }

  def dynMappings(elements: Elements): List[DynMapping] =
    (elements map dynMapping).toList

  def dynMapping(e: Element): DynMapping = {
    val pos = e.attribute("Position").toInt
    val ref = e.attribute("Reference").toInt
    val map =
      e.getChildElements("Case").foldLeft(Map[String, String]()) { (acc, x) =>
        acc + ( x.attribute("Value") -> x.attribute("Datatype") )
      }
    DynMapping(pos, ref, map)
  }

  def field(p: Int, e: Element) = dataElem(p, e, Field.apply)

  def datatype(e: Element): Datatype = {
    val id   = e.attribute("ID")
    val name = e.attribute("Name")
    val desc = e.attribute("Description")
    val cs   = e.getChildElements("Component")
    if( cs.size == 0 )
      Primitive(id, name, desc)
    else {
      val comps = for(i <- 0 until cs.size) yield component(i + 1, cs.get(i))
      Composite(id, name, desc, comps.toList)
    }
  }

  private def component(p: Int, e: Element) = dataElem(p, e, Component.apply)

  private
  def dataElem[A](p: Int, e: Element, f: (String, String, Req) => A): A = {
    val name = e.attribute("Name")
    val dtId = e.attribute("Datatype")
    val req  = requirement(p, e)
    f(name, dtId, req)
  }

  def requirement(p: Int, e: Element): Req = {
    val usage  = Usage.fromString( e.attribute("Usage") )
    val card   = cardinality(e)
    val len    = length(e)
    val table  = asOption( e.attribute("Table") )
    val confLen  = asOption( e.attribute("ConfLength") )
    Req(p, usage, card, len, confLen, table)
  }

  def length(e: Element): Option[Range] =
    asOption( e.attribute("MinLength") ) map { min =>
      Range( min.toInt, e.attribute("MaxLength") )
    }

  def cardinality(e: Element): Option[Range] =
    asOption( e.attribute("Min") ) map { min =>
      Range( min.toInt, e.attribute("Max") )
    }

  private def asOption(s: String) = if( s == "" ) None else Some( s )
}
