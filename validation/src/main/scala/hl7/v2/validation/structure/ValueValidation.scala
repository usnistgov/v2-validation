package hl7.v2.validation.structure

import gov.nist.validation.report.Entry
import hl7.v2.instance._
import hl7.v2.instance.util.ValueFormatCheckers._
import hl7.v2.profile._
import hl7.v2.profile.Usage
import hl7.v2.validation.report.ConfigurableDetections

object ValueValidation extends EscapeSeqHandler {

  def check(s: Simple)(implicit x: Separators, Detections: ConfigurableDetections): List[Entry] = {
    val optional = if(s.req.usage.equals(Usage.O)) Detections.ousage(s.location, s.value.raw) :: Nil else Nil
    checkValue(s.value, s.req.length, s.req.confRange, s.req.constantValue, s.location) ::: optional
  }

  /**
   * Checks the value format, length and presence of escape characters
   *
   * @param v  - The value to be checked
   * @param ln - The length constraint
   * @param constant  - Constant Value Constraint
   * @param l  - The location
   * @param s  - The separators
   * @return The list of problem found
   */
  def checkValue(v: Value, ln: Option[Range], conf: Option[Range], constant: Option[String], l: Location)(implicit s: Separators, Detections: ConfigurableDetections): List[Entry] =
    //No check if the value is Null and the location is a Field
    if (v.isNull && l.eType == EType.Field) {
        Nil
    } else {
      checkFormat(l, v).toList ::: checkLength(l, v, ln, conf).toList ::: checkConstantValue(l, v, constant).toList
    }

  def checkConstantValue(l: Location, v: Value, constant: Option[String])(implicit s: Separators, Detections: ConfigurableDetections): Option[Entry] = {
    constant match {
      case None => None
      case Some(constantValue) =>
        val raw = unescape(v.raw)
        if (raw.equals(constantValue)) None
        else Some(Detections.constantValue(l, constantValue, raw))
    }
  }

  /**
   * Checks the length and returns the error if any
   * @param l  - The location
   * @param v  - The value
   * @param ln - The length constraint
   * @param s  - The separators
   * @return The error if any or None
   */
  def checkLength(l: Location, v: Value, ln: Option[Range], conf: Option[Range])(implicit s: Separators, Detections: ConfigurableDetections): Option[Entry] =
    ln match {
      case Some(lr) => conf match {
        case Some(c) => Some(Detections.lengthSpecErrorXOR(l))
        case None => checkRange(l, v, ln)
      }
      case None => conf match {
        case Some(c) => checkRange(l, v, conf)
        case None => Some(Detections.lengthSpecErrorNF(l))
      }
    }

  def checkRange(l: Location, v: Value, r: Option[Range])(implicit s: Separators, Detections: ConfigurableDetections): Option[Entry] =
    r flatMap { range =>
      val raw = unescape(v.raw)
      if (range includes raw.length) None
      else Some(Detections.length(l, range, raw))
    }

  /**
   * Checks the format including the presence of separators in the value
   * @param l - The location
   * @param v - The value
   * @param s - The separators
   * @return The error if any or None
   */
  def checkFormat(l: Location, v: Value)(implicit s: Separators, Detections: ConfigurableDetections): Option[Entry] =
    v match {
      case Number(x) => checkNumber(x) map { m => Detections.format(l, m) }
      case Date(x) => checkDate(x) map { m => Detections.format(l, m) }
      case Time(x) => checkTime(x) map { m => Detections.format(l, m) }
      case DateTime(x) => checkDateTime(x) map { m => Detections.format(l, m) }
      case _ if containSeparators(v) && !l.path.equals("MSH-1") && !l.path.equals("MSH-2") => Some(Detections.unescaped(l))
      case _ => None
    }

  /**
   * Returns true if the value contain unescaped
   * field, component or sub-component separator
   */
  private def containSeparators(v: Value)(implicit s: Separators): Boolean =
    v.raw exists { c => c == s.fs || c == s.cs || c == s.ss || c == s.rs }
}
