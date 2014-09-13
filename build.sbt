import com.typesafe.sbt.web.SbtWeb
import play.PlayScala

name := """AyaIB"""

version := "0.1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.1"

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  jdbc,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT"
)

lazy val compileThemes = taskKey[Unit]("Compiles LESS entry points of all themes")

compileThemes := {
  val ec = (baseDirectory.value / "scripts/compile-themes.sh").getAbsolutePath !
  val log = streams.value.log
  log.debug(s"Exit code: $ec")
}

compileThemes <<= compileThemes triggeredBy (compile in Compile)
