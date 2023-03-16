lazy val scala213 = "2.13.8"
lazy val scala212 = "2.12.16"
lazy val supportedScalaVersions = List(scala213, scala212)

ThisBuild / scalaVersion := scala213
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / versionPolicyIntention := Compatibility.None

ThisBuild / licenses := List(
  "LGPL-2.1" -> url("https://opensource.org/licenses/LGPL-2.1"),
  "BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")
)

ThisBuild / organization := "io.grigg"
ThisBuild / homepage := Some(url("https://github.com/griggt/scalamyth"))
ThisBuild / developers := List(
  Developer(
    "griggt",
    "Tom Grigg",
    "tom@grigg.io",
    url("https://github.com/griggt"),
  )
)
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / testOptions += Tests.Argument(TestFramework("munit.Framework"), "+l")

Compile / unmanagedSourceDirectories += {
  val sourceDir = (Compile / sourceDirectory).value
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13"
    case _                       => sourceDir / "scala-2.12"
  }
}

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-Xlint:-unused,_"
  )
)

lazy val noPublishSettings = Seq(
  publish / skip := true,
)

lazy val root = project.in(file("."))
  .aggregate(bindings, examples)
  .settings(noPublishSettings)

lazy val bindings = project.in(file("bindings"))
  .settings(commonSettings)
  .settings(
    name := "scalamyth",
    description := "Scala bindings for MythTV",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules"     %% "scala-collection-compat" % "2.8.0",
      "org.scala-lang.modules"     %% "scala-xml"       % "2.1.0",
      "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5",
      "io.spray"                   %% "spray-json"      % "1.3.6",
      "ch.qos.logback"              % "logback-classic" % "1.4.6",
      "net.straylightlabs"          % "hola"            % "0.2.3",
      "org.scalameta"              %% "munit"           % "0.7.29" % Test,
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    Compile / doc / scalacOptions ++= Seq(
      "-doc-title", "MythTV Scala Bindings",
      "-doc-version", version.value,
    ),
  )

lazy val examples = project.in(file("examples"))
  .dependsOn(bindings)
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(
    crossScalaVersions := supportedScalaVersions,
  )
