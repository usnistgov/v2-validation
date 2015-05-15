/*package hl7.v2.validation.report
package extension

/**
  * Provides function to convert a report to Json
  */
object ReportAsJson {

  /**
    * Creates and returns a Json string from the validation report
    * @param r - The validation report
    * @return The Json string
    */
  def toJson(r: Report): String = {
    val structure = r.structure map ( x => x.toJson ) mkString("[", ",", "]")
    //val content   = r.content   map CEntryAsJson.toJson mkString("[", ",", "]")
    val content   = r.content map {
      case x: Failure          => x.toJson
      case x: PredicateFailure => x.toJson
      case _                   => "" //FIXME
    } mkString("[", ",", "]")

    val vs        = r.vs       map VSEntryAsJson.toJson mkString("[", ",", "]")
    s"""{"structure":$structure,"content":$content, "valueSet":$vs}"""
  }

}
*/