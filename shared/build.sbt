name := "shared"

organization := "com.navneetgupta"

version := "0.1.1"

libraryDependencies ++= {
	val akkaVersion = "2.5.13"
	Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.3",
		"com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
		"com.typesafe.akka" %% "akka-persistence" % akkaVersion,
        "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.84",
  		"com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % "0.84" % Test,
        "com.google.protobuf" % "protobuf-java" % "3.5.1",
        "org.json4s" %% "json4s-ext" % "3.6.0-M3",
        "org.json4s" %% "json4s-native" % "3.6.0-M3",
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "com.typesafe.akka" %% "akka-http"   % "10.1.1",
        "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
		"com.typesafe.akka" %% "akka-http-testkit" % "10.1.1",
		"com.typesafe.akka" %% "akka-cluster" % akkaVersion,
		"com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
		"com.typesafe.akka" %% "akka-cluster-tools" % "2.5.13",
		"com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
	)
}

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)