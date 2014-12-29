package hl7.v2.instance

import hl7.v2.profile._

/**
  * @author Salifou Sidi M. Malick <salifou.sidi@gmail.com>
  */

trait Mocks {

  trait Default {
    val reqs     = List[Req]()
    val location = Location("desc ...", "Path", -1, -1)
    /*val qProps   = QProps(QType.DT, "id", "name")*/
    val hasExtra = false
    val req = Req(-1, Usage.O, None, None, None, None)
  }

  case class S(override val position: Int, instance: Int, value: Value)
    extends Simple with Default

  case class C(override val position: Int, instance: Int, children: List[Element] )
    extends Complex with Default

  val s0  = S( 4, 1, Text("S0") )

  val c0 = C(2,1, s0 :: Nil)
  
  val c1 = C( 2, 3, ( 1 to 3 ).toList map { i => S( 1, i, Text(s"S1$i") ) } )

  val c2 = C(1,1, s0 :: c0 :: c1 :: Nil)

  def elementsDescription = 
    """s0 -> Simple(4, 1, Text(41) )

    c0 -> Complex( position= 2, instance= 1, No children)

    c1 -> Complex( position= 2, instance= 3)
        1[1] -> Simple( value=Text(S11) )
        1[2] -> Simple( value=Text(S12) )
        1[3] -> Simple( value=Text(S13) )

    c2 -> Complex( position= 1, instance= 1)
        2[1] -> c0
        2[3] -> c1"""
}
