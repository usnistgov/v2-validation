import Dependencies._
import sbt._
import Keys._

ThisBuild / organization  := "gov.nist"
ThisBuild / version       := "1.5.5"
ThisBuild / scalaVersion  := "2.13.10"
ThisBuild / resolvers    ++= Dependencies.resolutionRepos
ThisBuild / scalacOptions := Seq(
  "-encoding", "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-language:_"
)

lazy val moduleSettings =
  Seq(
    crossPaths := false,
    publishMavenStyle := true,
    credentials += Credentials(Path.userHome / ".nexusCredentials"),
    publishTo := {
      val nexus = "https://hit-nexus.nist.gov/"
      if (version.value.trim.endsWith("SNAPSHOT"))
        Some(("snapshots" at nexus + "repository/snapshots"))
      else
        Some(("releases" at nexus + "repository/releases"))
    }
  )

lazy val noPublishing = Seq(
  publish := None,
  publishLocal := None,
  publishTo := None
)

lazy val root = project.in(file(".")).aggregate(profile, parser, validation).settings(noPublishing: _*)

//----------------------------------------------------------------------------
// Modules
//----------------------------------------------------------------------------

lazy val profile = Project("hl7-v2-profile", file("profile"))
  .settings(moduleSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      xmlUtil,
      scala_xml,
      spec2 % "test",
      scalaCheck % "test",
      spec2sc % "test"
    )
  )

lazy val parser = Project("hl7-v2-parser", file("parser"))
  .dependsOn(profile % "test->test; compile->compile")
  .settings(moduleSettings: _*)

lazy val validation = Project("hl7-v2-validation", file("validation"))
  .settings(moduleSettings: _*)
  .dependsOn(parser % "test->test; compile->compile")
  .enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "buildinfo",
    buildInfoObject := "Info",
    libraryDependencies ++= Seq(
      typesafeConfig,
      stringUtils,
      vreport
    )
  )
