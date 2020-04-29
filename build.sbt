import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

addCompilerPlugin(kindProjector cross CrossVersion.full)
addCompilerPlugin(betterMonadicFor)

lazy val root = (project in file("."))
  .settings(
    name := "zio_experiments",
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= openTracing,
    libraryDependencies ++= jaegerTracer,
    libraryDependencies ++= zipkin,
    libraryDependencies ++= zio
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
