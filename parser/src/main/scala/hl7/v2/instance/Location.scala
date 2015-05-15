package hl7.v2.instance

/**
  * Class representing the location of an element
  */
case class Location(
    eType: EType,
    desc: String,
    path: String,
    line: Int,
    column: Int
  ) {

  val prettyString = s"$eType $path ($desc)"
}
