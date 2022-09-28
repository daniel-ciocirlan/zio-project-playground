ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.9"

ThisBuild / scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ymacro-annotations",
  "-Ywarn-unused"
)

ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / fork := true

val zioVersion        = "2.0.2"
val tapirVersion      = "1.1.0"
val zioLoggingVersion = "2.1.1"

lazy val libraries = Seq(
//  "dev.zio"                     %% "zio"                     % "2.0.0",
//  "dev.zio"                     %% "zio-streams"             % "2.0.0",
//  "dev.zio"                     %% "zio-json"                % "0.3.0-RC8",
//  "io.d11"                      %% "zhttp"                   % "2.0.0-RC11",
  "com.softwaremill.sttp.tapir" %% "tapir-zio"               % tapirVersion, // Brings in zio, zio-streams
  "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"   % tapirVersion, // Brings in zhttp
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"       % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-zio"          % tapirVersion, // brings in zio-json
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "dev.zio"                     %% "zio-logging"             % zioLoggingVersion,
  "dev.zio"                     %% "zio-logging-slf4j"       % zioLoggingVersion,
  "ch.qos.logback"               % "logback-classic"         % "1.4.1",
  "dev.zio"                     %% "zio-test"                % zioVersion,
  "dev.zio"                     %% "zio-test-sbt"            % zioVersion % "it, test",
  "dev.zio"                     %% "zio-test-magnolia"       % zioVersion % "it, test",
  "dev.zio"                     %% "zio-mock"                % "1.0.0-RC8" % "it, test"
)

lazy val root = (project in file("."))
  .settings(
    name := "zio-project-playground",
    libraryDependencies ++= libraries
  )

addCommandAlias("fmt", "all root/scalafmtSbt root/scalafmtAll")
addCommandAlias("fmtCheck", "all root/scalafmtSbtCheck root/scalafmtCheckAll")
addCommandAlias(
  "fix",
  "root/scalafixAll RemoveUnused"
)
