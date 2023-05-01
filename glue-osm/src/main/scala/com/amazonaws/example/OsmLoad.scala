package com.amazonaws.example

import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.{GlueArgParser, Job}
import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.locationtech.geomesa.spark.jts._

import scala.collection.JavaConverters._

object OsmLoad {
  val log = LogManager.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("OsmLoad")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryo.registrator", "org.locationtech.geomesa.spark.GeoMesaSparkKryoRegistrator")
      .getOrCreate()
      .withJTS

    val glue = new GlueContext(spark.sparkContext)
    import spark.implicits._

    val glueArgs = GlueArgParser.getResolvedOptions(args, Array("JOB_NAME", "output_bucket"))
    Job.init(glueArgs("JOB_NAME"), glue, glueArgs.asJava)

    val datastoreParams: Map[String, String] = Map(
      "fs.path" -> s"s3a://${glueArgs("output_bucket")}/osm/",
      "fs.encoding" -> "parquet"
    )

    val planet = spark.read.orc("s3://osm-pds/planet/planet-latest.orc")

    val nodes =
      planet
        .where($"type" === "node" &&
          $"lon".between(-122.45, -122.25) &&
          $"lat".between(47.5, 47.7)
        )
        .withColumn("geom", st_makePoint($"lon", $"lat"))
        .selectExpr("id as node_id", "geom")
        .repartition($"node_id")

    val WayTypes = List("motorway_link", "trunk_link", "primary_link", "secondary_link", "tertiary_link")

    val ways =
      planet
        .where($"type" === "way")
        .filter($"tags.highway".isInCollection(WayTypes))
        .selectExpr("id as way_id", "tags as way_tags", "posexplode(nds.ref) as (idx, node_id)")
        .repartition($"node_id")

    val referencedWays =
      ways
        .join(nodes, Seq("node_id"))
        .groupBy("way_id")
        .agg(
          first("way_tags").as("way_tags"),
          st_makeLine(transform(
            array_sort(collect_list(struct("idx", "geom"))),
            (elem, _) => elem.getField("geom")
          )).as("geom")
        )

    Functions.writeGeomesaFeature("ReferencedWays", referencedWays, datastoreParams, "xz2-8bit")

    Job.commit()
  }
}