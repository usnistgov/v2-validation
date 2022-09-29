import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "jitpack.io" at "https://jitpack.io",
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )

  val xom = "xom" % "xom" % "1.3.7"
  val stringUtils = "org.apache.commons" % "commons-lang3" % "3.12.0"
  val typesafe_config = "com.typesafe"                  % "config"                % "1.4.2"
  val xmlUtil       = "gov.nist"                      %  "xml-util"               % "2.0.0"
  val spec2         = "org.specs2"                    %%  "specs2"                %  "2.5"
  val vreport       = "com.github.hl7-tools"          %   "validation-report"     %  "1.1.0"
  val scala_xml     = "org.scala-lang.modules"        %%   "scala-xml"        %  "1.2.0"
  val json4s = "org.json4s" %% "json4s-jackson" % "4.0.5"

}
