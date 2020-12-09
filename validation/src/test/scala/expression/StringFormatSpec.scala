package expression
	
	import expression.EvalResult.Pass
	import hl7.v2.instance.{Element, Query, Text}
	import Query.{query, queryAsSimple}
	import org.specs2.Specification

	import scala.util.{Failure, Success}
	import expression.EvalResult.Fail
	import expression.EvalResult.Inconclusive
	
	trait StringFormatSpec extends Specification with Evaluator with Mocks {
	
	  /*
	      StringFormatSpec
	        StringFormat should succeed if the path is not populated $stringFormatPathNotPopulated
          StringFormat should fail if a LOINC string is invalid $stringFormatLOINCInvalid
          StringFormat should pass if a LOINC string is valid $stringFormatLOINCvalid
          StringFormat should fail if a SNOMED string is invalid $stringFormatSNOMEDInvalid
          StringFormat should pass if a SNOMED string is valid $stringFormatSNOMEDvalid
          StringFormat should be inconclusive if a string format is unrecognized $stringFormatUnknown
          If the path is valued to multiple elements
        		StringFormat should pass if one of the elements is in the list and AtLeastOnce = True           $stringFormatAtLeastOnceT
        		StringFormat should fail if one of the elements is not in the list and AtLeastOnce = False           $stringFormatAtLeastOnceF
      		StringFormat evaluation should fail If not present behavior is FAIL and no element is found  $stringFormatNoElmFAIL
      		StringFormat evaluation should be inconclusive If not present behavior is INCONCLUSIVE and no element is found $stringFormatNoElmINC
      		StringFormat evaluation should pass If not present behavior is PASS and no element is found $stringFormatNoElmPASS
	  */

		//c1.4[1] is not populated
		assert( queryAsSimple(c1, "4[1]") == Success(Nil) )
		def stringFormatPathNotPopulated = eval( StringFormat("4[1]", "LOINC", false), c1 ) === Pass

		def stringFormatNoElmFAIL = {
			val f = StringFormat("4[1]", "LOINC", false, "FAIL")
			eval(f, c1) === Failures.notPresentBehaviorFail(f, f.path, c1)
		}
		def stringFormatNoElmINC = {
			val f = StringFormat("4[1]", "LOINC", false, "INCONCLUSIVE")
			eval(f, c1) === Failures.notPresentBehaviorInconclusive(f, f.path, c1)
		}
		def stringFormatNoElmPASS = {
			val f = StringFormat("4[1]", "LOINC", false, "PASS")
			eval(f, c1) === Pass
		}

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

		assert( queryAsSimple(c3, "1[*]").isSuccess &&  queryAsSimple(c3, "1[*]").get.size > 1)
		def stringFormatAtLeastOnceT = {
			val p = StringFormat("1[*]", "LOINC", true)
			eval( p, c3 ) === Pass
		}

		def stringFormatAtLeastOnceF = {
			val p = StringFormat("1[*]", "LOINC", false)
			val `c3.1[2]`  = queryAsSimple(c3, "1[2]").get.head
			assert( `c3.1[2]`.value == s_SNOMED.value )
			eval( p, c3 ) === Failures.stringFromat(p, `c3.1[2]`::Nil)
		}
	}