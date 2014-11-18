package hl7.v2.parser

import hl7.v2.instance.Message
import hl7.v2.profile.{ Message => MM }

import scala.util.Try

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */
trait Parser {

  /**
    * Parses the message and returns the message instance model
    * @param message - The message to be parsed
    * @param model   - The message model (profile)
    * @return The message instance model
    */
  def parse( message: String, model: MM ): Try[Message]
}
