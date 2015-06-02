package hl7.v2.validation.structure

import gov.nist.validation.report.Entry
import hl7.v2.instance._
import hl7.v2.instance.util.ValueFormatCheckers._
import hl7.v2.profile.Range
import hl7.v2.validation.report.Detections

object ValueValidation extends EscapeSeqHandler  {

  def check(s: Simple)(implicit x: Separators): List[Entry] =
    checkValue(s.value, s.req.length, s.location)

  /**
    * Checks the value format, length and presence of escape characters
    * @param v  - The value to be checked
    * @param lc - The length constraint
    * @param l  - The location
    * @param s  - The separators
    * @return The list of problem found
    */
  def checkValue(v: Value, lc: Option[Range], l: Location)
                (implicit s: Separators): List[Entry] =
    v.isNull match {
      case true  => Nil //No check if the value is Null
      case false =>checkFormat(l, v).toList ::: checkLength(l, v, lc).toList
    }

  /**
    * Checks the length and returns the error if any
    * @param l  - The location
    * @param v  - The value
    * @param lc - The length constraint
    * @param s  - The separators
    * @return The error if any or None
    */
  def checkLength(l: Location, v: Value, lc: Option[Range])
                 (implicit s: Separators): Option[Entry] =
    lc flatMap { range =>
      val raw = unescape( v.raw )
      if (range includes raw.length) None
      else Some( Detections.length(l, range, raw))
    }

  /**
    * Checks the format including the presence of separators in the value
    * @param l - The location
    * @param v - The value
    * @param s - The separators
    * @return The error if any or None
    */
  def checkFormat(l: Location, v: Value)(implicit s: Separators): Option[Entry] =
    v match {
      case Number(x)   => checkNumber(x)   map { m => Detections.format(l, m) }
      case Date(x)     => checkDate(x)     map { m => Detections.format(l, m) }
      case Time(x)     => checkTime(x)     map { m => Detections.format(l, m) }
      case DateTime(x) => checkDateTime(x) map { m => Detections.format(l, m) }
      case _ if containSeparators(v) => Some(  Detections.unescaped(l) )
      case _ => None
    }

  /**
    * Returns true if the value contain unescaped
    * field, component or sub-component separator
    */
  private def containSeparators(v: Value)(implicit s: Separators): Boolean =
    v.raw exists { c => c == s.fs || c == s.cs || c == s.ss || c == s.rs }
}
