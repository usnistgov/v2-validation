package hl7.v2.validation.vs

import gov.nist.hit.hl7.v2.schemas.utils.{HL7v2Schema, HL7v2SchemaResourceResolver}

import java.io.InputStream
import hl7.v2.instance.{Component, Element, Field, Group, Path, Segment}
import hl7.v2.profile.{BindingStrength, Composite, Datatype, Profile, ValueSetSpec, BindingLocation => BL, Segment => SM}
import nist.xml.util.{ClassPathResourceResolver, XOMDocumentBuilder}
import nist.xml.util.XOMExtensions._

import scala.util.Try

class DefaultValueSetSpecification(
  val datatypeVsSpec : Map[String, List[ValueSetBinding]],
  val segmentVsSpec : Map[String, List[ValueSetBinding]],
  val groupVsSpec : Map[String, List[ValueSetBinding]],
  val datatypeSgSpec : Map[String, List[SingleCodeBinding]],
  val segmentSgSpec : Map[String, List[SingleCodeBinding]],
  val groupSgSpec : Map[String, List[SingleCodeBinding]],
) extends ValueSetSpecification {

  def vsSpecificationFor(e: Element) = e match {
    case c: Component => datatypeVsSpec.get(c.datatype.id)
    case f: Field => datatypeVsSpec.get(f.datatype.id)
    case s: Segment => segmentVsSpec.get(s.model.ref.id)
    case g: Group => groupVsSpec.get(g.model.id)
  }

  override def singleCodeSpecificationFor(e: Element): Option[List[SingleCodeBinding]] = e match {
    case c: Component => datatypeSgSpec.get(c.datatype.id)
    case f: Field => datatypeSgSpec.get(f.datatype.id)
    case s: Segment => segmentSgSpec.get(s.model.ref.id)
    case g: Group => groupSgSpec.get(g.model.id)
  }
}

object DefaultValueSetSpecification {

  /**
   * The resource resolver
   */
  private val resourceResolver = Some(new HL7v2SchemaResourceResolver())

  def apply(context: InputStream): Try[ValueSetSpecification] = {
    XOMDocumentBuilder.build(context, HL7v2Schema.getValueSetBindings, resourceResolver) map { doc =>
      val vsBindingsRoot = doc.getRootElement.getFirstChildElement("ValueSetBindings")
      val sgBindingsRoot = doc.getRootElement.getFirstChildElement("SingleCodeBindings")

      new DefaultValueSetSpecification(
        init(extractNodes(vsBindingsRoot, "Datatype"), vBinding),
        init(extractNodes(vsBindingsRoot, "Segment"), vBinding),
        init(extractNodes(vsBindingsRoot, "Message"), vBinding),
        init(extractNodes(sgBindingsRoot, "Datatype"), sgBinding),
        init(extractNodes(sgBindingsRoot, "Segment"), sgBinding),
        init(extractNodes(sgBindingsRoot, "Message"), sgBinding))
    }
  }

  def extractFromProfile(profile: Profile): DefaultValueSetSpecification = {
    val datatypes = profile.datatypes.view.mapValues(extractFromDatatype).toMap
    val segments = profile.segments.view.mapValues(extractFromSegment).toMap
    new DefaultValueSetSpecification(
      datatypes,
      segments,
      Map.empty[String, List[ValueSetBinding]],
      Map.empty[String, List[SingleCodeBinding]],
      Map.empty[String, List[SingleCodeBinding]],
      Map.empty[String, List[SingleCodeBinding]]
    )
  }

  def extractFromDatatype(dt: Datatype): List[ValueSetBinding] =  {
    dt match {
      case c: Composite => c.components.foldLeft(List[ValueSetBinding]()) {
        (acc, cmp) => cmp.req.vsSpec match {
          case Nil => acc
          case ls => acc ::: ls.map(mapLegacyToImprovedValueSetBindingSpec(cmp.req.position, cmp.datatype, _))
        }
      }
      case _ => Nil
    }
  }

