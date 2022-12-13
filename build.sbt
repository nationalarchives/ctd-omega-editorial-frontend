import sbt.Keys.{ libraryDependencies, publishMavenStyle }
import sbt.url
import de.heikoseeberger.sbtheader.FileType
import play.twirl.sbt.Import.TwirlKeys
import sbt.Keys.resolvers
import ReleaseTransformations._

val Slf4JVersion = "1.7.36"

organization := "uk.gov.nationalarchives"

ThisBuild / versionScheme := Some("semver-spec")

headerMappings := headerMappings.value + (FileType("html") -> HeaderCommentStyle.twirlStyleBlockComment)

Compile / headerSources ++= (Compile / TwirlKeys.compileTemplates / sources).value

lazy val root = Project("ctd-omega-editorial-frontend", file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(AutomateHeaderPlugin)
  .settings(
    Defaults.itSettings,
    organization := "uk.gov.nationalarchives",
    name := "ctd-omega-editorial-frontend",
    scalaVersion := "2.13.10",
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    homepage := Some(
      url("https://github.com/nationalarchives/ctd-omega-editorial-frontend")
    ),
    startYear := Some(2022),
    description := "Omega Editorial Frontend",
    organizationName := "The National Archives",
    organizationHomepage := Some(url("http://nationalarchives.gov.uk")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/nationalarchives/ctd-omega-editorial-frontend"),
        "scm:git@github.com:nationalarchives/ctd-omega-editorial-frontend.git"
      )
    ),
    developers := List(
      Developer(
        id = "enriquedelpino",
        name = "Enrique del Pino",
        email = "enrique@cicoders.com",
        url = url("http://www.cicoders.com")
      ),
      Developer(
        id = "rwalpole",
        name = "Rob Walpole",
        email = "rob.walpole@devexe.co.uk",
        url = url("http://www.devexe.co.uk")
      ),
      Developer(
        id = "userjd",
        name = "Jaishree Davey",
        email = "jjd@jdcode.com",
        url = url("http://www.jdcode.com")
      ),
      Developer(
        id = "adamretter",
        name = "Adam Retter",
        email = "adam@evolvedbinary.com",
        url = url("https://www.evolvedbinary.com")
      )
    ),
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "utf-8", // Specify character encoding used by source files.
      "-explaintypes", // Explain type errors in more detail.
      "-feature", // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
      "-language:experimental.macros", // Allow macro definition (besides implementation and application)
      "-language:higherKinds", // Allow higher-kinded types
      "-language:implicitConversions", // Allow definition of implicit functions called views
      "-unchecked", // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
//      "-Xfatal-warnings", // Fail the compilation if there are any warnings.
      "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
      "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
      "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
      "-Xlint:option-implicit", // Option.apply used implicit view.
      "-Xlint:package-object-classes", // Class or object defined in package object.
      "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
      "-Ywarn-dead-code", // Warn when dead code is identified.
      "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
      "-Ywarn-numeric-widen", // Warn when numerics are widened.
//      "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
//      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
//      "-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:params", // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates", // Warn if a private member is unused.
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.  //
      "-release:8",
      "-encoding",
      "utf-8"
    ),
    resolvers ++= Seq(
      Resolver.mavenLocal,
      MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
    ),
    headerLicense := Some(HeaderLicense.MIT("2022", "The National Archives")),
    libraryDependencies ++= Seq(
      guice,
      "org.webjars.npm"         % "govuk-frontend"     % "4.3.1",
      "uk.gov.hmrc"            %% "play-frontend-hmrc" % "3.30.0-play-28",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0"  % Test,
      "org.jsoup"              % "jsoup"               % "1.15.3" % Test
    ),
    publishMavenStyle := true,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots/")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2/")
    },
    releaseCrossBuild := false,
    releaseVersionBump := sbtrelease.Version.Bump.Minor,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommand("publishSigned"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "uk.gov.nationalarchives.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "uk.gov.nationalarchives.binders._"
