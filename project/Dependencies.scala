import sbt._

object Dependencies {
  val zioVersion = "1.0.3"

  lazy val zio = "dev.zio" %% "zio" % zioVersion
  lazy val zioStreams = "dev.zio" %% "zio-streams" % zioVersion

  lazy val zioTest = Seq(
    "dev.zio" %% "zio-test"          % zioVersion % "test",
    "dev.zio" %% "zio-test-sbt"      % zioVersion % "test",
    "dev.zio" %% "zio-test-magnolia" % zioVersion % "test", // optional
    "dev.zio" %% "zio-test-intellij" % zioVersion % "test"
  )

}
