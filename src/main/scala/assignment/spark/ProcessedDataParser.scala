package assignment.spark

import org.apache.spark.sql.{SaveMode, SparkSession}

class ProcessedDataParser(){
  val spark = SparkSession.builder
    .master("local[*]")
    .appName("Input Parser")
    .getOrCreate()

  def getSparkAddress: String = spark.sparkContext.uiWebUrl.get

  val filePathResult = "Result/result.parquet"
  val filePathResultCrimeTypes = "Result/crimeTypes.json"
  val filePathResultDistricts = "Result/districts.json"
  val filePathResultCrimesByDistrict = "Result/crimesByDistrict.json"
  val filePathResultCrimesByCrimeType = "Result/crimesByCrimeType.json"

  val rawData = spark.read.parquet(filePathResult)
  val crimeTypes = rawData.select("crimeType").dropDuplicates("crimeType")
  val districts = rawData.select("districtName").dropDuplicates("districtName")

  def writeCrimeTypesToJSON = {
    // To write to a single partition as to have only one json file
    crimeTypes.coalesce(1).write.mode(SaveMode.Overwrite).json(filePathResultCrimeTypes)
  }

  def getCrimeTypes: String = {
    crimeTypes.collect().mkString("\n")
  }

  def writeDistrictsToJSON = {
    // To write to a single partition as to have only one json file
    districts.coalesce(1).write.mode(SaveMode.Overwrite).json(filePathResultDistricts)
  }

  def getDistricts: String = {
    districts.collect().mkString("\n")
  }

  def getCrimesForDistrict(district : Option[String]): String = {
    val filteredRawData =
      if(district.isDefined) rawData.filter(rawData("districtName") === district.get) else rawData
    val dataByDistrictCrime = filteredRawData.groupBy(rawData("districtName"), rawData("crimeType")).count()
    dataByDistrictCrime.orderBy(dataByDistrictCrime("districtName"), dataByDistrictCrime("count").desc)
      .collect().mkString("\n")
  }

  def writeCrimesByDistrictToJSON = {
    val dataByDistrictCrime = rawData.groupBy(rawData("districtName"), rawData("crimeType")).count()
    dataByDistrictCrime.orderBy(dataByDistrictCrime("districtName"), dataByDistrictCrime("count").desc)
      .coalesce(1).write.mode(SaveMode.Overwrite).json(filePathResultCrimesByDistrict)
  }

  def writeCrimesByCrimeTypeToJSON = {
    val dataByCrimeCount = rawData.groupBy(rawData("crimeType")).count()
    dataByCrimeCount.orderBy(dataByCrimeCount("count").desc)
      .coalesce(1).write.mode(SaveMode.Overwrite).json(filePathResultCrimesByCrimeType)
  }

}