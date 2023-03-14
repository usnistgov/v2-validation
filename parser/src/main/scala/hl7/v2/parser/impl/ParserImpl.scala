package hl7.v2.parser.impl
import hl7.v2.instance._
import hl7.v2.profile.{Message => MM}

class ParserImpl extends DefaultParser {

  def jparse(message: String, model: MM): Message = this.parse(message, model).get

}
