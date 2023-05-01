ThisBuild / organization := "com.amazonaws.example"
ThisBuild / scalaVersion := "2.12.17"
ThisBuild / version := "1.0"

lazy val root = (project in file(".")).aggregate(cdkInfra, glueOsm)

lazy val cdkInfra = (project in file("cdk-infra"))

lazy val glueOsm = (project in file("glue-osm"))
