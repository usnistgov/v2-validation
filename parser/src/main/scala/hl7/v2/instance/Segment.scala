package hl7.v2.instance

import hl7.v2.profile.{SegmentRef => SM, Field => FM, Primitive, Composite, DynMapping}

import scala.util.Try

/**
  * Class representing a segment
  */
case class Segment (
    model: SM,
    location: Location,
    instance: Int,
    children: List[Field],
    hasExtra: Boolean
) extends SegOrGroup

/**
  * Segment companion object
  */
object Segment extends EscapeSeqHandler {

  //FIXME: Handle Dynamic Mapping

  /**
    * Creates and returns a segment
    * @param m - The segment model
    * @param v - The value as string
    * @param i - The instance number
    * @param l - The line number
    * @return A segment
    */
  def apply(m: SM, v: String, i: Int, l: Int)
           (implicit s: Separators, ctr : Counter): Segment = {
    require( isValid( s.fs, s.cs, v ), s"Invalid segment instance '$v'" )
    val name = m.ref.name
    require(name == v.take(3), s"Invalid segment name. Expected: '$name', Found: '$v'")
    val nb = ctr.countFor(name);
    val loc = Location(EType.Segment, m.ref.desc, name, l, 1, s"$name[$nb]")
    var vs: Array[(Int, String)] = Array((5,v drop 3))
    if (!(v startsWith "UNA")) {
      vs = split(s.fs, v drop 4, 6)
    }
    // Attempt to resolve dynamic data types abort if errors
    val fml = resolveDyn(m.ref.fields, vs, m.ref.mappings).getOrElse(m.ref.fields)//m.ref.fields
    val (hasExtra, lfs) =
      if ( v startsWith "UNA") (vs.size > fml.size) -> unaField(fml, vs, loc)
      else if ( v startsWith "MSH") (vs.size > fml.size -1) -> mshFields(fml, vs, loc)
        else (vs.size > fml.size) -> fields( fml, vs, loc )
    val flatten =
      if(lfs.size>1) lfs.flatten
      else lfs.head
    Segment(m, loc, i, flatten, hasExtra)
  }

  /**
    * Creates abd returns a list of field
    * @param fml - The list of field models
    * @param vs  - The value and column array
    * @param l   - The parent location
    * @return A list of field
    */
  private def fields( fml: List[FM], vs: Array[(Int, String)], l: Location)
                    (implicit s: Separators) =
    fml zip vs map { t => repetitions( t._1, t._2, l ) }

  private def mshFields( fml: List[FM], vs: Array[(Int, String)], l: Location )
                       (implicit s: Separators) = {
    val `MSH.1` = field(l, fml.head, escape( s"${s.fs}" ), 1, 4) //FIXME: Do we have to escape here ?
    val `MSH.2` = field(l, fml.tail.head, escape( vs(0)._2 ), 1, 5) //FIXME: Do we have to escape here ?
    val _fields = fields(fml.tail.tail, vs drop 1 , l)
    `MSH.1`.toList  :: `MSH.2`.toList :: _fields
  }

  private def unaField( fml: List[FM], vs: Array[(Int, String)], l: Location )
                       (implicit s: Separators) = {
    var value = vs(0)._2 + s.ts.get
    if(fml.head.datatype.isInstanceOf[Composite]){
      val composite = fml.head.datatype.asInstanceOf[Composite].components.head
      val child = SimpleComponent(composite.datatype.asInstanceOf[Primitive],composite.req,l.copy(EType.Component,composite.datatype.desc,l.path+"-1.1",l.line,3,l.uidPath+"[1]-1[1].1"),Value(composite.datatype.asInstanceOf[Primitive],value))
      val datatype = fml.head.datatype
      val UNA = ComplexField(fml.head.datatype.asInstanceOf[Composite],fml.head.req,l.copy(EType.Field,datatype.desc,l.path+"-1",l.line,3,l.uidPath+"[1]-1[1]"),1,child::Nil,value.size>6)
      //val UNA = field(l,fml.head,vs(0)._2,1,5)
      List(UNA::Nil)
    } else {
      //if (fml.head.datatype.isInstanceOf[Primitive]){
      val datatype = fml.head.datatype
      //var child = SimpleComponent(datatype.asInstanceOf[Primitive],fml.head.req,l.copy(EType.Component,datatype.desc,l.path+"-1.1",l.line,3,l.uidPath+"[1]-1[1].1"),Value(datatype.asInstanceOf[Primitive],value))
      val UNA = SimpleField(datatype.asInstanceOf[Primitive], fml.head.req, l.copy(EType.Component, datatype.desc, l.path + "-1.1", l.line, 3, l.uidPath + "[1]-1[1].1"), 1, Value(datatype.asInstanceOf[Primitive], value))
      //var UNA = ComplexField(fml.head.datatype.asInstanceOf[Composite],fml.head.req,l.copy(EType.Field,datatype.desc,l.path+"-1",l.line,3,l.uidPath+"[1]-1[1]"),1,child::Nil,value.size>6)
      //val UNA = field(l,fml.head,vs(0)._2,1,5)
      List(UNA :: Nil)
    }
  }

  /**
    * Creates and returns a field
    * @param l - The parent location
    * @param m - The field model
    * @param v - The value as string
    * @param i - The instance(repetition) number
    * @param c - The column
    * @return A field
    */
  private def field(l: Location, m: FM, v: String, i: Int, c: Int)
                   (implicit s: Separators) = {
    val loc = location(l, m.name, m.req.position, i, c)
    DataElement.field(m.datatype, m.req, loc, v, i)
  }
  /**
    * Creates and returns a list representing a repetition of a field
    * @param m - The field model
    * @param t - The column and value
    * @param l - The parent location
    * @return A list representing a repetition of a field
    */
  private def repetitions( m: FM, t: (Int, String), l: Location)
                         (implicit s: Separators): List[Field] = {
    val vs = split(s.rs, t._2, t._1)
    val r = vs.toList.zipWithIndex map { tt =>
      val( (col, v), ins) = tt
      field(l, m, v, ins + 1, col)
    }
    r.flatten
  }

  /**
    * Regular expression for matching valid segment instance
    */
  private def segFormat(fs: Char, cs: Char) = s"([A-Z]{2}[A-Z0-9](?:\\Q$fs\\E.*)|UNA(?:\\Q$cs\\E.*))*".r

  /**
    * Returns true if s is a valid segment instance
    * @param s - The segment as string
    * @return True if s is a valid segment instance
    */
  private def isValid( fs: Char, cs: Char, s: String ) = segFormat(fs,cs).pattern.matcher( s ).matches

  /**
    * Creates and returns a location from the parent location
    * @param l - The parent location
    * @param d - The current location description
    * @param p - The current location position
    * @param i - The current location instance
    * @param c - The current location column
    * @return A location
    */
  private def location(l: Location, d: String,  p: Int, i: Int, c: Int) =
    l.copy(EType.Field, desc=d, path=s"${l.path}-$p", column = c, uidPath = s"${l.uidPath}-$p[$i]")

  private def resolveDyn(
      models: List[FM],
      vs: Array[(Int, String)],
      mappings: List[DynMapping]): Try[List[FM]] = Try {
    mappings match {
      case Nil => models
      case xs  =>
        models map { x =>
          mappings find ( _.position == x.req.position ) match {
            case None          => x
            case Some(mapping) =>
              val dt = mapping.map(vs(mapping.reference - 1)._2)
              x.copy(datatype = dt)
          }
        }
    }
  }
}
