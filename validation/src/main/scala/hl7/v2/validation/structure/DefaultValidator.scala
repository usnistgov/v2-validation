package hl7.v2.validation.structure

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import hl7.v2.profile.{Req, Usage, Range}
import hl7.v2.instance.{Complex, Simple, Element, Value, Message, Location}
import hl7.v2.validation.report._


trait DefaultValidator extends Validator {


  def checkStructure(m: Message): Future[Seq[SEntry]] =
    Future { check( m.asGroup) }

  def check(e: Element, r: Req): List[SEntry] = e match {
    case s: Simple  => check(s, r.length.get, r.table) //FIXME will blow if length is not defined ... :-)
    case c: Complex => check(c)
  }

  // Checks a simple elememt
  def check(s: Simple, len: Range, table: Option[String]): List[SEntry] = {
    //FIXME 1. Check the length
    //FIXME 2. Check escape sequence
    ???
  }

  // Checks a complex element
  def check(c: Complex): List[SEntry] = {

    // Sort the children by position
    val map = c.children.groupBy( x => x.position )

    // For each position ...
    c.reqs.foldLeft( List[SEntry]() ) { (acc, r) =>
      // Get the children at the current position (r.position)
      val children = map.getOrElse(r.position, Nil)

      // Checks the usage
      checkUsage( r.usage, children )(c.location) match {
        case Nil => // No usage error thus we can continue the validation
          val r1 = checkExtra( c )  // Check for extra
          val r2 = checkCardinality( children, r.cardinality ) // Check the cardinality
          // Recursively check the children
          val r3 = children flatMap { check( _, r ) }
          r1 ::: r2 ::: r3 ::: acc
        case xs  => xs ::: acc // Usage problem no further check is necessary
      }
    }
  }

  /**
   * Returns a list of report entries if the usage is:
   *     1) R and the list of elements is empty
   *     2) X and the list of elements is not empty
   *     3) W and the list of elements is not empty
   *
   * @param u  - The usage
   * @param l  - The list of elements
   * @param dl - The default location
   * @return A list of report entries
   */
  def checkUsage(u: Usage, l: List[Element])(dl: Location): List[SEntry] =
    (u, l) match {
      case (Usage.R, Nil) => RUsage(dl) :: Nil
      case (Usage.X, xs ) => xs map { e => XUsage( e.location ) }
      case (Usage.W, xs ) => xs map { e => WUsage( e.location ) }
      case _                     => Nil
    }

  /**
   * Returns a list of report entries for every element which instance
   * number is greater than the maximum range or a list with a single
   * element if the highest instance number is lower than the minimum range
   *
   * @param l - The list of element
   * @param range - The cardinality range
   * @return A list of report entries
   */
  def checkCardinality(l: List[Element], range: Range): List[SEntry] =
    if( l.isEmpty ) Nil
    else {
      val highestRep = l maxBy instance
      val i = instance( highestRep )
      if( i < range.min ) MinCard( highestRep.location, i, range ) :: Nil
      else
        l filter { e => afterRange( instance(e), range ) } map { e =>
          MaxCard(e.location, instance(e), range)
        }
    }

  private
  def checkCardinality(l: List[Element], or: Option[Range]): List[SEntry] =
    or match {
      case Some(r) => checkCardinality(l, r)
      case None    => Nil
    }

  def checkExtra(c: Complex): List[SEntry] =
    if( c.hasExtra ) Extra( c.location ) :: Nil else Nil

  /**
    * Returns `Some(Entry)' if the value's length is not in range `None' otherwise
    *
    * This assumes that the underlining string has been properly escaped
    */
  def checkLength(s: Simple, range: Range): List[SEntry] =
    if( inRange(s.value.asString.length, range) ) Nil
    else Length(s.location, s.value.asString, range) :: Nil

  // Returns the instance number of the element
  private def instance(e: Element) = e.instance

  // Returns true if i is in the range
  def inRange(i: Int, r: Range) =
    i >= r.min && ( r.max == "*" || i <= r.max.toInt)

  // Returns true is i > Range.max
  def afterRange(i: Int, r: Range) =
    if( r.max == "*" ) false else i > r.max.toInt

  private def lengthOf(v: Value) = v.asString.length

}


