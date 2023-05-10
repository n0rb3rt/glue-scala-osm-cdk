package com.amazonaws.example

import org.apache.spark.sql.{DataFrame, SaveMode}
import org.geotools.data.DataStoreFinder
import org.locationtech.geomesa.spark.sql.SparkUtils

import scala.collection.JavaConverters._

object Functions extends Serializable {

  def writeGeomesaFeature(
     name: String,
     df: DataFrame,
     dsParams: Map[String, String],
     scheme: String
   ): Unit = {

    val sft = SparkUtils.createFeatureType(name, df.schema)
    sft.getUserData.put("geomesa.fs.scheme", s"""{"name": "${scheme}"}""")
    sft.getUserData.put("geomesa.ignore.dtg", "true") //don't create temporal index
    sft.getUserData.put("geomesa.fs.leaf-storage", "false") //create partition folders

    val ds = DataStoreFinder.getDataStore(dsParams.asJava)
    ds.createSchema(sft)

    df.write
      .mode(SaveMode.Overwrite)
      .format("geomesa")
      .options(dsParams)
      .option("geomesa.feature", name)
      .save()
  }
}
