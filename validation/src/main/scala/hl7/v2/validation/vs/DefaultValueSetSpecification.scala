package hl7.v2.validation.vs

import java.io.InputStream

import hl7.v2.instance.{Component, Element, Field, Group, Path, Segment}
import hl7.v2.profile.{BindingLocation => BL, BindingStrength, Composite, Datatype, Segment => SM, Profile, ValueSetSpec}
import nist.xml.util.{ClassPathResourceResolver, XOMDocumentBuilder}
import nist.xml.util.XOMExtensions._

import scala.util.Try

class DefaultValueSetSpecification(
  val datatypeSpec : Map[String, List[ValueSetBinding]],
  val segmentSpec : Map[String, List[ValueSetBinding]],
  val groupSpec : Map[String, List[ValueSetBinding]]
) extends ValueSetSpecification {


  def vsSpecificationFor(e: Element) = e match {
    case c: Component => datatypeSpec.get(c.datatype.id)
    case f: Field => datatypeSpec.get(f.datatype.id)
    case s: Segment => segmentSpec.get(s.model.ref.id)
    case g: Group => groupSpec.get(g.model.id)
  }

}

object DefaultValueSetSpecification {

  private val resourceResolver = Some(new ClassPathResourceResolver("/rules"))

  def apply(context: InputStream): Try[ValueSetSpecification] = {
    XOMDocumentBuilder.build(context, getClass.getResourceAsStream("/rules/ValueSetBindings.xsd"), resourceResolver) map { doc =>
      val root = doc.getRootElement.getFirstChildElement("ValueSetBindings")
      new DefaultValueSetSpecification(
        init(extractNodes(root, "Datatype"), vBinding),
        init(extractNodes(root, "Segment"), vBinding),
        init(extractNodes(root, "Group"), vBinding))
    }
  }

  def extractFromProfile(profile: Profile): DefaultValueSetSpecification = {
    val datatypes = profile.datatypes.mapValues(extractFromDatatype)
    val segments = profile.segments.mapValues(extractFromSegment)
    new DefaultValueSetSpecification(datatypes, segments, Map.empty[String, List[ValueSetBinding]])
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