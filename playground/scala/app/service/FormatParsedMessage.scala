package service
import hl7.v2.instance.{ComplexComponent, ComplexField, Component, Field, Group, Line, Message, NULLComplexField, Segment, Separators, SimpleComponent, SimpleField, TimeZone, UnresolvedField, SegOrGroup => SGM}
import hl7.v2.profile.{Composite, Primitive, Req, SegRefOrGroup, SegmentRef, Varies, Component => CM, Field => FM, Group => GM, Segment => SM}

object NodeType {
  val MESSAGE = "MESSAGE"
  val SEGMENTREF = "SEGMENTREF"
  val GROUP = "GROUP"
  val FIELD = "FIELD"
  val COMPONENT = "COMPONENT"
  val SUBCOMPONENT = "SUBCOMPONENT"
}

object RefType {
  val DATATYPE = "DATATYPE"
  val SEGMENT = "SEGMENT"
}

case class Ref(of: String, id: String, name: String)
sealed trait InstanceNodeData {
  val ref: Ref
  val populated: Boolean
}

sealed trait Node {
  val nodeType: String
  val populated: Boolean
}

sealed trait Complex[T] {
  val children: Map[Int, T]
}

sealed trait Simple {
  val value: String
}

sealed trait Repeatable[T] {
  val count: Int
  val instances: Map[Int, T]
}

sealed trait NonRepeatable[T] {
  val instance: T
}

case class MessageNode(
  id: String,
  structId: String,
  event: String,
  typ  : String,
  desc : String,
  invalid: List[Line],
  unexpected: List[Line],
  defaultTimeZone: Option[TimeZone],
  separators: Separators,
  children: Map[Int, SegRefOrGroupNode]
) extends Complex[SegRefOrGroupNode]

sealed trait SegRefOrGroupNode extends Node {}
case class SegRefOrGroupChildren(
  children: Map[Int, SegRefOrGroupNode]
) extends Complex[SegRefOrGroupNode]
case class GroupNode(
  req: Req,
  id: String,
  name: String,
  nodeType: String,
  populated: Boolean,
  count: Int,
  instances: Map[Int, SegRefOrGroupChildren]
) extends SegRefOrGroupNode with Repeatable[SegRefOrGroupChildren]

case class SegmentInstance(
  ref: Ref,
  populated: Boolean,
  raw: String,
  children: Map[Int, FieldNode]
) extends Complex[FieldNode] with InstanceNodeData

case class SegRefNode(
  req: Req,
  id: String,
  name: String,
  desc: String,
  nodeType: String,
  populated: Boolean,
  count: Int,
  instances: Map[Int, SegmentInstance]
) extends SegRefOrGroupNode with Repeatable[SegmentInstance]

case class FieldInstance(
  ref: Ref,
  populated: Boolean,
  primitive: Boolean,
  value: String,
  children: Map[Int, ComponentNode]
) extends Complex[ComponentNode] with InstanceNodeData

case class FieldNode(
  req: Req,
  name: String,
  datatype: Primitive,
  nodeType: String,
  fromVaries: Boolean,
  populated: Boolean,
  count: Int,
  instances: Map[Int, FieldInstance]
) extends Repeatable[FieldInstance]

sealed trait ComponentNode extends Node {}
case class ComplexComponentNode(
  req: Req,
  name: String,
  datatype: Primitive,
  nodeType: String,
  populated: Boolean,
  children: Map[Int, SimpleComponentNode]
) extends ComponentNode with Complex[SimpleComponentNode]

case class SimpleComponentNode(
  req: Req,
  name: String,
  datatype: Primitive,
  nodeType: String,
  populated: Boolean,
  value: String
) extends ComponentNode with Simple

object FormatParsedMessage {

  import org.json4s._
  import org.json4s.jackson.Serialization
  implicit val formats = Serialization.formats(NoTypeHints)

  def toJson(mn: MessageNode) = {
    Serialization.write(mn)
  }

