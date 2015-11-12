import sbt._
import Keys._
import sbtbuildinfo.Plugin._

object Build extends Build {

  import BuildSettings._
  import Dependencies._

  // configure prompt to show current project
  override lazy val settings = super.settings :+ {
    shellPrompt := { s => Project.extract(s).currentProject.id + " > " }
  }

  //----------------------------------------------------------------------------
  // Root Project
  //----------------------------------------------------------------------------

  lazy val root = Project("root", file("."))
    .aggregate(profile, parser, `validation`)
    .settings(basicSettings: _*)
    .settings(noPublishing: _*)

  //----------------------------------------------------------------------------
  // Modules
  //----------------------------------------------------------------------------

  lazy val profile = Project("hl7-v2-profile", file("profile"))
    .settings(basicSettings: _*)
    .settings(moduleSettings: _*)
    .settings(libraryDependencies ++=
      compile(`xml-util`) ++
      test(spec2))

  lazy val parser = Project("hl7-v2-parser", file("parser"))
    .dependsOn(profile % "test->test; compile->compile")
    .settings(basicSettings: _*)
    .settings(moduleSettings: _*)

  lazy val validation = Project("hl7-v2-validation", file("validation"))
    .dependsOn(parser % "test->test; compile->compile")
    .settings(basicSettings ++ Seq(sourceGenerators in Compile <+= (sourceManaged in Compile, version, name) map { (d, v, n) =>
      val file = d / "info.scala"
      IO.write(file, """package buildinfo
    |object Info {
    |  val version = "%s"
    |  val name = "%s"
    |}
    |""".stripMargin.format(v, n))
      Seq(file)
    }): _*)
    .settings(moduleSettings: _*)
    .settings(libraryDependencies ++= compile(config) ++ compile(vreport))

}