  def extractFromSegment(segment: SM): List[ValueSetBinding] = {
    segment.fields.foldLeft(List[ValueSetBinding]()) {
      (acc, field) =>
        field.req.vsSpec match {
          case Nil => acc
          case ls => acc ::: ls.map(mapLegacyToImprovedValueSetBindingSpec(field.req.position, field.datatype, _))
        }
    }
  }

  def mapLegacyToImprovedValueSetBindingSpec(position: Int, dt: Datatype, vsSpec: ValueSetSpec): ValueSetBinding = {
    val isCoded = isCodedElement(dt)
    val strength = vsSpec.bindingStrength
    val target = s"${position}[*]"
    val bindings = vsSpec.valueSetId.split(":").toList
    val bindingLocations = vsSpec.bindingLocation match {
      case None => List(BindingLocation(".", None))
      case Some(bl) => bl match {
        case BL.Position(value) => List(mapBindingLocation(value, isCoded))
        case BL.XOR(position1, position2) => List(
          mapBindingLocation(position1, isCoded),
          mapBindingLocation(position2, isCoded)
        )
      }
    }
    ValueSetBinding(target, strength, bindings, bindingLocations)
  }

  def mapBindingLocation(position: Int, coded: Boolean): BindingLocation = BindingLocation(s"${position}[1]", if(coded) Some(s"${position + 2}[1]") else None)

  def isCodedElement(dt: Datatype): Boolean = dt.name.matches("C(W|N)?E")

  private def asOption(s: String) = if (s == "") None else Some(s)

  private def extractNodes(e: nu.xom.Element, name: String) = {
    if (e == null) null
    else {
      val ee = e.getFirstChildElement(name)
      if (ee != null) ee.getChildElements else null
    }
  }

  def vBinding(e: nu.xom.Element): ValueSetBinding = {
    val target = e.attribute("Target")
    if(!Path.isValid(target)) throw new Exception(s"Invalid target '$target'")
    val bs = asOption(e.attribute("BindingStrength")) map { x => BindingStrength(x).get }
    ValueSetBinding(target, bs, bindingIdentifiers(e.getFirstChildElement("Bindings")), bindingLocations(e.getFirstChildElement("BindingLocations")))
  }

  def sgBinding(e: nu.xom.Element): SingleCodeBinding = {
    val target = e.attribute("Target")
    if(!Path.isValid(target)) throw new Exception(s"Invalid target '$target'")
    val code = e.attribute("Code") match {
      case "" | null => throw new Error("Single Code Binding's Code attribute is required")
      case c => c
    }
    val cs = e.attribute("CodeSystem") match {
      case "" | null => throw new Error("Single Code Binding's CodeSystem attribute is required")
      case c => c
    }
    SingleCodeBinding(target, code, cs, bindingLocations(e.getFirstChildElement("BindingLocations")))
  }

  private def bindingLocations(e: nu.xom.Element): List[BindingLocation] = {
    val children = e.getChildElements
    children.foldLeft(List[BindingLocation]()) {
      (acc, location) => if(location.getLocalName.equals("SimpleBindingLocation")) BindingLocationReader(location.getAttributeValue("CodeLocation"), None).get :: acc
      else if (location.getLocalName.equals("ComplexBindingLocation")) BindingLocationReader(location.getAttributeValue("CodeLocation"), Some(location.getAttributeValue("CodeSystemLocation"))).get :: acc
      else acc
    }
  }

  private def bindingIdentifiers(e: nu.xom.Element): List[String] = {
    val children = e.getChildElements
    children.foldLeft(List[String]()) {
      (acc, location) => val attribute = location.getAttribute("BindingIdentifier")
        if(attribute == null) acc else attribute.getValue :: acc
    }
  }

  private def byIDEntries[T](e: nu.xom.Element, f: nu.xom.Element => T) =
    (e.attribute("ID"), (e.getChildElements map f).toList)

  private def init[T](es: nu.xom.Elements, f: nu.xom.Element => T) = {
    val i = Map[String, List[T]]()
    if (es == null)
      i
    else
      es.foldLeft(i) { (acc, e) =>
        if ("ByID" == e.getLocalName)
          (acc + byIDEntries(e, f))
        else
          (acc)
      }
  }


}