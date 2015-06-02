/*package hl7.v2.validation.vs

import hl7.v2.instance.Query.queryAsSimple
import hl7.v2.instance.{Complex, Location, Simple}
import hl7.v2.profile.BindingLocation._
import hl7.v2.profile.{BindingLocation, Datatype, ValueSetSpec}
import hl7.v2.validation.report._

import scala.util.{Failure, Success, Try}

trait CodedElementValidator extends DefaultSimpleElemValidator {

  /**
    * Returns true if the data type is an HL7 coded element
    */
  def isCodedElement(d: Datatype): Boolean = isCodedElement(d.name)

  /**
    * Returns true if the data type is an HL7 coded element
    */
  def isCodedElement(d: String): Boolean = d matches "C(W|N)?E"

  /*
  Preconditions
    1) There is a table spec
    2) The complex element is a coded element

  Checks:
     For each valued binding location (If not valued usage will trigger)
        1) If the value set is not in the library then VSNotFound
        2) If the code is not in the value set then CodeNotFound
        3) If the code is in the value set
            a) If the usage is E then EVS
            b) If the usage is P then PVS
            c) If the code system is different from the value
               at position + 2 then InvalidCodeSystem
   */
  /**
    * Checks the coded element and return the result
    */
  def checkCodedElement(c: Complex, lib: ValueSetLibrary): List[VSEntry] =
    //FIXME require( isCodedElement(...)
    (c.req.vsSpec map { x =>
      val id = x.valueSetId
      if( lib skipValidation id ) NoVal(c.location, id) :: Nil
      else checkCE(c, getValueSet(id, lib), x) }
    ).flatten

  // Type Aliases
  private type OVS = Option[ValueSet]
  private type OBL = Option[BindingLocation]
  private type VSE = Either[ValueSet, String]

  private def checkCE(c: Complex, vse: VSE, spec: ValueSetSpec): List[VSEntry] =
    spec.bindingLocation match {
      case None     => bindingLocationMissing(c.location, vse, spec)
      case Some(bl) =>
        bl match {
          case Position(p) => checkPosition(c, p, vse, spec)
          case OR(p1, p2)  =>  checkOR(c, p1, p2, vse, spec)
          case XOR(p1, p2) => checkXOR(c, p1, p2, vse, spec)
          case AND(p1, p2) => checkAND(c, p1, p2, vse, spec)
          case NBL(p, sbl) => ??? //FIXME Not supported for now
        }
    }

  private def checkOR(c: Complex, p1: Int, p2: Int, vse: VSE,
                      spec: ValueSetSpec): List[VSEntry] =
    checkPosition(c, p1, vse, spec) match {
      case Nil => Nil
      case xs1 => checkPosition(c, p2, vse, spec) match {
        case Nil => Nil
        case xs2 => orError(c, spec, xs1 ::: xs2)
      }
    }

  private def checkAND(c: Complex, p1: Int, p2: Int, vse: VSE,
                      spec: ValueSetSpec): List[VSEntry] =
    checkPosition(c, p1, vse, spec) match {
      case Nil =>
        checkPosition(c, p2, vse, spec) match {
          case Nil => Nil
          case xs  => andError(c, spec, xs)
        }
      case xs => andError(c, spec, xs)
    }

  private def checkXOR(c: Complex, p1: Int, p2: Int, vse: VSE,
                       spec: ValueSetSpec): List[VSEntry] =
    (checkPosition(c, p1, vse, spec), checkPosition(c, p1, vse, spec)) match {
      case (Nil, Nil) => xorError(c, spec, Nil)
      case (_  , Nil) => Nil
      case (Nil, _  ) => Nil
      case (xs1, xs2) => xorError(c, spec, Nil)
    }

  private def checkPosition(c: Complex, p: Int, vse: VSE,
                            spec: ValueSetSpec): List[VSEntry] =
    resolve(c, p) match {
      case Failure(e)  => vsSpecErr(c, vse, spec, e)
      case Success(s1) if s1.value.isNull => Nil
      case Success(s1) =>
        resolve(c, p + 2) match {
          case Failure(e)  =>
            val msg = "Code system checking cannot be performed. " +
              s"Reason: Querying the code system failed. Details: ${e.getMessage}"
            CodedElem(c.location, spec, None, msg, Nil) :: Nil
          case Success(s2) => checkTriplet(s1, s2, vse, spec)
        }
    }

  private def resolve(c: Complex, p: Int): Try[Simple] =
    queryAsSimple(c, s"$p[1]") match {
      case Success(x :: Nil ) => Success(x)
      case Failure(e)         => Failure( new Exception(queryErrMsg(e)) )
      case Success(l)         =>
        Failure( new Exception(s"The query returned ${l.size} element(s)") )
    }

  /**
    * Check the triplet and return the list of problem
    */
  private
  def checkTriplet(s1: Simple, s2: Simple, vse: VSE, spec: ValueSetSpec) =
    vse match {
      case Right(m) => CodedElem(s1.location, spec, None, m, Nil) :: Nil
      case Left(vs) =>
        check(s1.location, s1.value, vs, spec.bindingStrength) match {
          case Nil =>
            val code = vs.codes.find(_.value == s1.value.raw).get
            checkCodeSys(s2, code, vse, spec)
          case xs  => xs
        }
    }

  /**
    * Check the code system
    */
  private def checkCodeSys(s: Simple, c: Code,
                           vse: VSE, spec: ValueSetSpec): List[VSEntry] =
    s.value.raw == c.codeSys match {
      case true  => Nil
      case false =>
        val m = s"Invalid Code System. Expected: '${c.codeSys}', Found: '${s.value.raw}'"
        CodedElem(s.location, spec, None, m, Nil) :: Nil
    }

  /**
    * Returns the value set and the binding strength or an error message
    */
  private
  def getValueSet(id: String, lib: ValueSetLibrary): VSE =
    lib get id match {
      case None    => Right(s"Value set '$id' cannot be found in the library")
      case Some(x) =>
        if( x.codes.isEmpty ) Right(s"Value set '$id' is empty") else Left(x)
    }

  /**
    * Return the detection for missing binding location
    */
  private
  def bindingLocationMissing(l: Location, vse: VSE, s: ValueSetSpec) = {
    val ovs = vse match { case Left(vs) => Some(vs) case _ => None }
    VSSpecError(l, ovs, s, "The binding location is missing") :: Nil
  }

  /**
    * Returns a VSSpecError
    */
  private def vsSpecErr(c: Complex, vse: VSE, s: ValueSetSpec, e: Throwable) = {
    val ovs = vse match { case Left(vs) => Some(vs) case _ => None }
    VSSpecError(c.location, ovs, s, e.getMessage) :: Nil
  }

  private def queryErrMsg(m: String) : String =
    s"An error occurred while resolving the biding location. Detail: $m"

  private def queryErrMsg(e: Throwable): String = queryErrMsg(e.getMessage)

  private def orError(c: Complex, spec: ValueSetSpec, l: List[VSEntry]) = {
    val m = s"At least one triplet should be valued from the value set '${
              spec.valueSetId}'"
    CodedElem(c.location, spec, None, m, l) :: Nil
  }

  private def andError(c: Complex, spec: ValueSetSpec, l: List[VSEntry]) = {
    val m = s"Both triplets should be valued from the value set '${
              spec.valueSetId}'"
    CodedElem(c.location, spec, None, m, l) :: Nil
  }

  private def xorError(c: Complex, spec: ValueSetSpec, l: List[VSEntry]) = {
    val m = s"One of the triplet (but not both) should be valued from the" +
            s" value set '${spec.valueSetId}'"
    CodedElem(c.location, spec, None, m, l) :: Nil
  }
}
*/