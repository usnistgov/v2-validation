package hl7.v2.validation.report.extension

import hl7.v2.instance.Location
import hl7.v2.profile.{BindingLocation, BindingStrength, ValueSetSpec}
import hl7.v2.validation.report._
import hl7.v2.validation.vs.{Stability, Extensibility, ValueSet}

/**
  * Provides functions to convert a value set report entry (VSEntry) to Json
  */
object VSEntryAsJson {

  /**
    * Creates and returns a Json string from a value set report entry (VSEntry)
    * @param e - The value report entry
    * @return The Json string
    */
  def toJson(e: VSEntry): String = e match {
    case x: EVS          => toJson(x)
    case x: PVS          => toJson(x)
    case x: CodeNotFound => toJson(x)
    case x: VSNotFound   => toJson(x)
    case x: EmptyVS      => toJson(x)
    case x: VSError      => toJson(x)
    case x: VSSpecError  => toJson(x)
    case x: CodedElem    => toJson(x)
    case x: NoVal        => toJson(x)
  }

  private def toJson(l: Location) = extension.toJson(l)

  private def toJson(x: EVS): String =
    gen("EVS", x.location, x.value, x.valueSet, x.bindingStrength)

  private def toJson(x: PVS): String =
    gen("PVS", x.location, x.value, x.valueSet, x.bindingStrength)

  private def toJson(x: CodeNotFound): String =
    gen("CodeNotFound", x.location, x.value, x.valueSet, x.bindingStrength)

  private def toJson(x: VSNotFound): String = {
    val bs = x.bindingStrength match { case None => "" case Some(y) => toJson(y)}
    s"""{"VSNotFound":{${gen(x.location, x.value)},"valueSetId":"${
      escape(x.valueSetId)
    }"$bs}}"""
  }

  private def toJson(x: EmptyVS): String = {
    val bs = x.bindingStrength match { case None => "" case Some(y) => toJson(y)}
    s"""{"EmptyVS":{${gen(x.location, x.valueSet)}$bs}}"""
  }

  private def toJson(x: VSError): String =
    s"""{"VSError":{${gen(x.location, x.valueSet)},"reason":"${escape(x.reason)}"}}"""

  private def toJson(x: VSSpecError): String =
    s"""{"VSSpecError":{${gen(x.location, x.valueSet, x.spec, x.msg) }}}"""

  private def toJson(x: CodedElem): String =
    s"""{"CodedElem":{${gen(x.location, x.valueSet, x.spec, x.msg)
        }, "details":${ toJson(x.details)}}}"""

  private def toJson(x: NoVal): String =
    s"""{"NoVal":{${toJson(x.location)}, "valueSetId":"${escape(x.valueSetId)}"}}"""

  private def toJson(x: ValueSet): String = {
    val e = x.extensibility match { case None => "" case Some(y) => toJson(y) }
    val s = x.stability match { case None => "" case Some(y) => toJson(y) }
    s"""{"id":"${escape(x.id)}"$e$s}"""
  }
  private def toJson(x: ValueSetSpec): String = {
    val bs = x.bindingStrength match { case None => "" case Some(y) => toJson(y)}
    val bl = x.bindingLocation match { case None => "" case Some(y) => toJson(y)}
    s"""{"valueSetId":"${x.valueSetId}"$bs$bl}"""
  }

  private def toJson(x: BindingStrength) = s""","bindingStrength":"$x""""

  private def toJson(x: BindingLocation) = s""","bindingLocation":"$x""""

  private def toJson(x: Extensibility) = s""","extensibility":"$x""""

  private def toJson(x: Stability) = s""","stability":"$x""""

  private def gen(n: String, l: Location, value: String, valueSet: ValueSet,
                  obs: Option[BindingStrength]): String = {
    val bs = obs match { case None => "" case Some(y) => toJson(y)}
    s"""{"$n":{${gen(l, valueSet, value)}$bs}}"""
  }

  private def gen(l: Location, vs: ValueSet): String =
    s"""${toJson(l)}, "valueSet":${toJson(vs)}"""

  private def gen(l: Location, vs: ValueSet, value: String): String =
    s"""${toJson(l)}, "valueSet":${toJson(vs)},"value":"${escape(value)}""""

  private def gen(l: Location, value: String): String =
    s"""${toJson(l)}, "value":"${escape(value)}""""

  private def gen(l: Location, ovs: Option[ValueSet],
                  spec: ValueSetSpec, msg: String): String = {
    val vs  = ovs match {
      case None => ""
      case Some(x) => s""","valueSet":${toJson(x)}"""
    }
    //val vs  = ovs map { y => s""","valueSet":${toJson(y)}""" }
    s"""${toJson(l)}$vs,"spec":${toJson(spec)},"msg":"${escape(msg)}""""
  }

  private def toJson(l: List[VSEntry]): String =
    s"""${ (l map toJson) mkString ("[", ",", "]") }"""

}
