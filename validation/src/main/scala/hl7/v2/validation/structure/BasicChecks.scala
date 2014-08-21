package hl7.v2.validation.structure

import scala.language.implicitConversions

import hl7.v2.instance.Element
import hl7.v2.instance.Location
import hl7.v2.instance.Simple
import hl7.v2.instance.Value
import hl7.v2.profile.Range
import hl7.v2.profile.Usage

/**
  * Trait containing functions for checking constraints defined 
  * in the profile i.e. usage, cardinality, length, code set etc.
  *
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait BasicChecks {

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
  def checkUsage(u: Usage, l: List[Element])(dl: Location): List[Entry] =
    (u, l) match {
      case (Usage.R, Nil) => RUsage(dl) :: Nil
      case (Usage.X,  xs) => xs map { e => XUsage( e.location ) }
      case (Usage.W,  xs) => xs map { e => WUsage( e.location ) }
      case _              => Nil
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
  def checkCardinality( l: List[Element], range: Range): List[Entry] =
    if( l.isEmpty ) Nil
    else {
      val highestRep = l maxBy instance
      val i = instance( highestRep )
      if( i < range.min ) MinCard( highestRep.location, i, range ) :: Nil
      else l filter { e => afterRange( instance(e), range ) } map { e => 
        MaxCard(e.location, instance(e), range)
      }
    }

  /**
    * Returns `Some(Entry)' if the value's length is not in range `None' otherwise
    */
  def checkLength(s: Simple, range: Range): List[Entry] = 
    if( inRange(s.value.length, range) ) Nil 
    else Length(s.location, s.value, range) :: Nil

  // Returns true if i is in the range 
  def inRange(i: Int, r: Range) = i >= r.min && ( r.max == "*" || i <= r.max.toInt)

  // Returns true is i > Range.max
  def afterRange(i: Int, r: Range) = if( r.max == "*" ) false else i > r.max.toInt

  // Returns the instance number of the element
  def instance(e: Element) = e.instance

  // Implicit conversion from Value to String
  private implicit def asString(v: Value): String = v.asString
}
