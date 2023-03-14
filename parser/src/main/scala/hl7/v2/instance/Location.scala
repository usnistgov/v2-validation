package hl7.v2.instance

/**
  * Class representing the location of an element
  */
case class Location(
    eType: EType,
    desc: String,
    path: String,
    line: Int,
    column: Int,
    uidPath : String 
  ) {
  val prettyString = s"$eType $path ($desc)"

}

object Location {
  def apply(
    eType: EType,
    desc: String,
    path: String,
    line: Int,
    column: Int
  ) : Location = {
    Location(eType,desc,path,line,column,path)
  }
}


