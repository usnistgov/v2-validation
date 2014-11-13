package hl7.v2.validation.structure

import hl7.v2.instance.{Complex, Element, Location, Message, Simple}
import hl7.v2.profile.{Range, Req, Usage}
import hl7.v2.validation.report._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Default implementation of the structure validation
  */
trait DefaultValidator extends Validator {

  def checkStructure(m: Message): Future[List[SEntry]] =
    Future {
      (m.invalid, m.unexpected) match {
        case (Nil, Nil) => check(m.asGroup)
        case (xs , Nil) => InvalidLines(xs) :: check(m.asGroup)
        case (Nil,  xs) => UnexpectedLines(xs) :: check(m.asGroup)
        case (xs, ys) => InvalidLines(xs) :: UnexpectedLines(ys) :: check(m.asGroup)
      }
    }

  /**
    * Checks the element against the the specified requirements
    * and recursively check the children if applicable
    * @param e - The element to be checked
    * @param r - The requirements
    * @return A list of problems found
    */
  def check(e: Element, r: Req): List[SEntry] = e match {
    case s: Simple  => check(s, r)
    case c: Complex => check(c)
  }

  /**
    * Checks the simple element against the specified requirements
    * @param s   - The simple element to be checked
    * @param req - The requirements
    * @return A list of problems found
    */
  def check(s: Simple, req: Req): List[SEntry] =
    checkUnescapedSeparators( s ) ::: checkLength( s, req.length )

  /**
    * Checks the children of the complex element against their requirements
    * @param c - The complex element to be checked
    * @return A list of problems found
    */
  def check(c: Complex): List[SEntry] = {
    // Sort the children by position
    val map = c.children.groupBy( x => x.position )

    // Check every position defined in the model
    val r = c.reqs.foldLeft( List[SEntry]() ) { (acc, r) =>
      // Get the children at the current position (r.position)
      val children = map.getOrElse(r.position, Nil)

      //FIXME we are missing the description here ...
      val dl = c.location.copy(desc="...", path=s"${c.location.path}.${r.position}[1]")

      // Checks the usage
      checkUsage( r.usage, children )(dl) match {
        case Nil => // No usage error thus we can continue the validation
          // Check the cardinality
          val r1 = checkCardinality( children, r.cardinality )
          // Recursively check the children
          val r2 = children flatMap { check( _, r ) }
          r1 ::: r2 ::: acc
        case xs  => xs ::: acc // Usage problem no further check is necessary
      }
    }

    // Check for extra children and return the result
    checkExtra( c ) ::: r
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
      case _              => Nil
    }

  /**
    * Returns a list of report entries for every element which instance
    * number is greater than the maximum range or a list with a single
    * element if the highest instance number is lower than the minimum range
    *
    * @param l     - The list of element
    * @param range - The cardinality range
    * @return A list of report entries
    */
  def checkCardinality(l: List[Element], range: Range): List[SEntry] =
    if( l.isEmpty ) Nil
    else {
      // The only reason this is needed is because of field repetition
      val highestRep = l maxBy instance
      val i = instance( highestRep )
      if( i < range.min ) MinCard( highestRep.location, i, range ) :: Nil
      else
        l filter { e => afterRange( instance(e), range ) } map { e =>
          MaxCard(e.location, instance(e), range)
        }
    }

  /**
    * Returns a list containing an extra entry
    * if the complex element has extra children.
    * @param c - The complex element
    * @return A list of report entries
    */
  def checkExtra(c: Complex): List[SEntry] =
    if( c.hasExtra ) Extra( c.location ) :: Nil else Nil

  private
  def checkCardinality(l: List[Element], or: Option[Range]): List[SEntry] =
    or match {
      case Some(r) => checkCardinality(l, r)
      case None    => Nil
    }

  /**
    * Returns a length entry if the length is not in range Nil otherwise.
    * This assumes that the underlining string has been properly escaped.
    */
  def checkLength(s: Simple, range: Range): List[SEntry] = {
    val v = s.value.asString
    if (inRange(v.length, range)) Nil else Length(s.location, v, range) :: Nil
  }

  private def checkLength(s: Simple, or: Option[Range]): List[SEntry] =
    or match {
      case Some(r) => checkLength(s, r)
      case None    => Nil
    }

  private def checkUnescapedSeparators(s: Simple): List[UnescapedSeparators] =
    if( s.value.isUnescaped ) UnescapedSeparators( s.location ):: Nil else Nil

  /**
    * Returns the instance number of the element
    */
  private def instance(e: Element) = e.instance

  /**
    * Returns true if i is in the range
    */
  def inRange(i: Int, r: Range) = i >= r.min && (r.max == "*" || i <= r.max.toInt)

  /**
    * Returns true is i > Range.max
    */
  def afterRange(i: Int, r: Range) = if(r.max == "*") false else i > r.max.toInt

}
