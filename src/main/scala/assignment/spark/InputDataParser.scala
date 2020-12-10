package assignment.spark

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{array, broadcast, callUDF, coalesce, col, countDistinct, explode, input_file_name, lit, rand}
import org.apache.spark.sql.types.{IntegerType, StringType, StructType}

object InputDataParser{

  // -- constants
  val CrimeId      = "crimeId"
  val Month        = "month"
  val ReportedBy   = "reportedBy"
  val FallsWithin  = "fallsWithin"
  val Longitude    = "longitude"
  val Latitude     = "latitude"
  val Location     = "location"
  val LSOACode     = "LSOACode"
  val LSOAName     = "LSOAName"
  val CrimeType    = "crimeType"
  val LastOutcome  = "lastOutcome"
  val Context      = "context"
  val DistrictName = "districtName"
  // --

  val spark = SparkSession.builder
    .master("local[*]")
    .appName("Input Parser")
    .getOrCreate()

  val filePathResult = "Result/"

  // -- input file path matchers:
  // "CSV_Resources/UK_CrimeData/*/*-street.csv""
  // "CSV_Resources/UK_CrimeData/*/*-outcomes.csv"

  val filePathStreet = ConfigFactory.load().getString("app.spark.filePathInput") + "/*/*-street.csv"
  val filePathOutcomes = ConfigFactory.load().getString("app.spark.filePathInput") + "/*/*-outcomes.csv"

  val schemaStreet = new StructType()
    .add(CrimeId,StringType,true)
    .add(Month,StringType,true)
    .add(ReportedBy,StringType,true)
    .add(FallsWithin,StringType,true)
    .add(Longitude,StringType,true)
    .add(Latitude,StringType,true)
    .add(Location,StringType,true)
    .add(LSOACode,StringType,true)
    .add(LSOAName,StringType,true)
    .add(CrimeType,StringType,true)
    .add(LastOutcome,StringType,true)
    .add(Context,StringType,true)

  val schemaOutcomes = new StructType()
    .add(CrimeId,StringType,true)
    .add(Month,StringType,true)
    .add(ReportedBy,StringType,true)
    .add(FallsWithin,StringType,true)
    .add(Longitude,StringType,true)
    .add(Latitude,StringType,true)
    .add(Location,StringType,true)
    .add(LSOACode,StringType,true)
    .add(LSOAName,StringType,true)
    .add(LastOutcome,StringType,true)

  spark.udf.register("get_district", (path: String) => path.split("/").last.split("\\.").head.split("-").drop(2).dropRight(1).mkString(" "))

  def parseInputFiles = {

    val streetData = spark.read.format("csv")
      .option("header", "true")
      .schema(schemaStreet).load(filePathStreet)
      .withColumn(DistrictName, callUDF("get_district", input_file_name()))
    val streetDataNoNullKeys = streetData.filter(streetData(CrimeId).isNotNull)
    val streetDataOnlyNullKeys = streetData.filter(streetData(CrimeId).isNull)

    val outcomesData = spark.read.format("csv")
      .option("header", "true")
      .schema(schemaOutcomes)
      .load(filePathOutcomes)
      .withColumn(DistrictName, callUDF("get_district", input_file_name()))

    val result = streetDataNoNullKeys.join(outcomesData,
      //streetDataNoNullKeys("crimeId") === outcomesData("crimeId"), //~ 1.7 min
      Seq(CrimeId, DistrictName),
      joinType = "leftouter")
      .select(streetDataNoNullKeys(CrimeId),
        streetDataNoNullKeys(DistrictName),
        streetDataNoNullKeys(Latitude),
        streetDataNoNullKeys(Longitude),
        streetDataNoNullKeys(CrimeType),
        coalesce(outcomesData(LastOutcome), streetData(LastOutcome)).as(LastOutcome))
      .union(streetDataOnlyNullKeys.select(streetDataOnlyNullKeys(CrimeId),
        streetDataOnlyNullKeys(DistrictName),
        streetDataOnlyNullKeys(Latitude),
        streetDataOnlyNullKeys(Longitude),
        streetDataOnlyNullKeys(CrimeType),
        streetDataOnlyNullKeys(LastOutcome)))

    val timesStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
    result.write.parquet(filePathResult + timesStamp + "_result.parquet")
  }
}