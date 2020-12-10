package assignment.spark

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{SaveMode, SparkSession}

class ProcessedDataParser(){

  // -- constants
  val CrimeId      = "crimeId"
  val Longitude    = "longitude"
  val Latitude     = "latitude"
  val CrimeType    = "crimeType"
  val LastOutcome  = "lastOutcome"
  val DistrictName = "districtName"
  // --

  // -- load paths from configuration
  lazy val filePathResult: String           = ConfigFactory.load().getString("app.spark.filePathResult")
  lazy val fileNameResult: String           = ConfigFactory.load().getString("app.spark.fileNameResult")
  lazy val filePathResultCrimeTypes: String = ConfigFactory.load().getString("app.spark.filePathResultCrimeTypes")
  lazy val filePathResultDistricts: String          = ConfigFactory.load().getString("app.spark.filePathResultDistricts")
  lazy val filePathResultCrimesByDistrict: String   = ConfigFactory.load().getString("app.spark.filePathResultCrimesByDistrict")
  lazy val filePathResultCrimesByCrimeType: String  = ConfigFactory.load().getString("app.spark.filePathResultCrimesByCrimeType")
  // --

  private lazy val spark = SparkSession.builder
    .master("local[*]")
    .appName("Spark Data Parser")
    .getOrCreate()

  private lazy val rawData    = spark.read.parquet(filePathResult + fileNameResult)
  private lazy val crimeTypes = rawData.select(CrimeType).dropDuplicates(CrimeType)
  private lazy val districts  = rawData.select(DistrictName).dropDuplicates(DistrictName)

  def getSparkAddress: String = spark.sparkContext.uiWebUrl.get

  def getCrimeTypes: String = {
    crimeTypes.collect().mkString("\n")
  }

  def getDistricts: String = {
    districts.collect().mkString("\n")
  }

  def getCrimesForDistrict(district : Option[String]): String = {
    val filteredRawData =
      if(district.isDefined) rawData.filter(rawData(DistrictName) === district.get) else rawData
    val dataByDistrictCrime = filteredRawData.groupBy(rawData(DistrictName), rawData(CrimeType)).count()
    dataByDistrictCrime.orderBy(dataByDistrictCrime(DistrictName), dataByDistrictCrime("count").desc)
      .collect().mkString("\n")
  }

  def writeCrimeTypesToJSON(path: String = filePathResultCrimeTypes): Unit = {
    // To write to a single partition as to have only one json file
    crimeTypes.coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeDistrictsToJSON(path: String = filePathResultDistricts): Unit = {
    // To write to a single partition as to have only one json file
    districts.coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeCrimesByDistrictToJSON(path: String = filePathResultCrimesByDistrict): Unit = {
    val dataByDistrictCrime = rawData.groupBy(rawData(DistrictName), rawData(CrimeType)).count()
    dataByDistrictCrime.orderBy(dataByDistrictCrime(DistrictName), dataByDistrictCrime("count").desc)
      .coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeCrimesByCrimeTypeToJSON(path: String = filePathResultCrimesByCrimeType): Unit = {
    val dataByCrimeCount = rawData.groupBy(rawData(CrimeType)).count()
    dataByCrimeCount.orderBy(dataByCrimeCount("count").desc)
      .coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

}