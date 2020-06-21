name := "akka-typed-persistence-sharding"

version := "0.1"

scalaVersion := "2.12.0"

val dependencies = Seq (
  "com.typesafe.akka" %% "akka-persistence-typed"      % "2.6.5",
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % "2.6.5",
  "com.typesafe.akka" %% "akka-serialization-jackson"  % "2.6.5",
  "com.typesafe.akka" %% "akka-actor-typed"            % "2.6.5",
  "com.typesafe.play" %% "play-json"                   % "2.9.0",
  "com.typesafe.akka" %% "akka-slf4j"                  % "2.6.5",
  "ch.qos.logback"    % "logback-classic"              % "1.2.3",
  "com.typesafe.akka" %% "akka-persistence-cassandra"  % "1.0.0",
  "com.typesafe.akka" %% "akka-persistence-query"      % "2.6.5"
)

lazy val banking = (project in file("banking"))
  .settings(Seq(libraryDependencies ++= dependencies))


lazy val `akka-typed-persistence-sharding` = (project in file("."))
  .aggregate(
   banking
  )
