/*package hl7.v2.validation.report

import hl7.v2.instance.Location
import hl7.v2.profile.{ValueSetSpec, BindingStrength}
import hl7.v2.validation.vs.ValueSet

import Configurations.{Templates, Categories, Classifications}

trait VSEntry extends Entry

case class EVS (
    location: Location,
    value: String,
    valueSet: ValueSet,
    bindingStrength: Option[BindingStrength]
  ) extends VSEntry {

  override def msg: String = String.format(Templates.EVS, value, location.prettyString, valueSet.id)
  override def category: String = Categories.EVS
  override def classification: String = Classifications.EVS
}

case class PVS (
    location: Location,
    value: String,
    valueSet: ValueSet,
    bindingStrength: Option[BindingStrength]
  ) extends VSEntry {

  override def msg: String = String.format(Templates.PVS, value, location.prettyString, valueSet.id)
  override def category: String = Categories.PVS
  override def classification: String = Classifications.PVS
}

case class CodeNotFound(
    location: Location,
    value: String,
    valueSet: ValueSet,
    bindingStrength: Option[BindingStrength]
  ) extends VSEntry {

  override def msg: String = String.format(Templates.CodeNotFound, value, location.prettyString, valueSet.id)
  override def category: String = Categories.CodeNotFound
  override def classification: String = Classifications.CodeNotFound
}

case class VSNotFound(
    location: Location,
    value: String,
    valueSetId: String,
    bindingStrength: Option[BindingStrength]
  ) extends VSEntry {

  override def msg: String = String.format(Templates.VSNotFound, value, location.prettyString, valueSetId)
  override def category: String = Categories.VSNotFound
  override def classification: String = Classifications.VSNotFound
}

case class EmptyVS(
    location: Location,
    valueSet: ValueSet,
    bindingStrength: Option[BindingStrength]
  ) extends VSEntry {

  override def msg: String = String.format(Templates.EmptyVS, valueSet.id)
  override def category: String = Categories.EmptyVS
  override def classification: String = Classifications.EmptyVS
}

case class VSError(
    location: Location,
    valueSet: ValueSet,
    reason: String
  ) extends VSEntry {

  override def msg: String = String.format(Templates.VSError, reason)
  override def category: String = Categories.VSError
  override def classification: String = Classifications.VSError
}

case class VSSpecError(
    location: Location,
    valueSet: Option[ValueSet],
    spec: ValueSetSpec,
    msg: String
  ) extends VSEntry {

  //FIXME override def msg: String = String.format(Templates.VSError, msg)
  override def category: String = Categories.VSError
  override def classification: String = Classifications.VSError
}

case class CodedElem(
    location: Location,
    spec: ValueSetSpec,
    valueSet: Option[ValueSet],
    msg: String,
    details: List[VSEntry] //FIXME
  ) extends VSEntry {

  //FIXME override def msg: String = String.format(Templates.CodedElement, reason)
  override def category: String = Categories.CodedElement
  override def classification: String = Classifications.CodedElement
}

case class NoVal(location: Location, valueSetId: String) extends VSEntry {

  override def msg: String = String.format(Templates.NoVal, valueSetId)
  override def category: String = Categories.NoVal
  override def classification: String = Classifications.NoVal
}
*/