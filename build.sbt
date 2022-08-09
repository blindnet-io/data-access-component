ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.1.3"

Test / fork := true

val circeVersion = "0.14.2"
val http4sVersion = "0.23.12"
val tapirVersion = "1.0.3"

lazy val root = (project in file("."))
  .settings(
    name := "data-access-component",
    organization := "io.blindnet",
    organizationName := "blindnet",
    organizationHomepage := Some(url("https://blindnet.io")),
    idePackagePrefix := Some("io.blindnet.dataaccess"),
    libraryDependencies ++= Seq(
      "com.azure"                   %  "azure-storage-blob"              % "12.18.0",
      "com.softwaremill.sttp.tapir" %% "tapir-core"                      % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"             % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"                % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"         % tapirVersion,
      "dev.profunktor"              %% "redis4cats-effects"              % "1.2.0",
      "io.circe"                    %% "circe-core"                      % circeVersion,
      "io.circe"                    %% "circe-generic"                   % circeVersion,
      "io.circe"                    %% "circe-literal"                   % circeVersion % Test,
      "org.http4s"                  %% "http4s-blaze-server"             % http4sVersion,
      "org.http4s"                  %% "http4s-circe"                    % http4sVersion,
      "org.scalatest"               %% "scalatest"                       % "3.2.12" % Test,
      "org.slf4j"                   %  "slf4j-simple"                    % "1.7.36",
      "org.typelevel"               %% "cats-effect"                     % "3.3.14",
      "org.typelevel"               %% "cats-effect-testing-scalatest"   % "1.4.0" % Test,
      "org.typelevel"               %% "log4cats-slf4j"                  % "2.4.0",
    ),
    assembly / mainClass := Some("io.blindnet.dataaccess.Main"),
    assembly / assemblyJarName := "data_access.jar",
    assembly / assemblyMergeStrategy := {
      case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.discard
      case PathList("META-INF", "maven", "org.webjars", "swagger-ui", "pom.properties") => MergeStrategy.singleOrError
      case x => assemblyMergeStrategy.value(x)
    },
    assembly / packageOptions += Package.ManifestAttributes(
      "Multi-Release" -> "true"
    )
  )
