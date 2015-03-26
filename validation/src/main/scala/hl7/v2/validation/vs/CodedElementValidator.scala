package hl7.v2.validation.vs

import hl7.v2.instance.Query.queryAsSimple
import hl7.v2.instance.{Complex, Simple}
import hl7.v2.profile.BindingLocation._
import hl7.v2.profile.{Datatype, ValueSetSpec}
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

  /**
   * Checks the coded element and return the result
   */
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
  def checkCodedElement(c: Complex, lib: Map[String, ValueSet]): List[VSEntry] =
  //FIXME require( isCodedElement(...) )
    c.req.vsSpec match {
      case Nil => Nil
      case x :: Nil => checkCE(c, x, lib)
      case xs => VSSpecError(c.location, moreThanOneVSSpec(xs)) :: Nil
    }

  private def checkCE(c: Complex, s: ValueSetSpec
                      , lib: Map[String, ValueSet]): List[VSEntry] =
    s.bindingLocation match {
      case None => VSSpecError(c.location, noBLMsg(s)) :: Nil
      case Some(bl) =>
        bl match {
          case Position(p) => checkPosition(c, p, s, lib)
          case OR(p1, p2)  => checkOR (c, p1, p2, s, lib)
          case XOR(p1, p2) => checkXOR(c, p1, p2, s, lib)
          case AND(p1, p2) => checkAND(c, p1, p2, s, lib)
          case NBL(p, bl)  => ??? //TODO
        }
    }

  private def checkOR(c: Complex, p1: Int, p2: Int, spec: ValueSetSpec,
                      lib: Map[String, ValueSet]): List[VSEntry] =
    checkPosition(c, p1, spec, lib) match {
      case Nil   => Nil
      case xs1   => checkPosition(c, p2, spec, lib) match {
        case Nil => Nil
        case xs2 =>
          val id = spec.valueSetId
          val m = s"At least on triplet should be valued from the value set $id"
          CodedElement(c.location, m, xs1 ::: xs2) :: Nil
      }
    }

  private def checkXOR(c: Complex, p1: Int, p2: Int, spec: ValueSetSpec,
                      lib: Map[String, ValueSet]): List[VSEntry] = ???

  private def checkAND(c: Complex, p1: Int, p2: Int, spec: ValueSetSpec,
                      lib: Map[String, ValueSet]): List[VSEntry] = ???

  private def checkPosition(c: Complex, p: Int, spec: ValueSetSpec
                            , lib: Map[String, ValueSet]): List[VSEntry] =
    resolve(c, p, spec) match {
      case Failure(e) => queryVSSpecErr(c, spec, e) :: Nil
      case Success((s1, s2)) => s1.value.isNull match {
        case true => Nil
        case false =>
          val id = spec.valueSetId
          val bs = spec.bindingStrength
          lib get id match {
            case None => VSNotFound(c.location, s1.value.raw, id, bs) :: Nil
            case Some(vs) if vs.codes.isEmpty => EmptyVS(c.location, vs) :: Nil
            case Some(vs) => checkTriplet(s1, s2, vs, bs)
          }
      }
    }

  private def resolve(c: Complex, p: Int,
                      spec: ValueSetSpec): Try[(Simple, Simple)] =
    (queryAsSimple(c, s"$p[1]"), queryAsSimple(c, s"${p + 2}[1]")) match {
      case (Failure(e), _) => Failure(new Exception(queryErrMsg(spec, c, p, e)))
      case (_, Failure(e)) => Failure(new Exception(queryErrMsg(spec, c, p, e)))
      case (Success(s1 :: Nil), Success(s2 :: Nil)) => Success(s1, s2)
      case _ => Failure(new Exception(queryMultipleResMsg(c, p)))
    }

  private def checkTriplet(s1: Simple, s2: Simple,
                           vs: ValueSet, obs: OBS): List[VSEntry] =
    check(s1.location, s1.value, vs, obs) match {
      case Nil => checkCodeSys(s2, vs.codes.find(_.value == s1.value.raw).get)
      case xs => xs
    }

  private def checkCodeSys(s: Simple, c: Code): List[VSEntry] =
    s.value.raw == c.codeSys match {
      case true  => Nil
      case false =>
        val m = s"Invalid code system. Expected: ${c.codeSys}, Found: ${s.value.raw}"
        CodedElement(s.location, m, Nil) :: Nil
    }

  private def moreThanOneVSSpec(l: List[ValueSetSpec]) =
    s"More than one value set specification found. ${l mkString("{", ", ", "}")}"

  private def noBLMsg(s: ValueSetSpec) =
    s"Invalid value set specification $s. Reason: no binding location is defined"

  private def queryVSSpecErr(c: Complex, s: ValueSetSpec, e: Throwable) = {
    val m = s"Invalid value set specification $s. Reason: ${e.getMessage}"
    VSSpecError(c.location, m)
  }

  private def queryErrMsg(s: ValueSetSpec, c: Complex, p: Int, e: Throwable) =
    s"Querying ${desc(c)} for the position $p failed. Detail: ${e.getMessage}"

  private def desc(c: Complex) = s"${c.location.path}(${c.location.desc})"

  private def queryMultipleResMsg(c: Complex, p: Int) =
    s"Querying ${desc(c)} for the position $p or ${p + 2
    } returned more than one element."
}
