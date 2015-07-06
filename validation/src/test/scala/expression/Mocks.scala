package expression

import hl7.v2.instance._
import hl7.v2.profile.{Usage, Req}
import hl7.v2.validation.vs.EmptyValueSetLibrary

trait Mocks {

  implicit val defaultValueSetLibrary = EmptyValueSetLibrary.getInstance()

  implicit val separators = Separators( '|', '^', '~', '\\', '&', Some('#') )

  implicit val dtz = Some( TimeZone("+0000") )

  trait Default {
    val reqs     = List[Req]()
    val location = Location(null, "desc ...", "Path", -1, -1)
    /*val qProps   = QProps(QType.DT, "id", "name")*/
    val hasExtra = false
    val req = Req(-1, "", Usage.O, None, None, None, Nil)
  }

  case class S(override val position: Int, instance: Int, value: Value)
    extends Simple  with Default

  case class C(override val position: Int, instance: Int, children: List[Element])
    extends Complex with Default

  val s0  = S( 4, 1, Text("41\\F\\") )
  val s1  = S( 5, 1, Number("51") )
  val s2  = S( 5, 2, Text("52")   )
  val s3  = S( 5, 3, Number("S53"))

  def c1Children = List(
    S(1, 1, Text("S11")) , S(1, 2, Text("S12")) , S(1, 3, Text("S13")),
    S(2, 1, Number("21")), S(2, 2, Number("22")), S(2, 3, Number("23")),
    S(3, 1, Text("S3"))  , S(3, 2, Text("S3"))  , S(3, 3, Text("S3"))
  )

  val c0 = C(2,1, Nil)
  val c1 = C(2,3, c1Children )
  val c2 = C(1,1, s0::s1::s2::c0::c1::Nil)

  def elementsDescription =
    """s0 -> Simple(4, 1, Text(41\F\) )                    c1 -> Complex( position= 2, instance= 3)
    s1 -> Simple(5, 1, Number(51) )                            1[1] -> Simple( value=Text(S11) )
    s2 -> Simple(5, 2, Text(52) )                              1[2] -> Simple( value=Text(S12) )
    s3 -> Simple(5, 3, Number("S53") )                         1[3] -> Simple( value=Text(S13) )
                                                               2[1] -> Simple( value=Number(21))
    c0 -> Complex( position= 2, instance= 1, No children)      2[2] -> Simple( value=Number(22))
                                                               2[3] -> Simple( value=Number(23))
    c2 -> Complex( position= 2, instance= 1)                   3[1] -> Simple( value=Text(S3)  )
       2[1] -> c0                                              3[2] -> Simple( value=Text(S3)  )
       2[3] -> c1                                              3[3] -> Simple( value=Text(S3)  )
       4[1] -> s0
       5[1] -> s1
       5[2] -> s2
       5[3] -> s3"""

  def c1Description =
    """ c1 -> Complex( position= 2, instance= 3)
       1[1] -> Simple( value=Text(S11) )        2[3] -> Simple( value=Number(23))
       1[2] -> Simple( value=Text(S12) )        3[1] -> Simple( value=Text(S3)  )
       1[3] -> Simple( value=Text(S13) )        3[2] -> Simple( value=Text(S3)  )
       2[1] -> Simple( value=Number(21))        3[3] -> Simple( value=Text(S3)  )
       2[2] -> Simple( value=Number(22))"""

}