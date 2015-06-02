/*package hl7.v2.validation.report

import com.typesafe.config.ConfigFactory

object Configurations {

  val conf = ConfigFactory.load

  object Classifications {
    val RUsage  = conf.getString("report.r-usage.classification")
    val XUsage  = conf.getString("report.x-usage.classification")
    val WUsage  = conf.getString("report.w-usage.classification")
    val REUsage = conf.getString("report.re-usage.classification")
    val MinCard = conf.getString("report.cardinality.classification")
    val MaxCard = conf.getString("report.cardinality.classification")
    val Length  = conf.getString("report.length.classification")
    val Format  = conf.getString("report.format.classification")
    val Extra   = conf.getString("report.extra.classification")
    val Unescaped  = conf.getString("report.unescaped.classification")
    val Unexpected = conf.getString("report.unexpected.classification")
    val Invalid    = conf.getString("report.invalid.classification")

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
    val RUsage  = conf.getString("report.r-usage.category")
    val XUsage  = conf.getString("report.x-usage.category")
    val WUsage  = conf.getString("report.w-usage.category")
    val REUsage = conf.getString("report.re-usage.category")
    val MinCard = conf.getString("report.cardinality.category")
    val MaxCard = conf.getString("report.cardinality.category")
    val Length  = conf.getString("report.length.category")
    val Format  = conf.getString("report.format.category")
    val Extra   = conf.getString("report.extra.category")
    val Unescaped  = conf.getString("report.unescaped.category")
    val Unexpected = conf.getString("report.unexpected.category")
    val Invalid    = conf.getString("report.invalid.category")

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
    val RUsage  = conf.getString("report.r-usage.template")
    val XUsage  = conf.getString("report.x-usage.template")
    val WUsage  = conf.getString("report.w-usage.template")
    val REUsage = conf.getString("report.re-usage.template")
    val MinCard = conf.getString("report.cardinality.template")
    val MaxCard = conf.getString("report.cardinality.template")
    val Length  = conf.getString("report.length.template")
    val Format  = conf.getString("report.format.template")
    val Extra   = conf.getString("report.extra.template")
    val Unescaped  = conf.getString("report.unescaped.template")
    val Unexpected = conf.getString("report.unexpected.template")
    val Invalid    = conf.getString("report.invalid.template")

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

}
*/