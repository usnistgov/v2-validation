package hl7.v2.validation.report
package n

import com.typesafe.config.ConfigFactory

object Configurations {
  def category[T: Config](t: T): String       = implicitly[Config[T]].category
  def classification[T: Config](t: T): String = implicitly[Config[T]].classification
  def template[T: Config](t: T): String       = implicitly[Config[T]].template
}

trait Config[T] {
  def category: String
  def classification: String
  def template: String
}

object Config {

  val conf = ConfigFactory.load

  implicit object RUsageConfig extends Config[RUsage] {
    val category       = conf.getString("report.r-usage.category")
    val classification = conf.getString("report.r-usage.classification")
    val template       = conf.getString("report.r-usage.template")
  }

  implicit object XUsageConfig extends Config[XUsage] {
    val category       = conf.getString("report.x-usage.category")
    val classification = conf.getString("report.x-usage.classification")
    val template       = conf.getString("report.x-usage.template")
  }

  implicit object WUsageConfig extends Config[WUsage] {
    val category       = conf.getString("report.w-usage.category")
    val classification = conf.getString("report.w-usage.classification")
    val template       = conf.getString("report.w-usage.template")
  }

  implicit object REUsageConfig extends Config[REUsage] {
    val category       = conf.getString("report.re-usage.category")
    val classification = conf.getString("report.re-usage.classification")
    val template       = conf.getString("report.re-usage.template")
  }

  implicit object MinCardConfig extends Config[MinCard] {
    val category       = conf.getString("report.cardinality.category")
    val classification = conf.getString("report.cardinality.classification")
    val template       = conf.getString("report.cardinality.template")
  }

  implicit object MaxCardConfig extends Config[MaxCard] {
    val category       = MinCardConfig.category
    val classification = MinCardConfig.classification
    val template       = MinCardConfig.template
  }

  implicit object LengthConfig extends Config[Length] {
    val category       = conf.getString("report.length.category")
    val classification = conf.getString("report.length.classification")
    val template       = conf.getString("report.length.template")
  }

  implicit object FormatConfig extends Config[Format] {
    val category       = conf.getString("report.format.category")
    val classification = conf.getString("report.format.classification")
    val template       = conf.getString("report.format.template")
  }

  implicit object ExtraConfig extends Config[MaxCard] {
    val category       = conf.getString("report.extra.category")
    val classification = conf.getString("report.extra.classification")
    val template       = conf.getString("report.extra.template")
  }

  implicit object UnescapedConfig extends Config[UnescapedSeparators] {
    val category       = conf.getString("report.unescaped.category")
    val classification = conf.getString("report.unescaped.classification")
    val template       = conf.getString("report.unescaped.template")
  }

  implicit object UnexpectedConfig extends Config[UnexpectedLine] {
    val category       = conf.getString("report.unexpected.category")
    val classification = conf.getString("report.unexpected.classification")
    val template       = conf.getString("report.unexpected.template")
  }

  implicit object InvalidConfig extends Config[InvalidLine] {
    val category       = conf.getString("report.invalid.category")
    val classification = conf.getString("report.invalid.classification")
    val template       = conf.getString("report.invalid.template")
  }

}


/*
object Configurations {

  val conf = ConfigFactory.load

  object Classifications {

    val Failure = conf.getString("report.failure.classification")
    val PredicateFailure = conf.getString("report.predicate-failure.classification")

    val EVS = conf.getString("report.evs.classification")
    val PVS = conf.getString("report.pvs.classification")
    val CodeNotFound = conf.getString("report.code-not-found.classification")
    val VSNotFound = conf.getString("report.vs-not-found.classification")
    val EmptyVS = conf.getString("report.empty-vs.classification")
    val VSError = conf.getString("report.vs-error.classification")
    val NoVal = conf.getString("report.vs-no-validation.classification")
    val CodedElement = conf.getString("report.coded-element.classification")

  }

  object Categories {

    val Failure = conf.getString("report.failure.category")
    val PredicateFailure = conf.getString("report.predicate-failure.category")

    val EVS = conf.getString("report.evs.category")
    val PVS = conf.getString("report.pvs.category")
    val CodeNotFound = conf.getString("report.code-not-found.category")
    val VSNotFound = conf.getString("report.vs-not-found.category")
    val EmptyVS = conf.getString("report.empty-vs.category")
    val VSError = conf.getString("report.vs-error.category")
    val NoVal = conf.getString("report.vs-no-validation.category")
    val CodedElement = conf.getString("report.coded-element.category")

  }

  object Templates {

    val Failure = conf.getString("report.failure.template")
    val PredicateFailure = conf.getString("report.predicate-failure.template")

    val EVS = conf.getString("report.evs.template")
    val PVS = conf.getString("report.pvs.template")
    val CodeNotFound = conf.getString("report.code-not-found.template")
    val VSNotFound = conf.getString("report.vs-not-found.template")
    val EmptyVS = conf.getString("report.empty-vs.template")
    val VSError = conf.getString("report.vs-error.template")
    val NoVal = conf.getString("report.vs-no-validation.template")
    val CodedElement = conf.getString("report.coded-element.template")
  }
}*/
