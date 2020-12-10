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
  val filePathResult           = ConfigFactory.load().getString("app.spark.filePathResult")
  val filePathResultCrimeTypes = ConfigFactory.load().getString("app.spark.filePathResultCrimeTypes")
  val filePathResultDistricts  = ConfigFactory.load().getString("app.spark.filePathResultDistricts")
  val filePathResultCrimesByDistrict  = ConfigFactory.load().getString("app.spark.filePathResultCrimesByDistrict")
  val filePathResultCrimesByCrimeType = ConfigFactory.load().getString("app.spark.filePathResultCrimesByCrimeType")
  // --

  val spark = SparkSession.builder
    .master("local[*]")
    .appName("Spark Data Parser")
    .getOrCreate()

  def getSparkAddress: String = spark.sparkContext.uiWebUrl.get

  val rawData = spark.read.parquet(filePathResult)
  val crimeTypes = rawData.select(CrimeType).dropDuplicates(CrimeType)
  val districts = rawData.select(DistrictName).dropDuplicates(DistrictName)

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

  def writeCrimeTypesToJSON(path: String = filePathResultCrimeTypes) = {
    // To write to a single partition as to have only one json file
    crimeTypes.coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeDistrictsToJSON(path: String = filePathResultDistricts) = {
    // To write to a single partition as to have only one json file
    districts.coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeCrimesByDistrictToJSON(path: String = filePathResultCrimesByDistrict) = {
    val dataByDistrictCrime = rawData.groupBy(rawData(DistrictName), rawData(CrimeType)).count()
    dataByDistrictCrime.orderBy(dataByDistrictCrime(DistrictName), dataByDistrictCrime("count").desc)
      .coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeCrimesByCrimeTypeToJSON(path: String = filePathResultCrimesByCrimeType) = {
    val dataByCrimeCount = rawData.groupBy(rawData(CrimeType)).count()
    dataByCrimeCount.orderBy(dataByCrimeCount("count").desc)
      .coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

}