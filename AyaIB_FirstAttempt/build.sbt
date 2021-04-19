play.Project.playScalaSettings

name := "ayaib"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.29",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0"
)

