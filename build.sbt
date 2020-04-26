import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

addCompilerPlugin(kindProjector cross CrossVersion.full)
//addCompilerPlugin(betterMonadicFor cross CrossVersion.full)

lazy val root = (project in file("."))
  .settings(
    name := "zio_experiments",
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC18-2",
    libraryDependencies += scalaTest % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
