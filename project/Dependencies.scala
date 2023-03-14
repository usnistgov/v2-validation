import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
    "HIT Nexus" at "https://hit-nexus.nist.gov/repository/releases"
  )

  val jacksonVersion = "2.13.4" // or 2.12.7
  val jacksonDatabindVersion = "2.13.4.2" // or 2.12.7.1

  val jacksonOverrides = Seq(
    "com.fasterxml.jackson.core" % "jackson-core",
    "com.fasterxml.jackson.core" % "jackson-annotations",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310"
  ).map(_ % jacksonVersion)

  val jacksonDatabindOverrides = Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonDatabindVersion
  )

  val akkaSerializationJacksonOverrides = Seq(
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor",
    "com.fasterxml.jackson.module" % "jackson-module-parameter-names",
    "com.fasterxml.jackson.module" %% "jackson-module-scala",
  ).map(_ % jacksonVersion)


  val schemas = "gov.nist.hit" % "hl7-v2-schemas" % "1.6.0"
  val xom = "xom" % "xom" % "1.3.7"
  val stringUtils = "org.apache.commons" % "commons-lang3" % "3.12.0"
  val typesafe_config = "com.typesafe"                  % "config"                % "1.4.2"
  val xmlUtil       = "gov.nist"                      %  "xml-util"               % "2.1.0"
  val spec2 = "org.specs2" %% "specs2-core" % "4.19.2"
  val spec2sc = "org.specs2" %% "specs2-scalacheck" % "4.19.2"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.17.0"
  val vreport       = "com.github.hl7-tools"          %   "validation-report"     %  "1.1.0"
  val scala_xml     = "org.scala-lang.modules"        %%   "scala-xml"        %  "1.3.0"
  val json4s = "org.json4s" %% "json4s-jackson" % "4.0.5"

}
