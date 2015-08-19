package edi.ncpdp.script.parser.impl

import hl7.v2.parser.impl.DefaultParser
import hl7.v2.instance._
import hl7.v2.profile.{Message => MM}

import scala.util.Try

/**
  * Default implementation of the parser
  *
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait DefaultNCPDPParser extends DefaultParser {

  /**
    * Parses the message and returns the message instance model
    * @param message - The message to be parsed
    * @param model   - The message model (profile)
    * @return The message instance model
    */
  override def parse( message: String, model: MM ): Try[Message] =
    NCPDPPreProcessor.process(message) map { t =>
      val PPR(valid, invalid, separators) = t
      implicit val s = separators
      val(children, unexpected) = processChildren( model.structure , valid)
      val tz: Option[TimeZone] = None //FIXME Get TZ from MSH.7
      val ils = invalid map ( x => Line( x._1, x._2 ) ) //FIXME Update PreProcessor to use Line
      val uls = unexpected map ( x => Line( x._1, x._2 ) ) //FIXME Update PreProcessor to use Line
      Message( model, children.reverse, ils, uls, tz, s )
    }

}
