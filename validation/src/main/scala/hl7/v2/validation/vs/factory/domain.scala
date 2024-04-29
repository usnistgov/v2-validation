package hl7.v2.validation.vs.factory

import hl7.v2.validation.vs.{Code, ValueSet, ValueSetLibrary, ValueSetNotFoundException}

import java.lang
import java.util.{List => JList}

abstract class ValueSetFacade {
  def getValueSet: ValueSet
  @throws(classOf[Exception])
  def getCodeMatches(value: String): JList[Code]
  @throws(classOf[Exception])
  def contains(value: String): Boolean
  @throws(classOf[Exception])
  def getCodeList: JList[Code]
}

abstract class ValueSetFactory() extends ValueSetLibrary {
  def isExcludedFromValidation(vsId: String): Boolean
  @throws(classOf[Exception])
  def getValueSetById(vsId: String): ValueSetFacade
}

object EmptyValueSetFactory extends ValueSetFactory {
  override def isExcludedFromValidation(vsId: String): Boolean = false
  override def getValueSetById(vsId: String): ValueSetFacade = throw new ValueSetNotFoundException(vsId);
  /**
   * Returns true if the value set with the specified
   * id is excluded from the validation
   *
   * @param id - The value set if
   * @return True if the value set is excluded from the validation
   */
  override def isExcludedFromTheValidation(id: String): lang.Boolean = ???

  /**
   * Returns the value set with the specified id
   *
   * @param id - The id of the value set
   * @return The value set with the specified id
   * @throws ValueSetNotFoundException if the value set is not in the library
   */
  override def get(id: String): ValueSet = throw new ValueSetNotFoundException(id);

  override def containsLegacy0396Codes(): Boolean = ???
}