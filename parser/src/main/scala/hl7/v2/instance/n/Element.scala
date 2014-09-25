package hl7.v2.instance.n

/**
  * Trait representing the kind of an element.
  * Possible kinds are: Group, Segment, Field,
  * Component and SubComponent.
  */
sealed trait EKind
object Group        extends EKind with QKind
object Segment      extends EKind with QKind
object Field        extends EKind
object Component    extends EKind
object SubComponent extends EKind

/**
  * Trait representing a query-able element kind.
  * Possible kinds are: Group, Segment and Datatype.
  */
sealed trait QKind
object Datatype extends QKind

/**
  * Class representing query-able properties of an element.
  */
case class QProps(
    kind : QKind,
    id   : String,
    name : String,
    position: Int,
    instance: Int
)

/**
  * Class representing requirement of an element.
  */
case class Requirement (
    usage      : String,
    cardinality: String,
    length     : Option[String],
    table      : Option[String]
)

/**
  * Trait representing an element
  */
trait Element {
  /**
    * The kind of the element.
    * @see hl7.v2.instance.EKind
    */
  def kind: EKind

  /**
    * The description of the element
    */
  def desc: String

  /**
    * The path of the element
    */
  def path: String

  /**
    * The line number of the element
    */
  def line: Int

  /**
    * The column number of the element
    */
  def column: Int

  /**
    * The query-able properties of the element
    * @see hl7.v2.instance.QProps
   */
  def qProps: QProps
}

/**
  * Trait representing a complex element
  */
trait Complex extends Element {
  /**
    * The children of the complex element
    */
  def children: List[Element]

  /**
    * The requirements of the children. The requirements are
    * sorted by the position of the child element. The head
    * of the list is the requirement for the first position
    * and the last, the requirement for the last position.
    *
    * An empty list of requirements should be interpreted
    * as no child is allowed for the complex element.
    *
    * If there is a child which instance number is greater
    * than the size of the list of requirements than this
    * complex has extra children.
    */
  def reqs: List[Requirement]
}

/**
  * Trait representing a simple element.
  */
trait Simple extends Element {
  /**
    * The value of the simple element
    */
  def value: Value
}

trait Value