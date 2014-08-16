package hl7.v2.instance

import hl7.v2.profile.{Component => CM}
import hl7.v2.profile.{Field => FM}
import hl7.v2.profile.{Group => GM}
import hl7.v2.profile.{Message => MM}
import hl7.v2.profile.{SegmentRef => SM}

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

sealed trait Component extends Element {
  def model: CM
  def position = model.position
  def instance = 1
}

case class SimpleComponent(
    model: CM,
    value: Value,
    location: Location
) extends Component with Simple

case class ComplexComponent(
    model: CM,
    components: List[Option[SimpleComponent]],
    location: Location
  ) extends Component with Complex {
  def children = components.flatten
}

sealed trait Field extends Element {
  def model: FM
  def position = model.position
}

case class SimpleField(
    model: FM,
    value: Value,
    instance: Int,
    location: Location
) extends Field with Simple

case class ComplexField(
    model: FM,
    components: List[Option[Component]],
    instance: Int,
    location: Location
  ) extends Field with Complex {
  def children = components.flatten
}

case class Segment(
    model: SM,
    fields: List[List[Field]],
    instance: Int,
    location: Location
  ) extends Complex {

  def position = model.position
  lazy val children = fields.flatten
}

case class Group(
    model: GM,
    structure: List[Either[List[Segment], List[Group]]],
    instance: Int
  ) extends Complex {

  def position = model.position
  def location = structure.head match {
    case Left (ls) => ls.head.location.copy(path = model.name)
    case Right(lg) => lg.head.location.copy(path = model.name)
  }
  def children = structure flatMap { _ match { case Left(ls) => ls case Right(lg) => lg } } //FIXME: use Lazy val 
}

case class Message(
    model: MM,
    structure: List[Either[List[Segment], List[Group]]]
  ) extends Complex {

  def position = 1
  def instance = 1
  def location = structure.head match {
    case Left (ls) => ls.head.location.copy(path = model.name)
    case Right(lg) => lg.head.location.copy(path = model.name)
  }
  lazy val children = structure flatMap { _ match { case Left(ls) => ls case Right(lg) => lg } }
}
