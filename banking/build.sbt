import Dependencies._

name := "banking"

organization := "com.navneetgupta"

version := "0.1.0"

scalaVersion := "2.12.6"

libraryDependencies ++= {
  val akkaVersion = "2.5.11"
  Seq(
  	"com.navneetgupta" %% "shared" % "0.1.1",
  	"com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
  )
}

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)