/*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import hl7.v2.instance.ComplexComponent
import hl7.v2.instance.ComplexField
import hl7.v2.instance.Component
import hl7.v2.instance.Field
import hl7.v2.instance.Group
import hl7.v2.old.Location
import hl7.v2.old.Message
import hl7.v2.instance.Segment
import hl7.v2.old.Simple
import hl7.v2.instance.SimpleComponent
import hl7.v2.instance.SimpleField
import hl7.v2.profile.Range
import hl7.v2.validation.report.{SEntry, InvalidLines, UnexpectedLines}

/**
  * Default implementation of the structure validator
  * 
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait DefaultValidator extends Validator with BasicChecks {

  def checkStructure(m: Message): Future[Seq[SEntry]] = Future {
    (m.unexpected, m.invalid) match {
      case (Nil, Nil) => check(m.asGroup)
      case (u, Nil)   => UnexpectedLines(u) :: check(m.asGroup)
      case (Nil, i)   => InvalidLines(i) :: check(m.asGroup)
      case (u, i)     => InvalidLines(i) :: UnexpectedLines(u) :: check(m.asGroup)
    }
  }

  /**
    * Checks the group against the constraints defined
    * in the profile and return the list of problem.
    */
  private def check(g: Group): List[SEntry] =
    (g.structure zip g.model.children) flatMap { _ match {
      case (Left(ls), Left(model)) => 
        val dl = location( g.location, model.position )
        checkUsage(model.usage, ls)(dl) match {
          case Nil => checkCardinality(ls, model.cardinality) ::: (ls flatMap check)
          case xs  => xs
        }
      case (Right(lg), Right(model)) => 
        val dl = location( g.location, model.position )
        checkUsage(model.usage, lg)(dl) match {
          case Nil => checkCardinality(lg, model.cardinality) ::: (lg flatMap check)
          case xs  => xs
        }
      case _ => ??? //FIXME
    }}

  /**
    * Checks the segment against the constraints defined
    * in the profile and return the list of problems.
    */
  private def check(s: Segment): List[SEntry] =
    (s.fields zip s.model.ref.fields) flatMap { t =>
      val(lf, model) = t
      val dl = location( s.location, model.position )
      checkUsage(model.usage, lf)(dl) match {
        case Nil => checkCardinality(lf, model.cardinality) ::: (lf flatMap check)
        case xs  => xs
      }
    }

  /**
    * Checks the component against the constraints defined
    * in the profile and return the list of problems.
    */
  private def check(f: Field): List[SEntry] = f match {
    case sf: SimpleField  => check(sf, sf.model.length)
    case cf: ComplexField => check(cf.location, cf.components, cfc(cf))
  }

  /**
    * Checks the component against the constraints defined
    * in the profile and return the list of problems.
    */
  private def check(c: Component): List[SEntry] = c match {
    case sc: SimpleComponent  => check(sc, sc.model.length)
    case cc: ComplexComponent => check(cc.location, cc.components, ccc(cc))
  }

  type OC = Option[Component] // Alias

  /**
    * Checks the complex data element (either a complex field or a 
    * complex component) children and returns the list of problems. 
    *
    * 
    * @param  l - The location of the complex data element
    * @param cl - The list of children
    * @param ml - The children models
    * @return The list of problems
    */
  private def check(l: Location, cl: List[OC], ml: List[Component]): List[SEntry] =
    (cl zip ml ) flatMap { t =>
      val(oc, model) = t
      val dl = location( l, model.position )
      checkUsage(model.usage, oc.toList)(dl) match {
        case Nil => oc match { case Some(c) => check(c) case _ => Nil }
        case xs  => xs
      }
    }

  /**
    * Checks the simple element and returns the list of problems
    * 
    * @param s - The simple element
    * @param l - The length constraint
    * @return The list of problems
    */
  private def check(s: Simple, l: Range): List[SEntry] = checkLength(s, l)

  /**
    * Creates a new location by changing the path
    */
  private def location(l: Location, position: Int) =
    l.copy( path = s"${l.path}.$position")

  /**
    * Complex field children models
    */
  private def cfc(cf: ComplexField) = cf.model.datatype.components

  /**
    * Complex component children models
    */
  private def ccc(cc: ComplexComponent) = cc.model.datatype.components
}
*/