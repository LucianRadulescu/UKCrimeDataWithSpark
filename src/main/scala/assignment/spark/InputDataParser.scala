package assignment.spark

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{callUDF, coalesce, input_file_name}
import org.apache.spark.sql.types.{StringType, StructType}

object InputDataParser {
  // -- constants
  private val CrimeId = "crimeId"
  private val Month = "month"
  private val ReportedBy = "reportedBy"
  private val FallsWithin = "fallsWithin"
  private val Longitude = "longitude"
  private val Latitude = "latitude"
  private val Location = "location"
  private val LSOACode = "LSOACode"
  private val LSOAName = "LSOAName"
  private val CrimeType = "crimeType"
  private val LastOutcome = "lastOutcome"
  private val Context = "context"
  private val DistrictName = "districtName"
  // --

  // -- result file path from configuration
  private lazy val configFilePathResult = ConfigFactory.load().getString("app.spark.filePathResult")
  private lazy val configFileNameResult = ConfigFactory.load().getString("app.spark.fileNameResult")
  // --

  // -- input file path matchers:
  //  "CSV_Resources/UK_CrimeData/*/*-street.csv""
  //  "CSV_Resources/UK_CrimeData/*/*-outcomes.csv"
  private lazy val filePathStreet = ConfigFactory.load().getString("app.spark.filePathInput") + "/*/*-street.csv"
  private lazy val filePathOutcomes = ConfigFactory.load().getString("app.spark.filePathInput") + "/*/*-outcomes.csv"
  // --

  private val schemaStreet = new StructType()
    .add(CrimeId, StringType, true)
    .add(Month, StringType, true)
    .add(ReportedBy, StringType, true)
    .add(FallsWithin, StringType, true)
    .add(Longitude, StringType, true)
    .add(Latitude, StringType, true)
    .add(Location, StringType, true)
    .add(LSOACode, StringType, true)
    .add(LSOAName, StringType, true)
    .add(CrimeType, StringType, true)
    .add(LastOutcome, StringType, true)
    .add(Context, StringType, true)

  private val schemaOutcomes = new StructType()
    .add(CrimeId, StringType, true)
    .add(Month, StringType, true)
    .add(ReportedBy, StringType, true)
    .add(FallsWithin, StringType, true)
    .add(Longitude, StringType, true)
    .add(Latitude, StringType, true)
    .add(Location, StringType, true)
    .add(LSOACode, StringType, true)
    .add(LSOAName, StringType, true)
    .add(LastOutcome, StringType, true)

  private lazy val spark = SparkSession.builder
    .master("local[*]")
    .appName("Input Parser")
    .getOrCreate()

  spark.udf.register("get_district", (path: String) => path.split("/").last.split("\\.").head.split("-").drop(2).dropRight(1).mkString(" "))

  def getSparkAddress: String = spark.sparkContext.uiWebUrl.get
}
class InputDataParser() {
  import assignment.spark.InputDataParser._

  /**
   * parse CSVs and write to result path
   *
   * result path default value is taken from application config - e.g. "Result/result.parquet"
   */
  def parseInputFiles(resultPath: String = (configFilePathResult + configFileNameResult)): Unit = {

    // load *-street.csv files
    val streetData = spark.read.format("csv")
      .option("header", "true")
      .schema(schemaStreet).load(filePathStreet)
      .withColumn(DistrictName, callUDF("get_district", input_file_name()))
    val streetDataNoNullKeys = streetData.filter(streetData(CrimeId).isNotNull)
    val streetDataOnlyNullKeys = streetData.filter(streetData(CrimeId).isNull)

    // load *-outcomes.csv
    val outcomesData = spark.read.format("csv")
      .option("header", "true")
      .schema(schemaOutcomes)
      .load(filePathOutcomes)
      .withColumn(DistrictName, callUDF("get_district", input_file_name()))

    /* Join the two data frames: streetData with no null keys, outcomesData
       on CrimeId and DistrictName, then union with the streetData that had null keys.

       Only the columns of interest are selected for the output file.
     */
    val result = streetDataNoNullKeys.join(outcomesData,
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

    result.write.parquet(resultPath)
  }
}