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

ThisBuild / fork       := true
Test / fork            := true
IntegrationTest / fork := true

ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

val zioVersion        = "2.0.2"
val tapirVersion      = "1.1.3"
val zioLoggingVersion = "2.1.2"
val zioConfigVersion  = "3.0.2"

lazy val libraries = Seq(
//  "dev.zio"                     %% "zio"                     % "2.0.0",
//  "dev.zio"                     %% "zio-streams"             % "2.0.0",
//  "dev.zio"                     %% "zio-json"                % "0.3.0-RC8",
//  "io.d11"                      %% "zhttp"                   % "2.0.0-RC11",
  "com.softwaremill.sttp.tapir" %% "tapir-zio"                         % tapirVersion, // Brings in zio, zio-streams
  "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"             % tapirVersion, // Brings in zhttp
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"                 % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-zio"                    % tapirVersion, // brings in zio-json
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"           % tapirVersion,
  "dev.zio"                     %% "zio-logging"                       % zioLoggingVersion,
  "dev.zio"                     %% "zio-logging-slf4j"                 % zioLoggingVersion,
  "ch.qos.logback"               % "logback-classic"                   % "1.4.3",
  "dev.zio"                     %% "zio-test"                          % zioVersion,
  "dev.zio"                     %% "zio-test-sbt"                      % zioVersion % "it, test",
  "dev.zio"                     %% "zio-test-magnolia"                 % zioVersion % "it, test",
  "dev.zio"                     %% "zio-mock"                          % "1.0.0-RC9" % "it, test",
  "dev.zio"                     %% "zio-config"                        % zioConfigVersion,
  "dev.zio"                     %% "zio-config-magnolia"               % zioConfigVersion,
  "dev.zio"                     %% "zio-config-typesafe"               % zioConfigVersion,
  "io.getquill"                 %% "quill-jdbc-zio"                    % "4.6.0",
  "org.postgresql"               % "postgresql"                        % "42.5.0",
  "org.flywaydb"                 % "flyway-core"                       % "9.4.0",
  "io.github.scottweaver"       %% "zio-2-0-testcontainers-postgresql" % "0.9.0",
  "dev.zio"                     %% "zio-prelude"                       % "1.0.0-RC16"
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "zio-project-playground",
    libraryDependencies ++= libraries,
    Defaults.itSettings
  )

addCommandAlias("fmt", "all root/scalafmtSbt root/scalafmtAll")
addCommandAlias("fmtCheck", "all root/scalafmtSbtCheck root/scalafmtCheckAll")
addCommandAlias(
  "fix",
  "root/scalafixAll RemoveUnused"
)
