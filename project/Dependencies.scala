import sbt._

object Dependencies {

  val resolutionRepos = Seq(
    "jitpack.io" at "https://jitpack.io",
    "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val config         = "com.typesafe"        % "config"       % "1.2.1"
  val `xml-util`     = "gov.nist"            %  "xml-util"    % "1.0.1-SNAPSHOT"
  val junit         = "junit"                 %   "junit"             %  "4.11"
  val spec2         = "org.specs2"            %%  "specs2"            %  "3.7"
  val vreport       = "com.github.hl7-tools"  %   "validation-report" %  "1.0.0"
  val scala_xml       = "org.scala-lang.modules"  %   "scala-xml_2.11" %  "1.0.6"
  val stringUtils = "org.apache.commons" % "commons-lang3" % "3.4"
}

