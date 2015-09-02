import sbt._
import Keys._

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
    .settings(publishM2Configuration <<= (packagedArtifacts, checksums in publish, ivyLoggingLevel) map { (arts, cs, level) => 
      Classpaths.publishConfig(arts, None, resolverName = m2Repo.name, checksums = cs, logging = level) 
   }, 
   publishM2 <<= Classpaths.publishTask(publishM2Configuration, deliverLocal), 
   otherResolvers += m2Repo )

  //----------------------------------------------------------------------------
  // Modules
  //----------------------------------------------------------------------------

  lazy val profile = Project("edi-profile", file("profile"))
    .settings(basicSettings: _*)
    .settings(moduleSettings: _*)
    .settings( libraryDependencies ++=
        compile(`xml-util`) ++
        test( spec2 )
    )
    .settings(publishM2Configuration <<= (packagedArtifacts, checksums in publish, ivyLoggingLevel) map { (arts, cs, level) => 
      Classpaths.publishConfig(arts, None, resolverName = m2Repo.name, checksums = cs, logging = level) 
   }, 
   publishM2 <<= Classpaths.publishTask(publishM2Configuration, deliverLocal), 
   otherResolvers += m2Repo )

  lazy val parser = Project("edi-parser", file("parser"))
    .dependsOn( profile % "test->test; compile->compile" )
    .settings(basicSettings: _*)
    .settings(moduleSettings: _*)
    .settings(publishM2Configuration <<= (packagedArtifacts, checksums in publish, ivyLoggingLevel) map { (arts, cs, level) => 
      Classpaths.publishConfig(arts, None, resolverName = m2Repo.name, checksums = cs, logging = level) 
   }, 
   publishM2 <<= Classpaths.publishTask(publishM2Configuration, deliverLocal), 
   otherResolvers += m2Repo )

  lazy val validation = Project("edi-validation", file("validation"))
    .dependsOn( parser % "test->test; compile->compile" )
    .settings(basicSettings: _*)
    .settings(moduleSettings: _*)
    .settings( libraryDependencies ++= compile( config ) ++ compile( vreport ))
    .settings(publishM2Configuration <<= (packagedArtifacts, checksums in publish, ivyLoggingLevel) map { (arts, cs, level) => 
      Classpaths.publishConfig(arts, None, resolverName = m2Repo.name, checksums = cs, logging = level) 
   }, 
   publishM2 <<= Classpaths.publishTask(publishM2Configuration, deliverLocal), 
   otherResolvers += m2Repo )

}
