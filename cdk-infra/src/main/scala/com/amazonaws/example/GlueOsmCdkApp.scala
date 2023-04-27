package com.amazonaws.example

import software.amazon.awscdk.{App, Environment, StackProps}

object GlueOsmCdkApp {
  def main(args: Array[String]): Unit = {
    val app = new App()
    new GlueOsmCdkStack(app, "GlueOsmCdkStack", StackProps.builder().build())
    app.synth()
  }
}
