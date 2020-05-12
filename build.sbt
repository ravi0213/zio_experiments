import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.abp"
ThisBuild / organizationName := "abp"

git.useGitDescribe := true
lazy val buildInfoSettings = Seq(
  buildInfoKeys ++= Seq[BuildInfoKey](git.gitHeadCommit),
  buildInfoPackage := "io.abp.users"
)

lazy val root = (project in file("."))
  .settings(
    name := "zio_experiments",
    skip in publish := true
  )
  .aggregate(users)

lazy val users = (project in file("users"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "users",
    resolvers += Resolver.sonatypeRepo("releases"),
    buildInfoSettings,
    libraryDependencies ++= circe,
    libraryDependencies ++= http4s,
    libraryDependencies ++= jaegerTracer,
    libraryDependencies += logback,
    libraryDependencies ++= openTracing,
    libraryDependencies ++= zio,
    libraryDependencies ++= zipkin,
    addCompilerPlugin(kindProjector cross CrossVersion.full),
    addCompilerPlugin(betterMonadicFor)
  )

Global / onChangedBuildSource := ReloadOnSourceChanges
Global / testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
