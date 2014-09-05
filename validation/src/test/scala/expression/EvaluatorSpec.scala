package expression

import org.specs2.Specification
import hl7.v2.instance.Simple
import hl7.v2.instance.Text
import hl7.v2.instance.Number
import hl7.v2.instance.Element
import hl7.v2.instance.Query.queryAsSimple

trait EvaluatorSpec extends Specification with Evaluator with PlainTextSpec with Mocks { def is = s2"""

  Expression evaluator specifications

    Given the following elements:

    ${ elementsDescription /* See ElementsMocks for details */ }

    PresenceSpec
      eval( Presence("2[1]"), c2 ) should pass"                                                   $pr1
      eval( Presence("2[2]"), c2 ) should fail wil the message "2[2] is missing"                  $pr2
      eval( Presence("2"), c2    ) should be inconclusive with the message "Invalid Path '2'"     $pr3
      eval( Presence("2[2]"), s0 ) should be inconclusive with the message "Unreachable Path ..." $pr4

    PlainTextSpec
      PlainText evaluation should succeed if the path is not populated"                           $plainTextPathNotPopulated
      PlainText evaluation should be inconclusive if the path is complex                          $plainTextPathComplex
      PlainText evaluation should be inconclusive if the path is invalid                          $plainTextPathInvalid
      PlainText evaluation should be inconclusive if the path is unreachable                      $plainTextPathUnreachable
      eval( PlainText(3[*], x, y ), c1 ) should pass for ( x in {S3, s3} and y=true ) and ( x=S3, y=false ) $plainTextPass
      eval( PlainText(3[*], XX, true/false ), c1 ) should fail                                    $pt6
      eval( PlainText(3[*], s3, true ), c1 ) should pass (case insensitive)                       $todo
      eval( PlainText(3[*], s3, false), c1 ) should fail (case sensitive)                         $todo
      
  """

  assert( c2.get(2, 1).nonEmpty )
  assert( c2.get(2, 2).isEmpty )
  assert( s0.isInstanceOf[Simple] )

  val `c1.2[*]`  = queryAsSimple(c1, "2[*]").get
  assert( `c1.2[*]`.length == 3 )
  assert( `c1.2[*]`(0).value == Number("21") )
  assert( `c1.2[*]`(1).value == Number("22") )
  assert( `c1.2[*]`(2).value == Number("23") )

  /*val `c1.3[*]`  = queryAsSimple(c1, "3[*]").get
  assert( `c1.3[*]`.length == 3 )
  assert( `c1.3[*]`(0).value == Text("S3") )
  assert( `c1.3[*]`(1).value == Text("S3") )
  assert( `c1.3[*]`(2).value == Text("S3") )
*/
  // Presence
  def pr1 = eval( Presence("2[1]"), c2 ) === Pass
  def pr2 = eval( Presence("2[2]"), c2 ) === fail( c2, "Path.2[2] is missing" )
  def pr3 = eval( Presence("2"), c2 )    === inconclusive( c2, Presence("2"), "Invalid Path '2'" )
  def pr4 = eval( Presence("2[2]"), s0 ) === inconclusive( s0, Presence("2[2]"), "Unreachable Path '2[2]'" )

  // ********************      PlainText test functions    **********************************************
  /*def pt1 = eval( PlainText("4[1]", "", true), c1 ) === Pass
  def pt2 = eval( PlainText("2[3]", "", true), c2 ) === 
    inconclusive(c2, PlainText("2[3]", "", true), "Path resolution returned at least one complex element")
  def pt3 = eval( PlainText("4", "", true), c1 )    === inconclusive(c1, PlainText("4", "", true), "Invalid Path '4'")
  def pt4 = eval( PlainText("4[1]", "", true), s0 ) === inconclusive(s0, PlainText("4[1]", "", true), "Unreachable Path '4[1]'")
  // This should pass because the value of c1.3[1], c1.3[3] and c1.3[3] is `S3'
  def pt5 = Seq( true, false ) map { b => eval( PlainText("3[*]", "S3", b ), c1 ) === Pass }
  // This will fail because the value of the resolved elements value is not `XX'
  def pt6 = Seq(true, false) map { b =>
    eval( PlainText("3[*]", "XX", b ), c1) === fail( `c1.3[*]` map { s => plainTextReason(PlainText("3[*]", "XX", b ), s) } )
  }
*/
  
  
  // This will pass because of case insensitivity
//  def pt6 = eval( PlainText("3[*]", "s3", true ), c1 ) === Pass

  // This will fail because of case sensitivity
//  def pt7 = {
//    val pt = PlainText("3[*]", "s3", false)
//    eval( pt, c1 ) === fail( `c1.3[*]`, plainTextReason( pt ) _ )
//  }

  
  
  //Helpers
 
  private def plainTextReason( e: PlainText, s: Simple ) = {
    val cs = if( e.ignoreCase ) "(case insensitive)" else "(case sensitive)"
    Reason(s.location, s"'${s.value.asString}' is different from '${e.text}' $cs")
  }
}