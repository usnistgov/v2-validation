package hl7.v2.validation.vs

import java.util.{Arrays => JArrays, List => JList}

import gov.nist.validation.report.{Entry, Trace => GTrace}
import hl7.v2.instance.Query.queryAsSimple
import hl7.v2.instance.{Complex, Simple}
import hl7.v2.profile.BindingLocation._
import hl7.v2.profile.{Datatype, ValueSetSpec}
import hl7.v2.validation.report.Detections

import scala.collection.JavaConversions.seqAsJavaList
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
     For each value specification
        1) Check if value set not excluded from the validation
        2) Check if the value set is in the library
        3) Check if the value set is empty
        4) Check if binding location is present
        2) Check if the code is in the value set
        3) If the code is in the value set
            a) If the usage is E then EVS
            b) If the usage is P then PVS
            c) If the code system is different from the value
               at position + 2 then InvalidCodeSystem
   */
  /**
   * Checks the coded element and return the result
   */
  def checkCodedElement(c: Complex, lib: ValueSetLibrary): List[Entry] =
  //FIXME require( isCodedElement(...)
    (c.req.vsSpec map { spec =>
      val id = spec.valueSetId
      lib skipValidation id match {
        case true  => Detections.vsNoVal(c.location, id) :: Nil
        case false =>
          lib get id match {
            case None =>
              val msg = s"Value set '$id' cannot be found in the library"
              Detections.codedElem(c.location, msg, null, spec, null) :: Nil
            case Some(vs) =>
              vs.codes.isEmpty match {
                case true  => Detections.emptyVS(c.location, vs, spec) :: Nil
                case false => checkCE(c, vs, spec)
              }
          }
      }
    }).flatten

  private def checkCE(c: Complex, vs: ValueSet, spec: ValueSetSpec): List[Entry] =
    spec.bindingLocation match {
      case None =>
        Detections.vsError(c.location, "The binding location is missing", vs, spec) :: Nil
      case Some(bl) =>
        bl match {
          case Position(p) => checkPosition(c, p, vs, spec)
          case OR(p1, p2)  => checkOR(c, p1, p2, vs, spec)
          case XOR(p1, p2) => checkXOR(c, p1, p2, vs, spec)
          case AND(p1, p2) => checkAND(c, p1, p2, vs, spec)
          case NBL(p, sbl) => ??? //FIXME Not supported for now
        }
    }

  private def checkOR(c: Complex, p1: Int, p2: Int, vs: ValueSet,
                      spec: ValueSetSpec): List[Entry] =
    checkPosition(c, p1, vs, spec) match {
      case Nil => Nil
      case xs1 => checkPosition(c, p2, vs, spec) match {
        case Nil => Nil
        case xs2 => orError(c, vs, spec, xs1 ::: xs2)
      }
    }

  private def checkAND(c: Complex, p1: Int, p2: Int, vs: ValueSet,
                       spec: ValueSetSpec): List[Entry] =
    checkPosition(c, p1, vs, spec) match {
      case Nil =>
        checkPosition(c, p2, vs, spec) match {
          case Nil => Nil
          case xs => andError(c, vs, spec, xs)
        }
      case xs => andError(c, vs, spec, xs)
    }

  private def checkXOR(c: Complex, p1: Int, p2: Int, vs: ValueSet,
                       spec: ValueSetSpec): List[Entry] =
    (checkPosition(c, p1, vs, spec), checkPosition(c, p1, vs, spec)) match {
      case (Nil, Nil) => xorError(c, vs, spec, Nil)
      case (_, Nil) => Nil
      case (Nil, _) => Nil
      case (xs1, xs2) => xorError(c, vs, spec, Nil)
    }

  private def checkPosition(c: Complex, p: Int, vs: ValueSet,
                            spec: ValueSetSpec): List[Entry] =
    resolve(c, p) match {
      case Failure(e) => Detections.vsError(c.location, e.getMessage, vs, spec) :: Nil
      case Success(s1) if s1.value.isNull => Nil
      case Success(s1) =>
        resolve(c, p + 2) match {
          case Failure(e) =>
            val msg = "Code system checking cannot be performed. " +
              s"Reason: Querying the code system failed. Details: ${e.getMessage}"
            Detections.codedElem(c.location, msg, vs, spec, null) :: Nil
          case Success(s2) => checkTriplet(s1, s2, vs, spec)
        }
    }

  private def resolve(c: Complex, p: Int): Try[Simple] =
    queryAsSimple(c, s"$p[1]") match {
      case Success(x :: Nil) => Success(x)
      case Failure(e) => Failure(new Exception(queryErrMsg(e)))
      case Success(l) =>
        Failure(new Exception(s"The query returned ${l.size} element(s)"))
    }

  /**
   * Check the triplet and return the list of problem
   */
  private
  def checkTriplet(s1: Simple, s2: Simple, vs: ValueSet, spec: ValueSetSpec) =
    check(s1.location, s1.value, vs, spec) match {
      case Nil =>
        val code = vs.codes.find(_.value == s1.value.raw).get
        checkCodeSys(s2, code, vs, spec)
      case xs => xs
    }

  /**
   * Check the code system
   */
  private def checkCodeSys(s: Simple, c: Code, vs: ValueSet,
                           spec: ValueSetSpec): List[Entry] =
    s.value.raw == c.codeSys match {
      case true => Nil
      case false =>
        val m = s"Invalid Code System. Expected: '${c.codeSys}', Found: '${s.value.raw}'"
        Detections.codedElem(s.location, m, vs, spec, null) :: Nil
    }

  private def queryErrMsg(m: String): String =
    s"An error occurred while resolving the biding location. Detail: $m"

  private def queryErrMsg(e: Throwable): String = queryErrMsg(e.getMessage)

  private
  def orError(c: Complex, vs: ValueSet, spec: ValueSetSpec, l: List[Entry]) = {
    val m = s"At least one triplet should be valued from the value set '${
      spec.valueSetId
    }'"
    Detections.codedElem(c.location, m, vs, spec, stackTrace(l)) :: Nil
  }

  private
  def andError(c: Complex, vs: ValueSet, spec: ValueSetSpec, l: List[Entry]) = {
    val m = s"Both triplets should be valued from the value set '${
      spec.valueSetId
    }'"
    Detections.codedElem(c.location, m, vs, spec, stackTrace(l)) :: Nil
  }

  private
  def xorError(c: Complex, vs: ValueSet, spec: ValueSetSpec, l: List[Entry]) = {
    val m = s"One of the triplet (but not both) should be valued from the" +
      s" value set '${spec.valueSetId}'"
    Detections.codedElem(c.location, m, vs, spec, stackTrace(l)) :: Nil
  }

  private def stackTrace(vsErrorStack: List[Entry]): JList[GTrace] =
    seqAsJavaList(vsErrorStack map { vsError =>
      val m = s"[${vsError.getLine}, ${vsError.getColumn}] ${vsError.getDescription}"
      new GTrace("VS Error", JArrays.asList(m))
    })
}
