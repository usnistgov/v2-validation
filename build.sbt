import Dependencies._
import sbt._
import Keys._

ThisBuild / organization  := "gov.nist"
ThisBuild / version       := "1.6.3-SNAPSHOT"
ThisBuild / scalaVersion  := "2.12.10"
ThisBuild / resolvers    ++= Dependencies.resolutionRepos
ThisBuild / scalacOptions := Seq(
  "-encoding", "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-target:jvm-1.8",
  "-language:_",
  "-Dconfig.trace=loads",
  "-J-Xmx2g"
)

lazy val moduleSettings =
    Seq(
      crossPaths := false,
      publishMavenStyle := true,
      credentials += Credentials(Path.userHome / ".nexusCredentials"),
      publishTo := {
        val nexus = "http://hit-dev-admin.nist.gov:9001/"
        if (version.value.trim.endsWith("SNAPSHOT"))
          Some(("snapshots" at nexus + "repository/snapshots").withAllowInsecureProtocol(true))
        else
          Some(("releases" at nexus + "repository/releases").withAllowInsecureProtocol(true))
      }
    )

lazy val noPublishing = Seq(
  publish := None,
  publishLocal := None,
  publishTo := None
)

lazy val root = project.in(file(".")).aggregate(profile, parser, validation).settings(noPublishing: _*)

// ============================ PROFILE MODULE ==========================

lazy val profile = Project("hl7-v2-profile", file("profile"))
  .settings(moduleSettings: _*)
  .settings(libraryDependencies ++= Seq(
    xom,
    xmlUtil,
    scala_xml,
    spec2)
  )

// ============================ PARSER MODULE ============================

lazy val parser = Project("hl7-v2-parser", file("parser"))
  .settings(moduleSettings: _*)
  .dependsOn(profile % "test->test; compile->compile")

// ============================ VALIDATION MODULE ========================

lazy val validation = Project("hl7-v2-validation", file("validation"))
  .settings(moduleSettings: _*)
  .dependsOn(parser % "test->test; compile->compile")
  .enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "buildinfo",
    buildInfoObject := "Info",
    libraryDependencies ++= Seq(
      typesafe_config,
      stringUtils,
      vreport
    )
  )

lazy val playground = Project("hl7-v2-validation-playground", file("playground/scala"))
  .settings(noPublishing: _*)
  .enablePlugins(PlayScala)
  .dependsOn(validation % "test->test; compile->compile")
  .settings(
    name := """hl7-v2-validation-playground""",
    organization := "gov.nist",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      guice,
      json4s,

    ) ++ jacksonDatabindOverrides ++ jacksonOverrides ++ akkaSerializationJacksonOverrides,
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )