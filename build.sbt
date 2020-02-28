import Dependencies._
import sbt._
import Keys._

ThisBuild / organization  := "gov.nist"
ThisBuild / version       := "1.1.18"
ThisBuild / scalaVersion  := "2.12.10"
ThisBuild / resolvers    ++= Dependencies.resolutionRepos
ThisBuild / scalacOptions := Seq(
  "-encoding", "utf8",
  "-feature",
  "-unchecked",
  "-deprecation",
  "-target:jvm-1.7",
  "-language:_",
  "-Dconfig.trace=loads"
)

lazy val root = project.in(file(".")).aggregate(profile, parser, validation)

// ============================ PROFILE MODULE ==========================

lazy val profile = Project("hl7-v2-profile", file("profile"))
  .settings(libraryDependencies ++= Seq(
    `xml-util`,
    scala_xml,
    sext,
    spec2)
  )

// ============================ PARSER MODULE ============================

lazy val parser = Project("hl7-v2-parser", file("parser"))
  .dependsOn(profile % "test->test; compile->compile")

// ============================ VALIDATION MODULE ========================

lazy val validation = Project("hl7-v2-validation", file("validation"))
  .dependsOn(parser % "test->test; compile->compile")
  .enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "buildinfo",
    buildInfoObject := "Info",
    libraryDependencies ++= Seq(
      typesafe_config,
      stringUtils,
      vreport,
      json
    )
  )