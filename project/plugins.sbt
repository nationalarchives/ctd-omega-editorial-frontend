addSbtPlugin("com.typesafe.play"        % "sbt-plugin"          % "2.8.20")
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.16.2")
addSbtPlugin("com.eed3si9n"             % "sbt-buildinfo"       % "0.11.0")
addSbtPlugin("org.scalameta"            % "sbt-scalafmt"        % "2.5.2")
addSbtPlugin("de.heikoseeberger"        % "sbt-header"          % "5.10.0")
addSbtPlugin("io.github.irundaia"       % "sbt-sassify"         % "1.5.2")
addSbtPlugin("com.github.sbt"           % "sbt-release"         % "1.1.0")
addSbtPlugin("org.scoverage"            % "sbt-scoverage"       % "2.0.9")

// This will prevent various version conflicts. You'll also find it in build.sbt, where it also necessary.
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
