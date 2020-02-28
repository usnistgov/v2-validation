import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "jitpack.io" at "https://jitpack.io",
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )

//  val jx_core       = "com.fasterxml.jackson.core"    %  "jackson-databind"       % "2.2.2"
//  val jx_md         = "com.fasterxml.jackson.module"  %% "jackson-module-scala"   % "2.8.4"
//  val scalaCheck    = "org.scalacheck"                %%  "scalacheck"            %  "1.11.3"
//  val junit         = "junit"                         %   "junit"                 %  "4.11"
  val json = "io.spray" %%  "spray-json" % "1.3.4"
  val stringUtils = "org.apache.commons" % "commons-lang3" % "3.4"
  val typesafe_config = "com.typesafe"                  % "config"                  % "1.2.1"
  val `xml-util`    = "gov.nist"                      %  "xml-util"               % "1.0.1-SNAPSHOT"
  val spec2         = "org.specs2"                    %%  "specs2"                %  "2.4.17"
  val vreport       = "com.github.hl7-tools"          %   "validation-report"     %  "1.0.0"
  val scala_xml     = "org.scala-lang.modules"        %%   "scala-xml"        %  "1.0.6"
  val sext          = "com.github.nikita-volkov"      % "sext"                    % "0.2.4"
}
