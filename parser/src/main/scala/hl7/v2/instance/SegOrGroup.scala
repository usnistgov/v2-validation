package hl7.v2.instance

import hl7.v2.profile.SegRefOrGroup

/**
  * Trait representing either a segment or a group
  */
trait SegOrGroup extends Complex {
  def model: SegRefOrGroup
  def req  = model.req
  def reqs = model.reqs
}
