package gen.model

import hl7.v2.profile.{Range, Usage}

/**
  * Trait representing a query-able element kind.
  * Possible kinds in case of HL7 v2x are: Group,
  * Segment and Datatype.
  */
trait QKind

/**
  * Class representing query-able properties of an element.
  */
case class QProps(kind: QKind, id: String, name : String, position: Int, instance: Int){
  assert( id.nonEmpty,   s"[Empty id] $this" )
  assert( name.nonEmpty, s"[Empty name] $this" )
  assert( position > 0 , s"[Invalid position] $this" )
  assert( instance > 0 , s"[Invalid instance] $this" )
}

/**
  * Class representing requirement of an element.
  */
case class Requirement (
    usage      : Usage,
    cardinality: Range,
    length     : Option[Range],
    table      : Option[String]
)

/**
  * Trait representing an element
  */
trait Element {

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

  assert( desc.nonEmpty, s"[Empty description] $this" )
  assert( path.nonEmpty, s"[Empty path] $this" )
  assert( line   == -1 || line   > 0, s"[Invalid line] $this" )
  assert( column == -1 || column > 0, s"[Invalid column] $this" )
}

/**
  * Trait representing a complex element.
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
    * @see hl7.v2.instance.Value
    */
  def value: Value
}
