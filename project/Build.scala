import sbt._
import Keys._

object Build extends Build {

  import Dependencies._

  lazy val profile = project
    .settings( libraryDependencies ++= compile(`xml-util`) ++ test( spec2 ) )

  lazy val parser = project.dependsOn( profile % "test->test; compile->compile" )
                           .settings( libraryDependencies ++= compile(joda) ++ compile(`joda-convert`) )

  lazy val `validation` = project.dependsOn( parser % "test->test; compile->compile" )

}
