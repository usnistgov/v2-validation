package hl7.v2.validation.vs

import hl7.v2.instance.Message
import scala.concurrent.Future

/**
  * Trait defining the value set validator
  */

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Validator {

  def checkValueSet(m: Message): Future[Seq[Entry]]
}
