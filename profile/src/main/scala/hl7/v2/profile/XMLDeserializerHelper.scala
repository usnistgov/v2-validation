package hl7.v2.profile

import nist.xml.util.XOMExtensions.{ExtendedElement, ExtendedElements}
import nu.xom.{Element, Elements}

object XMLDeserializerHelper {

  type SOG = Either[String, Group]

  def profile(e: Element): Profile = {
    val id = e.attribute("ID")
    //val typ        = e.attribute("Type")
    //val hl7Ver     = e.attribute("HL7Version")
    //val schemaVer  = e.attribute("SchemaVersion")
    val dtElems = e.getChildElements("Datatypes").get(0).getChildElements("Datatype")
    val sgElems = e.getChildElements("Segments").get(0).getChildElements("Segment")
    val mgElems = e.getChildElements("Messages").get(0).getChildElements("Message")
    implicit val dts = datatypes( dtElems )
    implicit val sgs = segments( sgElems )
    val mgs = messages( mgElems )
    Profile( id, mgs, sgs, dts )
  }

  def messages( elems: Elements )(implicit map: Map[String, Segment]) =
    elems.foldLeft(Map[String, Message]()) { (acc, x) =>
      val m = message(x)
      acc + ( (m.id, m) )
    }

  def segments( elems: Elements )(implicit map: Map[String, Datatype]) =
    elems.foldLeft( Map[String, Segment]() ){ (acc, e) =>
      val s = segment( e )
      acc + ( (s.id, s) )
    }

  /**
   * Creates and returns the data type map
   */
  //FIXME Exceptions can occur when l1 and l2 not properly defined.
  private def datatypes(elems: Elements): Map[String, Datatype] = {
    implicit var map = Map[String, Datatype]()
    val (primitives, l1, l2) = categorize( elems )
    primitives foreach { e => val dt = datatype( e ); map = map + ( (dt.id, dt) ) }
    l1 foreach { e => val dt = datatype( e ); map = map + ( (dt.id, dt) ) }
    l2 foreach { e => val dt = datatype( e ); map = map + ( (dt.id, dt) ) }
    map
  }


  def message(e: Element)(implicit map: Map[String, Segment]): Message = {
    val id = e.attribute("ID")
    val typ = e.attribute("Type")
    val event = e.attribute("Event")
    val desc = e.attribute("Description")
    val structId = e.attribute("StructID")
    val structure = children( e.getChildElements )
    Message(id, structId, event, typ, desc, structure )
  }

  private def group(name: String, r: Req, es: Elements)
                   (implicit map: Map[String, Segment])= {
    val structure = children( es )
    Group(name, structure, r)
  }

  def group(r: Req, e: Element)(implicit map: Map[String, Segment]): Group = {
    val name = e.attribute("Name")
    group(name, r, e.getChildElements)
  }

  private def children(es: Elements)
                      (implicit map: Map[String, Segment]): List[SegRefOrGroup] =
    (for( i <- 0 until es.size) yield {
      val ee  = es.get(i)
      val req = requirement(i + 1, ee)
      ee.getLocalName match {
        case "Segment" => SegmentRef(req, ee.attribute("Ref"))
        case "Group"   => group(req, ee)
        case x => throw new Error(s"[Error] Unsupported element '$x' found")
      }
    }).toList

  def segment(e: Element)(implicit map: Map[String, Datatype]): Segment = {
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

  def dynMappings(elements: Elements)
                 (implicit map: Map[String, Datatype]): List[DynMapping] =
    (elements map dynMapping).toList

  def dynMapping(e: Element)(implicit map: Map[String, Datatype]): DynMapping = {
    val pos = e.attribute("Position").toInt
    val ref = e.attribute("Reference").toInt
    val mapping =
      e.getChildElements("Case").foldLeft(Map[String, Datatype]()) { (acc, x) =>
        acc + ( x.attribute("Value") -> map(x.attribute("Datatype")) )
      }
    DynMapping(pos, ref, mapping)
  }

  /**
    * Creates and returns a field object from a xom.Element
    */
  def field(p: Int, e: Element)(implicit map: Map[String, Datatype]) =
    dataElem(p, e, Field.apply)

  /**
    * Creates a data type object from xom.Element
    */
  def datatype(e: Element)(implicit map: Map[String, Datatype]): Datatype = {
    val id   = e.attribute("ID")
    val name = e.attribute("Name")
    val desc = e.attribute("Description")
    val cs   = e.getChildElements("Component")
    val comps = for(i <- 0 until cs.size) yield component(i + 1, cs.get(i))
    if( comps.size == 0 ) Primitive(id, name, desc)
    else Composite(id, name, desc, comps.toList)
  }

  /**
    * Creates a component object from xom.Element
    */
  private def component(p: Int, e: Element)(implicit map: Map[String, Datatype]) =
    dataElem(p, e, Component.apply)

  private
  def dataElem[A](p: Int, e: Element, f: (String, Datatype, Req) => A)
                 (implicit map: Map[String, Datatype]): A = {
    val name = e.attribute("Name")
    val dtId = e.attribute("Datatype")
    val req  = requirement(p, e)
    f(name, map(dtId), req)
  }

  /**
    * Creates a requirement(Req) object from xom.Element
    */
  def requirement(p: Int, e: Element): Req = {
    val usage  = Usage.fromString( e.attribute("Usage") )
    val card   = cardinality(e)
    val len    = length(e)
    val table  = asOption( e.attribute("Table") )
    val confLen  = asOption( e.attribute("ConfLength") )
    Req(p, usage, card, len, confLen, table)
  }

  /**
    * Extracts the length from a xom.Element
    */
  def length(e: Element): Option[Range] =
    asOption( e.attribute("MinLength") ) map { min =>
      Range( min.toInt, e.attribute("MaxLength") )
    }

  /**
    * Extracts the cardinality
    */
  def cardinality(e: Element): Option[Range] =
    asOption( e.attribute("Min") ) map { min =>
      Range( min.toInt, e.attribute("Max") )
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
