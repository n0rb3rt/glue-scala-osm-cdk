package com.amazonaws.example

import software.amazon.awscdk.services.glue.alpha._
import software.amazon.awscdk.services.iam.{IManagedPolicy, ManagedPolicy, Role, ServicePrincipal}
import software.amazon.awscdk.services.s3.{Bucket, IBucket}
import software.constructs.Construct

import scala.collection.JavaConverters._

class GlueOsmJobConstruct(scope: Construct, id: String) extends Construct(scope, id) {

  val policy: IManagedPolicy =
    ManagedPolicy.fromAwsManagedPolicyName("service-role/AWSGlueServiceRole")

  val role: Role =
    Role.Builder
      .create(this, "AWSGlueServiceRole-Osm")
      .assumedBy(new ServicePrincipal("glue.amazonaws.com"))
      .managedPolicies(List(policy).asJava)
      .build()

  val osmBucket: IBucket =
    Bucket.fromBucketName(this, "OsmBucket", "osm-pds")

  val outputBucket: Bucket =
    Bucket.Builder.create(this, "glue-osm-bucket").build()

  osmBucket.grantRead(role)
  outputBucket.grantReadWrite(role)

  val script: AssetCode =
    Code.fromAsset("./glue-osm/src/main/scala/com/amazonaws/example/Launch.scala")

  val assemblyJar: AssetCode =
    Code.fromAsset(s"./glue-osm/target/scala-2.12/glue-osm-assembly-${BuildInfo.version}.jar")

  val executable: JobExecutable =
    JobExecutable.scalaEtl(
      ScalaJobExecutableProps.builder()
        .glueVersion(GlueVersion.V4_0)
        .script(script)
        .extraJars(List(assemblyJar).asJava)
        .className("com.amazonaws.example.Launch")
        .build()
    )

  val args: Map[String, String] = Map(
    "--enable-continuous-cloudwatch-log" -> "true",
    "--enable-auto-scaling" -> "true",
    "--output_bucket" -> outputBucket.getBucketName
  )

  val job: Job =
    Job.Builder
      .create(this, "OsmLoad")
      .jobName("OsmLoad")
      .executable(executable)
      .defaultArguments(args.asJava)
      .role(role)
      .workerCount(10)
      .workerType(WorkerType.G_2_X)
      .build()
}
