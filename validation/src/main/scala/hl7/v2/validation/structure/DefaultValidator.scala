package hl7.v2.validation
package structure

import hl7.v2.instance._
import hl7.v2.profile
import profile.{Range, Usage}
import hl7.v2.validation.report._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Default implementation of the structure validation
  */
trait DefaultValidator extends Validator with EscapeSeqHandler {

  /**
    * Checks the message structure and returns the list of problems.
    *
    * @param m - The message to be checked
    * @return  - The list of problems
    */
  def checkStructure(m: Message): Future[List[SEntry]] = Future {
    implicit val s = m.separators
    invalid( m.invalid ) ::: unexpected(m.unexpected) ::: check(m.asGroup)
  }

  /**
    * Checks the element against the the specified requirements
    * and recursively check the children if applicable
    * @param e - The element to be checked
    * @return A list of problems found
    */
  private def check(e: Element)(implicit sep: Separators): List[SEntry] =
    e match {
      case s: Simple  => check(s)
      case c: Complex => check(c)
    }

  /**
    * Checks the simple element against the specified requirements
    * @param s   - The simple element to be checked
    * @return A list of problems found
    */
  private def check(ss: Simple)(implicit s: Separators): List[SEntry] =
    ValueValidation.check(ss)

  /**
    * Checks the children of the complex element against their requirements
    * @param c - The complex element to be checked
    * @return A list of problems found
    */
  private def check(c: Complex)(implicit s: Separators): List[SEntry] = {
    // Sort the children by position
    val map = c.children.groupBy( x => x.position )

    // Check every position defined in the model
    val r = c.reqs.foldLeft( List[SEntry]() ) { (acc, r) =>
      // Get the children at the current position (r.position)
      val children = map.getOrElse(r.position, Nil)

      //FIXME we are missing the description here ...
      //val dl = c.location.copy(desc=r.description,
      //  path=s"${c.location.path}.${r.position}[1]")
      val dl = Utils.defaultLocation(c, r)

      // Checks the usage
      checkUsage( r.usage, children )(dl) match {
        case Nil => // No usage error thus we can continue the validation
          // Check the cardinality
          val r1 = checkCardinality( children, r.cardinality )
          // Recursively check the children
          val r2 = children flatMap check
          r1 ::: r2 ::: acc
        case xs  => xs ::: acc // Usage problem no further check is necessary
      }
    }

    // Check for extra children and return the result
    if( c.hasExtra ) Extra( c.location ) :: r else r
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
  private def checkUsage(u: Usage, l: List[Element])(dl: Location): List[SEntry] =
    (u, l) match {
      case (Usage.R,  Nil) => RUsage(dl) :: Nil
      case (Usage.RE, Nil) => REUsage(dl) :: Nil
      case (Usage.X,  xs ) => xs map { e => XUsage( e.location ) }
      case (Usage.W,  xs ) => xs map { e => WUsage( e.location ) }
      case _               => Nil
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
  private def checkCardinality(l: List[Element], range: Range): List[SEntry] =
    if( l.isEmpty ) Nil
    else {
      // The only reason this is needed is because of field repetition
      val highestRep = l maxBy ( e => e.instance )
      val i = highestRep.instance
      if( i < range.min ) MinCard( highestRep.location, i, range ) :: Nil
      else
        l filter { e => range.isBefore( e.instance ) } map { e =>
          MaxCard(e.location, e.instance, range)
        }
    }

  private
  def checkCardinality(l: List[Element], or: Option[Range]): List[SEntry] =
    or match {
      case Some(r) => checkCardinality(l, r)
      case None    => Nil
    }

  private def invalid(xs: List[Line]): List[InvalidLine] = xs map { line =>
    InvalidLine(line.number, line.content)
  }

  private def unexpected(xs: List[Line]): List[UnexpectedLine] =
    xs map { line =>
      UnexpectedLine(line.number, line.content)
    }
}
