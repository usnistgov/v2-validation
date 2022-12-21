import sbt._
import Keys._

object BuildSettings {

  lazy val basicSettings = Seq(

    version              := "1.5.4",
    //FIXME homepage             := Some(new URL("xxx")),
    organization         := "gov.nist",
    organizationHomepage := Some(new URL("https://nist.gov")),
    description          := "A suite of libraries for parsing and validating " +
                            "HL7 v2xx messages and conformance profile",
    startYear            := Some(2013),
    //licenses             := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion         := "2.11.5",
    resolvers            ++= Dependencies.resolutionRepos,
    scalacOptions        := Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-target:jvm-1.7",
      "-language:_",
      "-Yinline-warnings"/*,
      "-Dconfig.trace=loads"*/
    )
  )

  lazy val moduleSettings =
    basicSettings ++
      Seq(
        crossPaths        := false,
        publishMavenStyle := true,
        credentials       += Credentials(Path.userHome / ".nexusCredentials"),
        publishTo         := {
          val nexus = "http://hit-dev-admin.nist.gov:9001/"
          if (version.value.trim.endsWith("SNAPSHOT"))
            Some("snapshots" at nexus + "repository/snapshots")
          else
            Some("releases"  at nexus + "repository/releases")
        }
      )

  lazy val noPublishing = Seq(
    publish      := (),
    publishLocal := (),
    publishTo    := None
  )

}
