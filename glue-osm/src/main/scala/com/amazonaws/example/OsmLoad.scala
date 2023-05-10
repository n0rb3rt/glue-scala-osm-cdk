package com.amazonaws.example

import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.{GlueArgParser, Job}
import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._
import org.locationtech.geomesa.spark.jts._

import scala.collection.JavaConverters._

object OsmLoad {
  val log = LogManager.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    val spark: SparkSession =
      SparkSession.builder()
        .appName("OsmLoad")
        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .config("spark.kryo.registrator", "org.locationtech.geomesa.spark.GeoMesaSparkKryoRegistrator")
        .getOrCreate()
        .withJTS

    val glue: GlueContext = new GlueContext(spark.sparkContext)

    import spark.implicits._

    val glueArgs: Map[String, String] =
      GlueArgParser.getResolvedOptions(args, Array("JOB_NAME", "output_bucket"))

    Job.init(glueArgs("JOB_NAME"), glue, glueArgs.asJava)

    val datastoreParams: Map[String, String] = Map(
      "fs.path" -> s"s3a://${glueArgs("output_bucket")}/osm/",
      "fs.encoding" -> "parquet"
    )

    val planet: DataFrame = spark.read.orc("s3://osm-pds/planet/planet-latest.orc")

    /**
     * root
     * |-- id: long (nullable = true)
     * |-- type: string (nullable = true)
     * |-- tags: map (nullable = true)
     * |    |-- key: string
     * |    |-- value: string (valueContainsNull = true)
     * |-- lat: decimal(9,7) (nullable = true)
     * |-- lon: decimal(10,7) (nullable = true)
     * |-- nds: array (nullable = true)
     * |    |-- element: struct (containsNull = true)
     * |    |    |-- ref: long (nullable = true)
     * |-- members: array (nullable = true)
     * |    |-- element: struct (containsNull = true)
     * |    |    |-- type: string (nullable = true)
     * |    |    |-- ref: long (nullable = true)
     * |    |    |-- role: string (nullable = true)
     * |-- changeset: long (nullable = true)
     * |-- timestamp: timestamp (nullable = true)
     * |-- uid: long (nullable = true)
     * |-- user: string (nullable = true)
     * |-- version: long (nullable = true)
     * |-- visible: boolean (nullable = true)
     */

    val nodes: Dataset[Row] =
      planet
        .where(
          $"type" === "node" &&
          $"lon".between(-122.45, -122.25) &&
          $"lat".between(47.5, 47.7)
        )
        .withColumn("geom", st_makePoint($"lon", $"lat"))
        .selectExpr("id as node_id", "geom")
        .repartition($"node_id")

    /**
     * root
     * |-- node_id: long (nullable = true)
     * |-- geom: point (nullable = true)
     */

    val ways: Dataset[Row] =
      planet
        .where($"type" === "way")
        .filter($"tags.highway".isNotNull)
        .selectExpr("id as way_id", "tags as way_tags", "posexplode(nds.ref) as (idx, node_id)")
        .repartition($"node_id")

    /**
     * root
     * |-- way_id: long (nullable = true)
     * |-- way_tags: map (nullable = true)
     * |    |-- key: string
     * |    |-- value: string (valueContainsNull = true)
     * |-- idx: integer (nullable = false)
     * |-- node_id: long (nullable = true)
     */

    val referencedWays: DataFrame =
      ways
        .join(nodes, Seq("node_id"))
        .groupBy("way_id")
        .agg(
          first("way_tags").as("way_tags"),
          transform(
            array_sort(collect_list(struct("idx", "geom"))),
            (elem, _) => elem.getField("geom")
          ).as("geoms")
        )
        .where(size($"geoms") >= 2)
        .select($"way_id", $"way_tags", st_makeLine($"geoms").as("geom"))

    /**
     * root
     * |-- way_id: long (nullable = true)
     * |-- way_tags: map (nullable = true)
     * |    |-- key: string
     * |    |-- value: string (valueContainsNull = true)
     * |-- geom: linestring (nullable = true)
     */

    Functions.writeGeomesaFeature("ReferencedWays", referencedWays, datastoreParams, "xz2-8bit")

    Job.commit()
  }
}