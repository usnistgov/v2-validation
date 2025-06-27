package hl7.v2.validation.vs

import gov.nist.hit.hl7.v2.schemas.utils.HL7v2Schema
import java.io.InputStream
import nist.xml.util.XOMDocumentBuilder

import scala.util.Try

case class ValueSetLibraryImpl(noValidation: Seq[String], library: Map[String, ValueSet]) extends ValueSetLibrary {

  private val legacy0396: Boolean = library.foldLeft(false) {
    (found, table) =>
      if (found) found
      else {
        table._2 match {
          case internalValueSet: InternalValueSet => val tableMatch = table._1.matches(".*0396.*")
            val codeHL7nnnn = tableMatch && internalValueSet.codes.exists(c => c.value.equals("HL7nnnn") && c.pattern.isEmpty)
            val code99zzz = tableMatch && internalValueSet.codes.exists(c => c.value.equals("99zzz") && c.pattern.isEmpty)
            tableMatch && (codeHL7nnnn || code99zzz)
          case _ => found
        }
      }
  }

  private val hasExternalValueSets: Boolean  = {
    library.values.exists(vs => vs.isInstanceOf[ExternalValueSet])
  }

  override def isExcludedFromTheValidation(id: String): java.lang.Boolean =
    noValidation contains id

  override def get(id: String): ValueSet =
    library get id match {
      case Some(x) => x
      case None    => throw new ValueSetNotFoundException(id)
    }

  override def containsLegacy0396Codes(): Boolean = legacy0396

  override def containsExternalValueSets(): Boolean = hasExternalValueSets
}

object ValueSetLibraryImpl {

  import nist.xml.util.XOMExtensions._

  /**
   * Builds and returns the value set map from the XML file
   */
  def apply(vsXML: InputStream): Try[ValueSetLibrary] = {
    XOMDocumentBuilder.build(vsXML, HL7v2Schema.getValueSetLibrary) map { doc =>
      val root = doc.getRootElement
      val noValDef = root.getFirstChildElement("NoValidation")
      val tables = root.getChildElements("ValueSetDefinitions")
      val external = root.getChildElements("ExternalValueSetDefinitions")
      val noVal    = noValidation( noValDef )
      val lib = extractValueSetDefinitions(tables).toList ::: extractExternalValueSetDefinitions(external).toList
      ValueSetLibraryImpl(noVal, lib.toMap)
    }
  }

  private def noValidation(e: nu.xom.Element): Seq[String] =
    if(e == null) Nil else e.getChildElements("BindingIdentifier").map(_.getValue.trim).toList
  
  private def extractValueSetDefinitions(e: nu.xom.Elements): Map[String, ValueSet] =
    if( e == null ) Map()
    else {
      e.foldLeft(Map[String, ValueSet]()){ (vsList, definitions) =>
        val internal = definitions
          .getChildElements("ValueSetDefinition")
          .foldLeft(Map[String, ValueSet]()) { (vsList, e) => {
            val vs = valueSet(e)
            vsList + (vs.id -> vs)
          }}
        val external = definitions
            .getChildElements("ExternalValueSetDefinition")
            .foldLeft(Map[String, ValueSet]()) { (extVsList, e) => {
              val vs = externalValueSet(e)
              extVsList + (vs.id -> vs)
            }}
        vsList ++ internal ++ external
      }
    }

  private def extractExternalValueSetDefinitions(e: nu.xom.Elements): Map[String, ValueSet] =
    if( e == null ) Map()
    else {
      e.foldLeft(Map[String, ValueSet]()){ (vsList, definitions) =>
        val external = definitions
          .getChildElements("ValueSetDefinition")
          .foldLeft(Map[String, ValueSet]()) { (vsList, e) => {
            val vs = externalValueSet(e)
            vsList + (vs.id -> vs)
          }}
        vsList ++ external
      }
    }

  private def externalValueSet(e: nu.xom.Element): ExternalValueSet = {
    val id = e.attribute("BindingIdentifier")
    val _stability = stability(e.attribute("Stability"))
    val _extensibility = extensibility(e.attribute("Extensibility"))
    val url = e.attribute("URL")
    ExternalValueSet(id, url, _extensibility, _stability)
  }

  private def valueSet(e: nu.xom.Element): InternalValueSet = {
    val id = e.attribute("BindingIdentifier")
    val _stability = stability( e.attribute("Stability") )
    val _extensibility = extensibility( e.attribute("Extensibility") )
    val codes = (e.getChildElements("ValueElement") map code).toList
    InternalValueSet(id, _extensibility, _stability, codes )
  }

  private def stability(s: String): Option[Stability] =
    s match {
      case ""        => None
      case "Static"  => Some(Stability.Static)
      case "Dynamic" => Some(Stability.Dynamic)
      case x => throw new Exception(s"Invalid value set stability '$x'")
    }

  private def extensibility(s: String): Option[Extensibility] =
    s match {
      case ""      => None
      case "Open"  => Some(Extensibility.Open)
      case "Closed" => Some(Extensibility.Closed)
      case x => throw new Exception(s"Invalid value set extensibility '$x'")
    }

  private def pattern(s: String): Option[String] =
    s match {
      case "" => None
      case x  => Some(x)
    }

  private def code(e: nu.xom.Element): Code = {
    val value   = e.attribute("Value")
    val desc    = e.attribute("DisplayName")
    val usage   = codeUsage( e.attribute("Usage") )
    val codeSys = e.attribute("CodeSystem")
    val codePattern = pattern( e.attribute("CodePattern") )
    Code(value, desc, usage, codeSys, codePattern)
  }

  private def codeUsage(s: String): CodeUsage = s match {
    case "R" => CodeUsage.R
    case "E" => CodeUsage.E
    case "P" => CodeUsage.P
    case ""  => CodeUsage.R //FIXME This is not needed if the xml is validated against the XSD
    case  x  => throw new Exception(s"Invalid code usage '$x'")
  }
}
