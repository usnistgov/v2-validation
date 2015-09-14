package edi.ncpdp.script.parser.impl

import org.specs2._
import scala.util.Success
import scala.util.Failure

import NCPDPPreProcessor._

/**
 * This specification shows how to create examples using the "acceptance" style
 */
class NCPDPPreProcessorSpec extends Specification { def is = s2"""
 This is a specification to check the 'NCPDPPreProcessor' class
 
 The message 
   UNA:+./*'
   UIB+UNOA:0++MESSAGE_ID+++SENDER_ID:D+RECIPIENT_ID:P+20121012:101022'

 should 
   have 2 valid segments							 $e1
   have no invalid segments							 $e2

 The message 
   UNA:+./*'UIB+UNOA:0++MESSAGE_ID+++SENDER_ID:D+RECIPIENT_ID:P+20121012:101022'
 should 
   have 2 valid segments							 $e3
   have no invalid segments							 $e4

 The message 
   UNA./UIBUNOA09911234567890123D1234563P20100125081322
 should 
   have 2 valid segments							 $e5
   have no invalid segments							 $e6

 The message 
   UN:+./*'UIB+UNOA:0++MESSAGE_ID+++SENDER_ID:D+RECIPIENT_ID:P+20121012:101022'
 should 
   generate an exception							 $e7       

                                                     """
  val m =
    """/UNA:+./*'
      /UIB+UNOA:0++MESSAGE_ID+++SENDER_ID:D+RECIPIENT_ID:P+20121012:101022'
      /""".stripMargin('/')

  val m1 =
    """/UNA:+./*'UIB+UNOA:0++MESSAGE_ID+++SENDER_ID:D+RECIPIENT_ID:P+20121012:101022'""".stripMargin('/')   

  val m2 =
    """/UNA./UIBUNOA09911234567890123D1234563P20100125081322""".stripMargin('/')     

  val m3 =
    """/UNA+++/*'UIB+UNOA:0++MESSAGE_ID+++SENDER_ID:D+RECIPIENT_ID:P+20121012:101022'""".stripMargin('/')   

  def e1 = process(m)  match {
  	case Success(ps) => ps.valid.length must_== 3
  	case Failure(e) => throw e
  }

  def e2 = process(m)  match {
  	case Success(ps) => ps.invalid.length must_== 0
  	case Failure(e) => throw e
  }

  def e3 = process(m1)  match {
  	case Success(ps) => ps.valid.length must_== 2
  	case Failure(e) => throw e
  }

  def e4 = process(m1)  match {
  	case Success(ps) => ps.invalid.length must_== 0
  	case Failure(e) => throw e
  }
  	
  def e5 = process(m2)  match {
  	case Success(ps) => ps.valid.length must_== 2
  	case Failure(e) => throw e
  }

  def e6 = process(m2)  match {
  	case Success(ps) => ps.invalid.length must_== 0
  	case Failure(e) => throw e
  }

  def e7 = process(m3) must beFailedTry

}