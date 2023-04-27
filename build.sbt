val commonSettings = Seq(
  organization := "com.amazonaws.example",
  scalaVersion := "2.12.17",
  version := "1.0"
)

lazy val cdkInfra = (project in file("cdk-infra"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "cdk-infra",
    commonSettings,
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := organization.value
  )

lazy val glueOsm = (project in file("glue-osm"))
  .settings(
    name := "glue-osm",
    commonSettings
  )