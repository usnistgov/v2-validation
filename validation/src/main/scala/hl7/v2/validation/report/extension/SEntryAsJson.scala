package hl7.v2.validation.report
package extension

import hl7.v2.profile.Range
import hl7.v2.instance.{Line, Location}
import hl7.v2.validation.vs.{BindingStrength, ValueSet}

/**
  * Provides functions to convert a structure report entry (SEntry) to Json
  */
object SEntryAsJson {

  /**
    * Creates and returns a Json string from a structure report entry (SEntry)
    * @param s - The structure report entry
    * @return The Json string
    */
  def toJson(s: SEntry): String = s match {
    case x: RUsage   => toJson(x)
    case x: REUsage  => toJson(x)
    case x: XUsage   => toJson(x)
    case x: WUsage   => toJson(x)
    case x: MinCard  => toJson(x)
    case x: MaxCard  => toJson(x)
    case x: Length   => toJson(x)
    case x: Format   => toJson(x)
    case x: Extra    => toJson(x)
    case x: EVS      => toJson(x)
    case x: PVS      => toJson(x)
    case x: CodeNotFound    => toJson(x)
    case x: VSNotFound      => toJson(x)
    case x: VSSpecError     => toJson(x)
    case x: UnexpectedLines => toJson(x)
    case x: InvalidLines    => toJson(x)
    case x: UnescapedSeparators => toJson(x)
  }

  private def toJson(l: Location) = extension.toJson(l)

  /**
    * Creates and returns a Json string from a Range
    */
  private def toJson(r: Range): String = s""""range":{"min":"${r.min}","max":"${r.max}"}"""

  /**
    * Creates and returns a Json string from an RUsage report entry
    */
  private def toJson(x: RUsage): String = s"""{"RUsage":{${toJson(x.location)}}}"""

  /**
   * Creates and returns a Json string from an RUsage report entry
   */
  private def toJson(x: REUsage): String = s"""{"REUsage":{${toJson(x.location)}}}"""

  /**
    * Creates and returns a Json string from an XUsage report entry
    */
  private def toJson(x: XUsage): String = s"""{"XUsage":{${toJson(x.location)}}}"""

  /**
    * Creates and returns a Json string from a WUsage report entry
    */
  private def toJson(x: WUsage): String = s"""{"WUsage":{${toJson(x.location)}}}"""

  /**
    * Creates and returns a Json string from a MinCard report entry
    */
  private def toJson(x: MinCard): String =
    s"""{"MinCard":{${toJson(x.location)},${toJson(x.range)},"instance":"${x.instance}"}}"""

  /**
    * Creates and returns a Json string from a MaxCard report entry
    */
  private def toJson(x: MaxCard): String =
    s"""{"MaxCard":{${toJson(x.location)},${toJson(x.range)},"instance":"${x.instance}"}}"""

  /**
    * Creates and returns a Json string from a Length report entry
    */
  private def toJson(x: Length): String =
    s"""{"Length":{${toJson(x.location)},${toJson(x.range)},"value":"${escape(x.value)}"}}"""

  /**
    * Creates and returns a Json string from a Format report entry
    */
  private def toJson(x: Format): String =
    s"""{"Format":{${toJson(x.location)},"details":"${escape(x.details)}"}}"""

  /**
    * Creates and returns a Json string from an Extra report entry
    */
  private def toJson(x: Extra): String = s"""{"Extra":{${toJson(x.location)}}}"""

  /**
    * Creates and returns a Json string from an UnescapedSeparators report entry
    */
  private def toJson(x: UnescapedSeparators): String =
    s"""{"UnescapedSeparators":{${toJson(x.location)}}}"""

  /**
    * Creates and returns a Json string from the InvalidLine
    */
  private def toJson(x: InvalidLines): String =
    s"""{"InvalidLines":${ x.list map toJson mkString("[", ",", "]" )}}"""

  /**
    * Creates and returns a Json string from the UnexpectedLine
    */
  private def toJson(x: UnexpectedLines): String =
    s"""{"UnexpectedLines":${ x.list map toJson mkString("[", ",", "]" )}}"""

  private def toJson(l: Line): String =
    s"""{"number":"${l.number}","content":"${escape(l.content)}"}"""

  private def toJson(x: EVS): String =
    s"""{"EVS":{${toJson(x.location)},"value":"${escape(x.value)
    }",${toJson(x.valueSet)}${toJson(x.bindingStrength)}}}"""

  private def toJson(x: PVS): String =
    s"""{"PVS":{${toJson(x.location)},"value":"${escape(x.value)
    }",${toJson(x.valueSet)}${toJson(x.bindingStrength)}}}"""

  private def toJson(x: CodeNotFound): String =
    s"""{"CodeNotFound":{${toJson(x.location)},"value":"${escape(x.value)
    }",${toJson(x.valueSet)}${toJson(x.bindingStrength)}}}"""

  private def toJson(x: VSNotFound): String =
    s"""{"VSNotFound":{${toJson(x.location)},"value":"${escape(x.value)
    }","ValueSetId":"${escape(x.valueSetId)}"${toJson(x.bindingStrength)}}}"""

  private def toJson(x: VSSpecError): String =
    s"""{"VSSpecError":{${toJson(x.location)},"ValueSetId":"${escape(x.valueSetId)
    }", "msg":"${escape(x.msg)}"}}"""

  private def toJson(os: Option[BindingStrength]): String =
    os match {
      case Some(s) => s""","bindingStrength":"$s""""
      case None    => ""
    }

  private def toJson(x: ValueSet): String = {
    val stability = x.stability match {
      case Some(s) => s""","stability":"$s""""
      case None => ""
    }
    val extensibility = x.extensibility match {
      case Some(s) => s""","extensibility":"$s""""
      case None    => ""
    }
    s""""ValueSet":{"id":"${escape(x.id)}"$stability$extensibility}"""
  }

}
