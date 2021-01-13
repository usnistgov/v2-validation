package hl7.v2.validation.vs

import java.io.InputStream

import hl7.v2.instance.{Component, Element, Field, Group, Path, Segment}
import hl7.v2.profile.BindingStrength
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
      new DefaultValueSetSpecification(init(extractNodes(root, "Datatype"), vBinding),init(extractNodes(root, "Segment"), vBinding), init(extractNodes(root, "Group"), vBinding))
    }
  }

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

  private def byNameEntries[T](e: nu.xom.Element, f: nu.xom.Element => T) =
    (e.attribute("Name"), (e.getChildElements map f).toList)

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