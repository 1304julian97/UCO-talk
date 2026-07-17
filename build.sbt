// Scala version.
ThisBuild / scalaVersion := "3.8.4"

// Organization
ThisBuild / organization := "co.edu.uco"

// Dependencies versions.
val catsVersion = "2.13.0"
val catsEffectVersion = "3.7.0"
val fs2Version = "3.13.0"
val http4sVersion = "0.23.34"
val munitVersion = "1.1.0"

// Common settings.
lazy val commonSettings = Seq(
  // Ensure we publish an artifact linked to the appropriate Java std library.
  scalacOptions += "-java-output-version:21",
  // Make all warnings verbose.
  scalacOptions += "-Wconf:any:verbose",
  // Base dependencies.
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion,
    "co.fs2" %% "fs2-core" % fs2Version,
    "co.fs2" %% "fs2-io" % fs2Version
  )
)

// Core module.
lazy val core =
  project
    .in(file("modules/core"))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scalameta" %% "munit" % munitVersion % Test,
        "org.scalameta" %% "munit-scalacheck" % munitVersion % Test
      )
    )

// Server module.
lazy val server =
  project
    .in(file("modules/server"))
    .dependsOn(core)
    .settings(commonSettings)
    .settings(
      run / fork := true,
      libraryDependencies ++= Seq(
        "org.http4s" %% "http4s-server" % http4sVersion,
        "org.http4s" %% "http4s-ember-server" % http4sVersion
      )
    )

// Root module.
lazy val root =
  project
    .in(file("."))
    .aggregate(
      core,
      server
    )

// Format check, compile, and test in one step.
addCommandAlias("validate", "scalafmtCheckAll; compile; test")
