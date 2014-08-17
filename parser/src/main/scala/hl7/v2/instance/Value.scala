package hl7.v2.instance


/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

sealed trait Value {
  def asString: String
}

case class Number(asString: String)   extends Value
case class Text(asString: String)     extends Value
case class Date(asString: String)     extends Value
case class Time(asString: String)     extends Value
case class DateTime(asString: String) extends Value

object Value {

  /**
    * Create the value from string depending on the data type
    */
  def apply(datatype: String, asString: String): Value = datatype match {
    case "NM"  => Number(asString)
    case "DT"  => Date(asString)
    case "TM"  => Time(asString)
    case "DTM" => DateTime(asString)
    case _     => Text(asString)
  }
}
