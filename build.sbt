import sbt.Keys.libraryDependencies

organization := "uk.gov.nationalarchives"

version := "1.0.0-SNAPSHOT"
ThisBuild / versionScheme := Some("semver-spec")

lazy val root = Project("ctd-omega-editorial-frontend", file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    Defaults.itSettings,
    organization := "uk.gov.nationalarchives",
    name := "ctd-omega-editorial-frontend",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.13.9",
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    homepage := Some(
      url("https://github.com/nationalarchives/ctd-omega-services-prototype")
    ),


libraryDependencies ++= Seq( guice,
 "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
))
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "uk.gov.nationalarchives.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "uk.gov.nationalarchives.binders._"
