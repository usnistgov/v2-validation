package hl7.v2.parser.impl

import scala.language.implicitConversions

import gen.model.{Requirement, Text, Value, QProps}
import hl7.v2.instance.n._
import hl7.v2.profile.{Field => FM, Component => CM}
import hl7.v2.profile.{Datatype => DT}

object DEBuilder {

  val kind = Datatype

  def simpleFied(  ): SField = ???

  def complexField( ): CField = ???


  /**
   *
   * @param m - The model
   * @param v - The value
   * @param l - The line
   * @param c - The column
   * @param p - The path
   * @return
   */
  def component(m: CM, v: String, l: Int, c: Int, p: String): Option[Component] =
    if( v matches emptyComponent )
      None
    else
      Some {
        implicit val qProps = qprops(m.datatype, m.position)
        m.datatype.components match {
          case Nil => SComponent(v, p, m.name, l, c, qProps)
          case xs  => cComponent(v, xs, l, c, p)
        }
      }

  private def cComponent(v: String, cm: List[CM], l: Int, c: Int, p: String)
                (implicit  qp: QProps): CComponent = {
    //1. get the list of requirements
    // val reqs = ....

    //2. for each child model create the sub component
    // val children: List[SComponent] = ...

    //CComponent(children, path, desc, l, c, qProps, reqs)
    ???
  }

  private val emptyComponent = s"(?:\\s*\\Q$cs\\E*\\s*$ss*\\s*)*"

  private val emptyField = s"(?:\\s*\\Q$cs\\E*\\s*$ss*\\s*)*"

  implicit def hl7Value(v: String): Value = Text(v) ///FIXME

  private def qprops(d: DT, position: Int, instance: Int = 1) =
    QProps(kind, d.id, d.name, position, instance)

  //private def reqs(l: ) : List[Requirement] = ???

}
