package expression
	
	import expression.EvalResult.Pass
	import hl7.v2.instance.{Element, Query}
	import Query.query
	import org.specs2.Specification
	
	import scala.util.{Failure, Success}
	import expression.EvalResult.Fail
	import expression.EvalResult.Inconclusive
	
	trait StringFormatSpec extends Specification with Evaluator with Mocks {
	
	  /*
	      StringFormatSpec
          Expression should fail if a LOINC string is invalid $stringFormatLOINCInvalid
          Expression should pass if a LOINC string is valid $stringFormatLOINCvalid
          Expression should fail if a SNOMED string is invalid $stringFormatSNOMEDInvalid
          Expression should pass if a SNOMED string is valid $stringFormatSNOMEDvalid
          Expression should be inconclusive if a string format is unrecognized $stringFormatUnknown
	  */
	
	  def stringFormatLOINCvalid = {
	    val p = StringFormat(".","LOINC",true)
	    eval( p, s_LOINC) === Pass
	  }
	  
	  def stringFormatSNOMEDvalid = {
	    val p = StringFormat(".","SNOMED",true)
	    eval( p, s_SNOMED) === Pass
	  }
	
	  def stringFormatLOINCInvalid = {
	    val p = StringFormat(".","LOINC",true)
	    eval( p, s0).isInstanceOf[Fail]
	  }
	  
	  def stringFormatSNOMEDInvalid = {
	    val p = StringFormat(".","SNOMED",true)
	    eval( p, s0).isInstanceOf[Fail]
	  }
	  
	  def stringFormatUnknown = {
	    val p = StringFormat(".","UNKNOWN",true)
	    eval( p, s0).isInstanceOf[Inconclusive]
	  }
	}