name := "http4s-backend"

version := "0.1"

scalaVersion := "2.12.6"

val http4sVersion = "0.18.11"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.9.3",
  "io.circe" %% "circe-literal" % "0.9.3"
)

scalacOptions ++= Seq("-Ypartial-unification")