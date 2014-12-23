package hl7.v2.instance.serializer

import java.util
import java.util.{List => JList, ArrayList => JAL}

import hl7.v2.profile.{Req, Range}
import hl7.v2.instance._
import hl7.v2.instance.tree._

import tree.NodeType._

object Tree {

  def message(m: Message): Node = {
    val children = new JAL[Node]()
    m.children foreach { x => children add segOrGroup(x) }

    val invalid = new JAL[Line]()
    m.invalid foreach { t => invalid add new Line(t._1, t._2) }

    val unexpected = new JAL[Line]()
    m.unexpected foreach { t => invalid add new Line(t._1, t._2) }

    new Root( m.model.structId, children, invalid, unexpected )
  }

  private def segOrGroup(x: SegOrGroup): Node = x match {
    case s: Segment => segment(s)
    case g: Group   => group(g)
  }

  private def group(g: Group): Node = {
    val children = new JAL[Node]()
    g.children foreach { x => children add segOrGroup(x) }
    complexNode(GROUP, g.model.req, g.location, children )
  }

  private def segment(s: Segment): Node = {
    val children = new JAL[Node]()
    s.children foreach { x => children add field(x) }
    complexNode(SEGMENT, s.model.req, s.location, children )
  }

  private def field(f: Field): Node = f match {
    case SimpleField(_, r, l, _, v)      => simpleNode(FIELD, r, l, v )
    case ComplexField(_, r, l, _, cs, _) =>
      val children = new JAL[Node]()
      cs foreach { c => children add component(c) }
      complexNode(FIELD, r, l, children )
  }

  private def component(c: Component): Node = c match {
    case SimpleComponent(_, r, l, v)      => simpleNode(COMPONENT, r, l, v )
    case ComplexComponent(_, r, l, cs, _) =>
      val children = new util.ArrayList[Node]()
      cs foreach { c => children add component(c) }
      complexNode(COMPONENT, r, l, children )
  }

  private def complexNode (nt: NodeType, r: Req, l: Location, children: JList[Node]) = {
    val u = r.usage.toString
    val (min, max) = range( r.cardinality )
    val Location(name, path, line, col) = l
    new ComplexNode(nt, name, u, min, max, path, line, col, children)
  }

  private def simpleNode (nt: NodeType, r: Req, l: Location, v: Value) = {
    val u = r.usage.toString
    val (min, max) = range( r.cardinality )
    val Location(name, path, line, col) = l
    new SimpleNode(nt, name, u, min, max, path, line, col, v.raw)
  }

  private def range(or: Option[Range]) =
    or match { case Some(Range(x, y)) => (x, y) case _ => (-1, "-1") }

}
