import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "jitpack.io" at "https://jitpack.io"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val config         = "com.typesafe"        % "config"       % "1.2.1"
  val `xml-util`     = "gov.nist"            %  "xml-util"    % "1.0.1-SNAPSHOT"
  //val json4sJackson  = "org.json4s"          %% "json4s-jackson"   % "3.2.11"
  //val sprayJson      = "io.spray"            %% "spray-json"       % "1.3.1"
  //val joda           = "joda-time"            %   "joda-time"        % "2.6"
  //val `joda-convert` = "org.joda"             %   "joda-convert"     % "1.7"

  val junit         = "junit"               %   "junit"             %  "4.11"
  val spec2         = "org.specs2"          %%  "specs2"            %  "2.3.11"
  val scalaCheck    = "org.scalacheck"      %%  "scalacheck"        %  "1.11.3"
  val vreport       = "com.github.salifou"  %   "validation-report" % "1.0.0"

}
