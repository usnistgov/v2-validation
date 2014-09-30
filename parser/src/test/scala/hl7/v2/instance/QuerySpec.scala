package hl7.v2.instance

import hl7.v2.instance.Query._
import org.specs2.Specification

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

class QuerySpec extends Specification with Mocks { def is = s2"""

  Query Specification

  Given the following elements :

    ${elementsDescription /* See generic.Mocks for details */}

    A query should fail if the path is invalid                                                      $q1
    A query should succeed if the path is valid                                                     $q2
    Querying a simple element should fail with unreachable path error                               $q3
    Querying an element with no children should return an empty list                                $q4
    Querying a missing position (c1, "4[*]") should return an empty list                            $q5
    Querying c2 for the path 2[*] should return c0 and c1                                           $q6
    Querying c2 for the path 2[3] should return c1                                                  $q7
    Querying c2 for the path 2[*] and casting the result as list of `Simple' should return an error $q8
    Querying c2 for the path 4[1] and casting the result as list of `Simple' should succeed         $q9
"""

  def q1 = Seq("1", "1[a]", "0[1]", "1[2].", "1[2].a", "1[1]/2[2]" ) map { p => 
    query(c0, p) must beFailedTry.withThrowable[Error]( s"Invalid Path '\\Q${p}\\E'" ) 
  }

  def q2 = Seq("1[1]", "1[*]", "2[2].3[*].4[4]" ) map { p => query(c0, p) must beSuccessfulTry }

  def q3 = query(s0, "1[1].2[3]") must
    beFailedTry.withThrowable[Error]( "Unreachable Path '\\Q1[1].2[3]\\E'" )

  def q4 = query(c0, "1[1]") must beSuccessfulTry.withValue( ===(Seq[Element]()) )

  def q5 = query(c1, "4[*]") must beSuccessfulTry.withValue( ===(Seq[Element]()) )

  def q6 = query(c2, "2[*]") must beSuccessfulTry.withValue( ===(Seq[Element](c0, c1)) )

  def q7 = query(c2, "2[3]") must beSuccessfulTry.withValue( ===(Seq[Element]( c1 )) )

  def q8 = queryAsSimple(c2, "2[*]") must
    beFailedTry.withThrowable[Error]("Path resolution returned at least one complex element")

  def q9 = queryAsSimple(c2, "4[1]") must beSuccessfulTry.withValue( ===( Seq[Simple]( s0 ) ) )
}
