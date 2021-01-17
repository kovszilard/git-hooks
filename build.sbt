import Dependencies._

ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .enablePlugins(NativeImagePlugin)
  .settings(
    name := "git-hooks",
    libraryDependencies ++= Seq(zio, zioStreams),
    libraryDependencies ++= zioTest,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    // Compile / mainClass := Some("example.Hello"),
    nativeImageOptions ++= List(
      "--initialize-at-build-time",
      "--no-fallback"
      )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
