import com.typesafe.sbt.web.SbtWeb
import play.PlayScala

name := """AyaIB"""

version := "0.1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  jdbc,
  "org.reactivemongo"      %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT",
  "com.twitter"            %% "util-core"           % "6.20.0",
  "com.sksamuel.scrimage"  %% "scrimage-core"       % "1.4.1",
  "com.github.nscala-time" %% "nscala-time"         % "1.4.0",
  "jp.t2v"                 %% "play2-auth"          % "0.12.0",
  "com.github.t3hnar"      %% "scala-bcrypt"        % "2.4"
)

lazy val compileThemes = taskKey[Unit]("Compiles LESS entry points of all themes")

compileThemes := {
  val ec = (baseDirectory.value / "scripts/compile-themes.sh").getAbsolutePath !
  val log = streams.value.log
  log.debug(s"Exit code: $ec")
}

compileThemes <<= compileThemes triggeredBy (compile in Compile)