  def format(message: Message, full: Boolean): MessageNode = {
    MessageNode(
      message.model.id,
      message.model.structId,
      message.model.event,
      message.model.typ,
      message.model.desc,
      message.invalid,
      message.unexpected,
      message.defaultTimeZone,
      message.separators,
      formatSegRefOrGroup(message.model.structure, message.children, full)
    )
  }

  def formatSegRefOrGroup(structure: List[SegRefOrGroup], children: List[SGM], full: Boolean): Map[Int, SegRefOrGroupNode] = {
    structure.map {
      case srf: SegmentRef =>
        val instances = children.filter(_.position == srf.req.position).map(_.asInstanceOf[Segment])
        (srf.req.position, formatSegRefNode(srf, instances, full))
      case gm: GM =>
        val instances = children.filter(_.position == gm.req.position).map(_.asInstanceOf[Group])
        (gm.req.position, formatGroupNode(gm, instances, full))
    }.groupBy(_._1).view.mapValues(_.map(_._2).head).toMap
  }

  def formatSegRefOrGroupNode(grp: GM, children: List[SGM], full: Boolean): SegRefOrGroupChildren = {
    val parsed = grp.structure.map {
      case srf: SegmentRef =>
        val instances = children.filter(_.position == srf.req.position).map(_.asInstanceOf[Segment])
        (srf.req.position, formatSegRefNode(srf, instances, full))
      case gm: GM =>
        val instances = children.filter(_.position == gm.req.position).map(_.asInstanceOf[Group])
        (gm.req.position, formatGroupNode(gm, instances, full))
    }.groupBy(_._1).view.mapValues(_.map(_._2).head).toMap
    SegRefOrGroupChildren(parsed)
  }


  def formatGroupNode(gm: GM, instances: List[Group], full: Boolean): GroupNode = {
    val instanceMap: Map[Int, SegRefOrGroupChildren] = instances.map(grp => {
      (grp.instance, formatSegRefOrGroupNode(gm, grp.children, full))
    }).groupBy(_._1).view.mapValues(_.map(_._2).head).toMap


    GroupNode(
      gm.req,
      gm.id,
      gm.name,
      NodeType.GROUP,
      instances.nonEmpty,
      instances.size,
      instanceMap
    )
  }

  def formatSegRefNode(srf: SegmentRef, instances: List[Segment], full: Boolean): SegRefNode = {
    val instanceMap: Map[Int, SegmentInstance] = instances.map(seg => {
      (seg.instance, formatSegmentInstance(srf.ref, seg.model.ref, seg, seg.children, full))
    }).groupBy(_._1).view.mapValues(_.map(_._2).head).toMap

    SegRefNode(
      srf.req,
      srf.ref.id,
      srf.ref.name,
      srf.ref.desc,
      NodeType.SEGMENTREF,
      instances.nonEmpty,
      instances.size,
      instanceMap
    )
  }

  def formatSegmentInstance(sm: SM, ism: SM, s: Segment, fields: List[Field], full: Boolean): SegmentInstance = {
    SegmentInstance(
      Ref(
        RefType.SEGMENT,
        ism.id,
        ism.name,
      ),
      fields.nonEmpty,
      if(!full) s.rawMessageValue else null,
      if(full)
        sm.fields.map {
          (f) =>
            val instances = fields.filter(_.position == f.req.position)
            (f.req.position, formatFieldNode(f, instances))
        }.groupBy(_._1).view.mapValues(_.map(_._2).head).toMap
      else
        null
    )
  }

  def formatFieldNode(fm: FM, fields: List[Field]): FieldNode = {
    val instanceMap: Map[Int, FieldInstance] = fields.map(f => {
      (f.instance, f match {
        case sf: SimpleField => formatSimpleFieldInstance(sf)
        case uf: UnresolvedField => formatUnresolvedFieldInstance(uf)
        case cf: ComplexField => formatComplexFieldInstance(cf)
        case nf: NULLComplexField => formatNULLComplexFieldInstance(nf)
      })
    }).groupBy(_._1).view.mapValues(_.map(_._2).head).toMap

    FieldNode(
      fm.req,
      fm.name,
      Primitive(
        fm.datatype.id,
        fm.datatype.name,
        fm.datatype.desc,
        fm.datatype.version
      ),
      NodeType.FIELD,
      false,
      fields.nonEmpty,
      fields.size,
      instanceMap
    )
  }

