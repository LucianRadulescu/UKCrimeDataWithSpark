package assignment.spark

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{array, broadcast, callUDF, coalesce, col, countDistinct, explode, input_file_name, lit, rand}
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}

object InputDataParser{
  val spark = SparkSession.builder
    .master("local[*]")
    .appName("Input Parser")
    .getOrCreate()

  val filePathResult = "Result/"

  val filePathStreet = "CSV_Resources/UK_CrimeData/*/*-street.csv"
  val filePathOutcomes = "CSV_Resources/UK_CrimeData/*/*-outcomes.csv"

  val schemaStreet = new StructType()
    .add("crimeId",StringType,true)
    .add("month",StringType,true)
    .add("reportedBy",StringType,true)
    .add("fallsWithin",StringType,true)
    .add("longitude",StringType,true)
    .add("latitude",StringType,true)
    .add("location",StringType,true)
    .add("LSOACode",StringType,true)
    .add("LSOAName",StringType,true)
    .add("crimeType",StringType,true)
    .add("lastOutcome",StringType,true)
    .add("context",StringType,true)

  val schemaOutcomes = new StructType()
    .add("crimeId",StringType,true)
    .add("month",StringType,true)
    .add("reportedBy",StringType,true)
    .add("fallsWithin",StringType,true)
    .add("longitude",StringType,true)
    .add("latitude",StringType,true)
    .add("location",StringType,true)
    .add("LSOACode",StringType,true)
    .add("LSOAName",StringType,true)
    .add("lastOutcome",StringType,true)

  spark.udf.register("get_district", (path: String) => path.split("/").last.split("\\.").head.split("-").drop(2).dropRight(1).mkString(" "))

  def parseInputFiles = {

    val streetData = spark.read.format("csv")
      .option("header", "true")
      .schema(schemaStreet).load(filePathStreet)
      .withColumn("districtName", callUDF("get_district", input_file_name()))
    val streetDataNoNullKeys = streetData.filter(streetData("crimeId").isNotNull)
    val streetDataOnlyNullKeys = streetData.filter(streetData("crimeId").isNull)

    val outcomesData = spark.read.format("csv")
      .option("header", "true")
      .schema(schemaOutcomes)
      .load(filePathOutcomes)
      .withColumn("districtName", callUDF("get_district", input_file_name()))

    val result = streetDataNoNullKeys.join(outcomesData,
      //streetDataNoNullKeys("crimeId") === outcomesData("crimeId"), //~ 1.7 min
      Seq("crimeId", "districtName"),
      joinType = "leftouter")
      .select(streetDataNoNullKeys("crimeId"),
        streetDataNoNullKeys("districtName"),
        streetDataNoNullKeys("latitude"),
        streetDataNoNullKeys("longitude"),
        streetDataNoNullKeys("crimeType"),
        coalesce(outcomesData("lastOutcome"), streetData("lastOutcome")).as("lastOutcome"))
      .union(streetDataOnlyNullKeys.select(streetDataOnlyNullKeys("crimeId"),
        streetDataOnlyNullKeys("districtName"),
        streetDataOnlyNullKeys("latitude"),
        streetDataOnlyNullKeys("longitude"),
        streetDataOnlyNullKeys("crimeType"),
        streetDataOnlyNullKeys("lastOutcome")))

    val timesStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
    result.write.parquet(filePathResult + timesStamp + "_result.parquet")
  }
}