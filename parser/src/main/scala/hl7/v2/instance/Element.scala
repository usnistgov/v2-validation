package hl7.v2.instance

import hl7.v2.profile.Req

/**
  * Trait representing an element
  */
trait Element {

  /**
    * The location of the element
    */
  def location: Location

  /**
    * The position of the element
    */
  def position: Int

  /**
    * The instance number of the element
    */
  def instance: Int
}

/**
  * Trait representing a complex element.
  */
trait Complex extends Element {

  /**
    * Returns true if the complex element has extra children
    */
  def hasExtra: Boolean

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
  //FIXME: This makes element tied to the profile which is probably not necessary
  def reqs: List[Req]
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
