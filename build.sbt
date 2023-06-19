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

lazy val IntegrationTestConfig = config("it") extend Test
IntegrationTestConfig / scalaSource := baseDirectory.value / "/it"

lazy val root = Project("ctd-omega-editorial-frontend", file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(AutomateHeaderPlugin)
  .configs(IntegrationTest extend Test)
  .settings(
    Defaults.itSettings,
    organization := "uk.gov.nationalarchives",
    name := "ctd-omega-editorial-frontend",
    maintainer := "webmaster@nationalarchives.gov.uk",
    scalaVersion := "2.13.10",
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    homepage := Some(
      url("https://github.com/nationalarchives/ctd-omega-editorial-frontend")
    ),
    startYear := Some(2022),
    description := "Omega Editorial Frontend",
    organizationName := "The National Archives",
    organizationHomepage := Some(url("http://nationalarchives.gov.uk")),
    githubOwner := "nationalarchives",
    githubRepository := "ctd-omega-editorial-frontend",
    githubTokenSource := TokenSource.Or(
      TokenSource.Environment("GITHUB_TOKEN"),
      TokenSource.GitConfig("github.token") //  ~/.gitconfig
    ),
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
      ),
      Developer(
        id = "felixgb",
        name = "Felix Bowman",
        email = "felix@enceladus.software",
        url = url("https://www.linkedin.com/in/felix-bowman-75b26913a/")
      ),
      Developer(
        id = "tna-erasmos",
        name = "Sean Rasmussen",
        email = "tna@erasmos.com",
        url = url("https://www.linkedin.com/in/erasmos//")
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
      "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
      "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals", // Warn if a local definition is unused.
      "-Ywarn-unused:params", // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates", // Warn if a private member is unused.
      "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
      "-Wconf:src=target/.*:s", // Exclude templates from any checks.
      "-release:8",
      "-encoding",
      "utf-8"
    ),
    resolvers ++= Seq(
      Resolver.mavenLocal,
      MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
    ),
    githubTokenSource := TokenSource.Or(
      TokenSource.Environment("GITHUB_TOKEN"),
      TokenSource.GitConfig("github.token") //  ~/.gitconfig
    ),
    headerLicense := Some(HeaderLicense.MIT("2022", "The National Archives")),
    libraryDependencies ++= Seq(
      guice,
      "com.github.pureconfig"  %% "pureconfig"                    % "0.17.4",
      "uk.gov.nationalarchives.thirdparty.dev.fpinbo" %% "jms4s-simple-queue-service" % "0.5.0-TNA-OMG-0.1.0",
      "org.typelevel"          %% "cats-core"                     % "2.9.0",
      "org.typelevel"          %% "cats-effect"                   % "3.4.8",
      "org.typelevel"          %% "cats-effect-kernel"            % "3.4.7",
      "org.typelevel"          %% "log4cats-core"                 % "2.5.0",
      "org.typelevel"          %% "log4cats-slf4j"                % "2.5.0",
      "org.webjars.npm"         % "govuk-frontend"                % "4.3.1",
      "uk.gov.hmrc"            %% "play-frontend-hmrc"            % "6.2.0-play-28",
      "com.lihaoyi"            %% "pprint"                        % "0.8.1",
      "com.beachape"           %% "enumeratum-play-json"          % "1.7.2",
      "org.scalatestplus.play" %% "scalatestplus-play"            % "5.1.0"   % Test,
      "org.jsoup"               % "jsoup"                         % "1.15.4"  % Test,
      "org.typelevel"          %% "cats-effect-testing-scalatest" % "1.5.0"   % Test,
      "org.mockito"            %% "mockito-scala-scalatest"       % "1.17.12" % Test,
      "org.mockito"            %% "mockito-scala-cats"            % "1.17.12" % Test
    ).map(_.exclude("org.slf4j", "*")),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.3.5" // Java 8 compatible
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

// Integration tests are automatically included, as the IntegrationTest config is extended from Test.
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

// If set to 'true', we might have intermittent test failures due to how MonitoredApiConnector is currently implemented.
Test / parallelExecution := false

//
// Test Coverage
//

// This will prevent the version conflict with Twirl, when running coverage.
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

// This will include coverage generation for both unit and integration tests.
coverageEnabled := true

val packagesExcludedFromCoverageCheck = Seq(
  ".*Reverse.*",
  "uk.gov.nationalarchives.omega.editorial.views.html.*",
  "router.*"
)

coverageExcludedPackages := packagesExcludedFromCoverageCheck.mkString(";")
