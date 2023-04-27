resolvers ++= Seq(
  "aws-glue-etl-artifacts" at "https://aws-glue-etl-artifacts.s3.amazonaws.com/release/",
  "osgeo" at "https://repo.osgeo.org/repository/release"
)

libraryDependencies ++= Seq(
  "com.amazonaws" % "AWSGlueETL" % "3.0.0" % "provided",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5" % "provided",
  "org.locationtech.geomesa" %% "geomesa-fs-spark-runtime" % "3.5.0" excludeAll ("*")
)