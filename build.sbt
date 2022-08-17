import Dependencies._
import sbt._
import Keys._

ThisBuild / organization  := "gov.nist"
ThisBuild / version       := "1.6.0-SNAPSHOT"
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

lazy val root = project.in(file(".")).aggregate(profile, parser, validation)

// ============================ PROFILE MODULE ==========================

lazy val profile = Project("hl7-v2-profile", file("profile"))
  .settings(libraryDependencies ++= Seq(
    xom,
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
      jx_core,
      vreport,
      spray_json
    )
  )

lazy val playground = Project("hl7-v2-validation-playground", file("playground/scala"))
  .enablePlugins(PlayScala)
  .dependsOn(validation % "test->test; compile->compile")
  .settings(
    name := """hl7-v2-validation-playground""",
    organization := "gov.nist",
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      json4s
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )