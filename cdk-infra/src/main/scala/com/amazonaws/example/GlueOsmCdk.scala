package com.amazonaws.example

import software.amazon.awscdk.{App, Environment, Stack, StackProps}
import software.constructs.Construct

object GlueOsmCdkApp {
  def main(args: Array[String]): Unit = {
    val app = new App()
    new GlueOsmCdkStack(app, "GlueOsmCdkStack")
    app.synth()
  }
}

class GlueOsmCdkStack(scope: Construct, id: String, props: StackProps) extends Stack(scope, id, props) {
  def this(scope: Construct, id: String) = this(scope, id, null)

  val glue = new GlueOsmJobConstruct(this, s"$id-glue")
  val ec2 = new GeoServerEc2Construct(this, s"$id-ec2", glue.outputBucket)
}
