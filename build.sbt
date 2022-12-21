import com.typesafe.sbt.packager.docker._

name := "Task Manager"
organization := "ru.ingostrah"
scalaVersion := "2.13.4"

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

// Docker
lazy val ingostrahRegistry = "localhost:5000"
lazy val baseImageTag  = "openjdk:8u332-jre-slim"
lazy val baseImage     = if (sys.env.getOrElse("CI_SERVER", "no") == "yes") ingostrahRegistry + "/" + baseImageTag else baseImageTag

dockerBaseImage := baseImage
dockerRepository := Some(ingostrahRegistry + "/ingostrah")
dockerAlias := dockerAlias.value.copy(tag = Option("v" + version.value))
dockerExposedPorts ++= Seq(8080)
daemonUserUid in Docker := None
daemonUser in Docker := "daemon"

// change user for apt-get command
dockerCommands += Cmd("USER", "root")
dockerCommands ++= Seq(
  ExecCmd("RUN", "apt-get", "autoremove", "--purge", "openssl", "-y"),
  ExecCmd("RUN", "dpkg", "--purge", "openssl")
)

// change user back
dockerCommands += Cmd("USER", (daemonUser in Docker).value)

// Dependencies
lazy val akkaHttpVersion   = "10.2.6"
lazy val akkaVersion       = "2.6.15"
lazy val prometheusVersion = "0.11.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka"            %% "akka-http"                % akkaHttpVersion,
  "com.typesafe.akka"            %% "akka-actor-typed"         % akkaVersion,
  "com.typesafe.akka"            %% "akka-stream"              % akkaVersion,
  "joda-time"                     % "joda-time"               % "2.10.10",
  "ch.qos.logback"                % "logback-classic"          % "1.2.5",
  "org.json4s"                   %% "json4s-native"            % "4.0.2",
  "de.heikoseeberger"            %% "akka-http-json4s"         % "1.37.0",
  "ch.megard"                    %% "akka-http-cors"           % "1.1.2",
  "com.github.swagger-akka-http" %% "swagger-akka-http"        % "2.4.2",
  "javax.ws.rs"                   % "javax.ws.rs-api"          % "2.1.1",
  "io.prometheus"                 % "simpleclient"             % prometheusVersion,
  "io.prometheus"                 % "simpleclient_hotspot"     % prometheusVersion,
  "io.prometheus"                 % "simpleclient_httpserver"  % prometheusVersion,
  "io.prometheus"                 % "simpleclient_pushgateway" % prometheusVersion,
  "org.scalikejdbc"              %% "scalikejdbc"              % "3.5.0",
  "org.scalikejdbc"              %% "scalikejdbc-config"       % "3.5.0",
  "org.postgresql"               % "postgresql"                % "42.2.24",
  "org.scalikejdbc"              %% "scalikejdbc-joda-time"    % "3.5.0",
  "org.scala-lang.modules"       %% "scala-collection-compat"  % "2.5.0"
)
