lazy val scala213 = "2.13.5"
lazy val scala212 = "2.12.13"
lazy val supportedScalaVersions = List(scala213, scala212)

ThisBuild / organization := "io.grigg"
ThisBuild / version      := "0.1.0"
ThisBuild / scalaVersion := scala213

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
    "-Xlint:-unused,_"
  )
)

lazy val scalamyth = project.in(file("."))
  .aggregate(bindings, examples)
  .settings(commonSettings, crossScalaVersions := Nil)

lazy val bindings = project.in(file("bindings"))
  .settings(commonSettings)
  .settings(
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules"     %% "scala-collection-compat" % "2.3.1",
      "org.scala-lang.modules"     %% "scala-xml"       % "1.3.0",
      "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.3",
      "io.spray"                   %% "spray-json"      % "1.3.6",
      "ch.qos.logback"              % "logback-classic" % "1.2.3",
      "net.straylightlabs"          % "hola"            % "0.2.3",
      "org.scalameta"              %% "munit"           % "0.7.25" % Test,
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )

lazy val examples = project.in(file("examples"))
  .dependsOn(bindings)
  .settings(commonSettings)
  .settings(
    crossScalaVersions := supportedScalaVersions
  )
