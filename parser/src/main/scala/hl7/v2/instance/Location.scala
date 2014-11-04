package hl7.v2.instance

/**
  * Class representing the location of an element
  */
case class Location(desc: String, path: String, line: Int, column: Int)

/*
/**
  * The location companion object
  */
object Location {

  // The default location
  val default = Location("Unknown description", "Unknown path", line=1, column=1)
}*/