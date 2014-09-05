package expression

import org.specs2.Specification
import hl7.v2.instance.Simple
import hl7.v2.instance.Text
import hl7.v2.instance.Number
import hl7.v2.instance.Element
import hl7.v2.instance.Query.queryAsSimple

trait PlainTextSpec extends Specification with Evaluator  with Mocks { 
  
  /*
  PlainTextSpec
      PlainTextSpec
      PlainText evaluation should succeed if the path is not populated"                           $plainTextPathNotPopulated
      PlainText evaluation should be inconclusive if the path is complex                          $plainTextPathComplex
      PlainText evaluation should be inconclusive if the path is invalid                          $plainTextPathInvalid
      PlainText evaluation should be inconclusive if the path is unreachable                      $plainTextPathUnreachable
      eval( PlainText(3[*], S3, true ), c1 ) should pass (same value)                             $plainTextValueEqual
      eval( PlainText(3[*], S3/s3, false ), c1 ) should pass (case insensitive)                   $plainTextValueEqualCaseInsensitive

      PlainText evaluation should succeed if the path is not populated"                           $pt1
      PlainText evaluation should be inconclusive if the path is complex                          $pt2
      PlainText evaluation should be inconclusive if the path is invalid                          $pt3
      PlainText evaluation should be inconclusive if the path is unreachable                      $pt4
      eval( PlainText(3[*], S3, true/false ), c1 ) should pass (normal)                           $pt5
      eval( PlainText(3[*], XX, true/false ), c1 ) should fail                                    $pt6
      eval( PlainText(3[*], s3, true ), c1 ) should pass (case insensitive)                       $todo
      eval( PlainText(3[*], s3, false), c1 ) should fail (case sensitive)                         $todo
  */

  //c1.4[1] is not populated
  assert( queryAsSimple(c1, "4[1]").get == Nil )
  def plainTextPathNotPopulated = eval( PlainText("4[1]", "", true), c1 ) === Pass

  private val p2 = PlainText("2[3]", "", true)
  def plainTextPathComplex = eval( p2, c2 ) === inconclusive(c2, p2, "Path resolution returned at least one complex element")

  private val p3 = PlainText("4", "", true)
  def plainTextPathInvalid = eval( p3, c1 )    === inconclusive(c1, p3, s"Invalid Path '${p3.path}'")

  private val p4 = PlainText("4[1]", "", true)
  def plainTextPathUnreachable = eval( p4, s0 ) === inconclusive(s0, p4, s"Unreachable Path '${p4.path}'")


  private val `c1.3[*]`  = queryAsSimple(c1, "3[*]").get
  assert( `c1.3[*]`.length == 3 )
  assert( `c1.3[*]`(0).value == Text("S3") )
  assert( `c1.3[*]`(1).value == Text("S3") )
  assert( `c1.3[*]`(2).value == Text("S3") )

  //eval( PlainText(3[*], x, y ), c1 ) should pass for ( x in {S3, s3} and y=true ) and ( x=S3, y=false ) $plainTextPass
  def plainTextPass = (Seq( true -> Seq("S3", "s3"), false -> Seq("S3") ) map { t =>
    val(cs, values) = t
    values map { v => eval( PlainText("3[*]", v, cs ), c1 ) === Pass }
  }).flatten

  def plainTextFailValueNotEqual = todo

  def plainTextFailCaseInsensitive = todo

  
  def pt1 = eval( PlainText("4[1]", "", true), c1 ) === Pass
  def pt2 = eval( PlainText("2[3]", "", true), c2 ) === 
    inconclusive(c2, PlainText("2[3]", "", true), "Path resolution returned at least one complex element")
  def pt3 = eval( PlainText("4", "", true), c1 )    === inconclusive(c1, PlainText("4", "", true), "Invalid Path '4'")
  def pt4 = eval( PlainText("4[1]", "", true), s0 ) === inconclusive(s0, PlainText("4[1]", "", true), "Unreachable Path '4[1]'")
  
  // This should pass because the value of c1.3[1], c1.3[3] and c1.3[3] is `S3'
  def pt5 = Seq( true, false ) map { b => eval( PlainText("3[*]", "S3", b ), c1 ) === Pass }
  // This will fail because the value of the resolved elements value is not `XX'
  def pt6 = todo/*Seq(true, false) map { b =>
    eval( PlainText("3[*]", "XX", b ), c1) === fail( `c1.3[*]` map { s => plainTextReason(PlainText("3[*]", "XX", b ), s) } )
  }*/    
  def pt7 = todo
  def pt8 = todo
}