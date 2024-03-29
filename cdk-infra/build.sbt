enablePlugins(BuildInfoPlugin)

name := "cdk-infra"

libraryDependencies ++= Seq(
  "software.amazon.awscdk" % "aws-cdk-lib" % "2.75.1",
  "software.amazon.awscdk" % "glue-alpha" % "2.75.1-alpha.0",
  "software.constructs" % "constructs" % "10.1.280"
)

buildInfoKeys := Seq[BuildInfoKey](version)

buildInfoPackage := organization.value

Compile / run / mainClass := Some("com.amazonaws.example.GlueOsmCdkApp")