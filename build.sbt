ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ymacro-annotations",
  "-Ywarn-unused"
)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val root = (project in file("."))
  .settings(
    name := "zio-project-playground"
  )
  .aggregate(server, app)
  .dependsOn(server, app)

lazy val common = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("common"))
  .settings(
    name := "zio-project-playground-common",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %%% "tapir-core"     % Dependencies.tapirVersion,
      "com.softwaremill.sttp.tapir" %%% "tapir-json-zio" % Dependencies.tapirVersion // brings in zio-json

    )
  )

lazy val server = (project in file("server"))
  .configs(IntegrationTest)
  .settings(
    name := "zio-project-playground-server",
    libraryDependencies ++= Dependencies.server,
    Defaults.itSettings,
    fork := true
  )
  .dependsOn(common.jvm)

lazy val app = (project in file("app"))
  .settings(
    name                            := "zio-project-playground-app",
    libraryDependencies ++= Seq(
//      "com.raquo"     %%% "laminar"    % "0.14.5",
      "io.frontroute" %%% "frontroute" % "0.16.1" // Brings in Laminar
    ),
    scalaJSUseMainModuleInitializer := true,
    Compile / mainClass             := Some("Main")
  )
  .dependsOn(common.js)
  .enablePlugins(ScalaJSPlugin)

addCommandAlias("fmt", "all root/scalafmtSbt root/scalafmtAll")
addCommandAlias("fmtCheck", "all root/scalafmtSbtCheck root/scalafmtCheckAll")
addCommandAlias(
  "fix",
  "root/scalafixAll RemoveUnused"
)