  def formatSimpleFieldInstance(sf: SimpleField): FieldInstance = {
    FieldInstance(
      Ref(
        RefType.DATATYPE,
        sf.datatype.id,
        sf.datatype.name,
      ),
      sf.value != null && !sf.value.raw.isEmpty,
      true,
      sf.value.raw,
      null
    )
  }

  def formatUnresolvedFieldInstance(uf: UnresolvedField): FieldInstance = {
    FieldInstance(
      Ref(
        RefType.DATATYPE,
        uf.datatype.id,
        uf.datatype.name,
      ),
      uf.value != null && !uf.value.raw.isEmpty,
      true,
      uf.value.raw,
      null
    )
  }

  def formatNULLComplexFieldInstance(nf: NULLComplexField): FieldInstance = {
    FieldInstance(
      Ref(
        RefType.DATATYPE,
        nf.datatype.id,
        nf.datatype.name,
      ),
      false,
      false,
      null,
      Map(),
    )
  }

  def formatComplexFieldInstance(cf: ComplexField): FieldInstance = {
    val children: Map[Int, ComponentNode] = cf.datatype.components.map {
      (c) =>
        val instances = cf.children.filter(_.position == c.req.position)
        if(instances.size > 1) throw new Exception("Multiple instances of a component")

        (c.req.position, formatComponentNode(c, instances.headOption))
    }.groupBy(_._1).view.mapValues(_.map(_._2).head).toMap

    FieldInstance(
      Ref(
        RefType.DATATYPE,
        cf.datatype.id,
        cf.datatype.name,
      ),
      cf.children.nonEmpty,
      false,
      null,
      children
    )
  }

  def formatComponentNode(cm: CM, component: Option[Component]): ComponentNode = {
    cm.datatype match {
      case vd: Varies => formatVariesComponentNode()
      case pd: Primitive => formatSimpleComponentNode(cm, pd, component)
      case cd: Composite => formatComplexComponentNode(cm, cd, component)
    }
  }

  def formatSimpleComponentNode(cm: CM, dt: Primitive, component: Option[Component]): SimpleComponentNode = {
    component match {
      case Some(cp) =>
        if(!cp.isInstanceOf[SimpleComponent]) throw new Exception("Primitive datatype parsed non simple component")
        val simple = cp.asInstanceOf[SimpleComponent]
        SimpleComponentNode(
          cm.req,
          cm.name,
          dt,
          NodeType.COMPONENT,
          true,
          simple.value.raw
        )
      case None => SimpleComponentNode(
        cm.req,
        cm.name,
        dt,
        NodeType.COMPONENT,
        false,
        ""
      )
    }
  }

  def formatComplexComponentNode(cm: CM, dt: Composite, component: Option[Component]): ComplexComponentNode = {
    val primitive = Primitive(
      dt.id,
      dt.name,
      dt.desc,
      dt.version
    )

    component match {
      case Some(cp) =>
        if(!cp.isInstanceOf[ComplexComponent]) throw new Exception("Composite datatype parsed non complex component")
        val complex = cp.asInstanceOf[ComplexComponent]

        val children: Map[Int, SimpleComponentNode] = complex.datatype.components.map {
          (scm) =>
            val instances = complex.children.filter(_.position == scm.req.position)
            if(instances.size > 1) throw new Exception("Multiple instances of a component")
            (scm.req.position, formatSimpleComponentNode(scm, scm.datatype.asInstanceOf[Primitive], instances.headOption))
        }.groupBy(_._1).view.mapValues(_.map(_._2).head).toMap

        ComplexComponentNode(
          cm.req,
          cm.name,
          primitive,
          NodeType.COMPONENT,
          true,
          children
        )
      case None => ComplexComponentNode(
        cm.req,
        cm.name,
        primitive,
        NodeType.COMPONENT,
        false,
        Map()
      )
    }
  }

  def formatVariesComponentNode(): SimpleComponentNode = {
    throw new Exception("Varies Component")
  }

}