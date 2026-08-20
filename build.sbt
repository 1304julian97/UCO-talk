// Scala version
ThisBuild / scalaVersion := "3.8.4"

// Organization
ThisBuild / organization := "co.edu.uco"

// Dependencies versions
val catsVersion = "2.13.0"
val catsEffectVersion = "3.7.0"
val circeVersion = "0.14.16"
val fs2Version = "3.13.0"
val http4sVersion = "0.23.34"
val ironVersion = "3.3.2"

// Common settings
lazy val commonSettings = Seq(
  // Ensure we publish an artifact linked to the appropriate Java std library
  //scalacOptions += "-java-output-version:21",
  // Silence "unused explicit parameter" (E198), and make all other warnings verbose
  scalacOptions += "-Wconf:any:verbose,id=E198:silent",
  // Base dependencies
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core"   % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "co.fs2"        %% "fs2-core"    % fs2Version,
    "co.fs2"        %% "fs2-io"      % fs2Version
  )
)

// Domain module
lazy val domain =
  project
    .in(file("modules/domain"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "io.circe"           %% "circe-core"          % circeVersion,
        "org.http4s"         %% "http4s-circe"        % http4sVersion,
        "org.http4s"         %% "http4s-client"       % http4sVersion,
        "org.http4s"         %% "http4s-ember-client" % http4sVersion,
        "io.github.iltotore" %% "iron"                % ironVersion
      )
    )

// Server module
lazy val server =
  project
    .in(file("modules/server"))
    .dependsOn(domain)
    .settings(commonSettings)
    .settings(
      run / fork := true,
      libraryDependencies ++= Seq(
        "org.http4s" %% "http4s-server"       % http4sVersion,
        "org.http4s" %% "http4s-ember-server" % http4sVersion,
        "org.http4s" %% "http4s-dsl"          % http4sVersion,
        "org.http4s" %% "http4s-circe"        % http4sVersion,
        "io.circe"   %% "circe-core"          % circeVersion,
        "io.circe"   %% "circe-generic"       % circeVersion,
        "io.circe"   %% "circe-parser"        % circeVersion
      )
    )

// Root module
lazy val root =
  project
    .in(file("."))
    .aggregate(
      domain,
      server
    )

// Format check, compile, and test in one step
addCommandAlias("validate", "scalafmtCheckAll; compile; test")
