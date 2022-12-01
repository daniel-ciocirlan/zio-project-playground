import sbt._

object Dependencies {

  val zioVersion        = "2.0.4"
  val tapirVersion      = "1.2.3"
  val zioLoggingVersion = "2.1.5"
  val zioConfigVersion  = "3.0.2"
  val sttpVersion       = "3.8.3"

  val server: Seq[ModuleID] = Seq(
//  "dev.zio"                     %% "zio"                     % "2.0.0",
//  "dev.zio"                     %% "zio-streams"             % "2.0.0",
//  "dev.zio"                     %% "zio-json"                % "0.3.0-RC8",
    "com.softwaremill.sttp.tapir" %% "tapir-zio"                         % tapirVersion, // Brings in zio, zio-streams
    "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"             % tapirVersion, // Brings in zhttp
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"           % tapirVersion,
    "dev.zio"                     %% "zio-logging"                       % zioLoggingVersion,
    "dev.zio"                     %% "zio-logging-slf4j"                 % zioLoggingVersion,
    "ch.qos.logback"               % "logback-classic"                   % "1.4.4",
    "dev.zio"                     %% "zio-test"                          % zioVersion,
    "dev.zio"                     %% "zio-test-sbt"                      % zioVersion % "it, test",
    "dev.zio"                     %% "zio-test-magnolia"                 % zioVersion % "it, test",
    "dev.zio"                     %% "zio-mock"                          % "1.0.0-RC9" % "it, test",
    "dev.zio"                     %% "zio-config"                        % zioConfigVersion,
    "dev.zio"                     %% "zio-config-magnolia"               % zioConfigVersion,
    "dev.zio"                     %% "zio-config-typesafe"               % zioConfigVersion,
    "io.getquill"                 %% "quill-jdbc-zio"                    % "4.6.0",
    "org.postgresql"               % "postgresql"                        % "42.5.0",
    "org.flywaydb"                 % "flyway-core"                       % "9.7.0",
    "io.github.scottweaver"       %% "zio-2-0-testcontainers-postgresql" % "0.9.0",
    "dev.zio"                     %% "zio-prelude"                       % "1.0.0-RC16",
    "com.auth0"                    % "java-jwt"                          % "4.2.1"
  )

}
