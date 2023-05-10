package com.amazonaws.example

import software.amazon.awscdk.services.ec2._
import software.amazon.awscdk.services.iam.{IManagedPolicy, ManagedPolicy, Role, ServicePrincipal}
import software.amazon.awscdk.services.s3.Bucket
import software.constructs.Construct

import scala.collection.JavaConverters._
import scala.io.Source

class GeoServerEc2Construct(scope: Construct, id: String, bucket: Bucket) extends Construct(scope, id) {

  val vpc: Vpc =
    Vpc.Builder
      .create(this, s"$id-vpc")
      .vpcName(s"$id-vpc")
      .build()

  val securityGroup: ISecurityGroup =
    SecurityGroup.Builder
      .create(this, s"$id-sg")
      .securityGroupName(s"$id-sg")
      .vpc(vpc)
      .build()

  securityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(8080))

  val policy: IManagedPolicy =
    ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMManagedInstanceCore")

  val role: Role =
    Role.Builder
      .create(this, s"$id-role")
      .roleName(s"$id-role")
      .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
      .managedPolicies(List(policy).asJava)
      .build()

  bucket.grantRead(role)

  val image: IMachineImage = AmazonLinuxImage.Builder.create().generation(AmazonLinuxGeneration.AMAZON_LINUX_2).build()
  val instanceType: InstanceType = InstanceType.of(InstanceClass.T2, InstanceSize.XLARGE)
  val subnet: SubnetSelection = SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build()

  val instance: Instance =
    Instance.Builder
      .create(this, s"$id-instance")
      .machineImage(image)
      .securityGroup(securityGroup)
      .instanceType(instanceType)
      .role(role)
      .vpcSubnets(subnet)
      .vpc(vpc)
      .build()

  instance.addUserData(Source.fromResource("install-geomesa-geoserver.sh").mkString)

}
