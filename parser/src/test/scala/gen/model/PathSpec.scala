package gen.model

import gen.model.Path.isValid
import org.specs2.mutable.Specification

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

class PathSpec extends Specification {

  val valid   = Seq( "1[1]", "1[2].2[*].3[*]" )
  val invalid = Seq( "1", "0[1]", "1[0]", "1[1].2", "1[1].", "1[1].1", "1[1].x" )

  "Path.isValid( x: String ) " should {
    s"return true  for ${valid.mkString("{ ", ", ", " }")}" in {
      valid map ( isValid( _ ) === true )
    }

    s"return false for ${invalid.mkString("{ ", ", ", " }")}" in { 
      invalid map ( isValid(_) === false )
    }
  }
}
