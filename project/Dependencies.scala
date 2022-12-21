import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "jitpack.io" at "https://jitpack.io",
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )

  val typesafeConfig        = "com.typesafe"        % "config"       % "1.2.1"
  val xmlUtil    = "gov.nist"            %  "xml-util"    % "2.1.0"
  val junit         = "junit"                 %   "junit"             %  "4.11"
  val spec2         = "org.specs2"            %%  "specs2-core"       %  "4.19.0"
  val spec2sc       = "org.specs2"            %%  "specs2-scalacheck" %  "4.19.0"
  val scalaCheck    = "org.scalacheck"        %%  "scalacheck"        %  "1.14.0"
  val vreport       = "com.github.hl7-tools"  %   "validation-report" %  "1.1.0"
  val scala_xml      = "org.scala-lang.modules"  %%   "scala-xml" %  "1.3.0"
  val stringUtils    = "org.apache.commons" % "commons-lang3" % "3.4"
}

