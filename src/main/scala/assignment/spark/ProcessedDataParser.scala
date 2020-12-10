package assignment.spark
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{SaveMode, SparkSession}

object ProcessedDataParser {
  // -- constants
  private val CrimeId = "crimeId"
  private val Longitude = "longitude"
  private val Latitude = "latitude"
  private val CrimeType = "crimeType"
  private val LastOutcome = "lastOutcome"
  private val DistrictName = "districtName"
  // --

  // -- load paths from configuration
  private lazy val configFilePathResult: String = ConfigFactory.load().getString("app.spark.filePathResult")
  private lazy val configFileNameResult: String = ConfigFactory.load().getString("app.spark.fileNameResult")
  private lazy val configFilePathResultCrimeTypes: String = ConfigFactory.load().getString("app.spark.filePathResultCrimeTypes")
  private lazy val configFilePathResultDistricts: String = ConfigFactory.load().getString("app.spark.filePathResultDistricts")
  private lazy val configFilePathResultCrimesByDistrict: String = ConfigFactory.load().getString("app.spark.filePathResultCrimesByDistrict")
  private lazy val configFilePathResultCrimesByCrimeType: String = ConfigFactory.load().getString("app.spark.filePathResultCrimesByCrimeType")
  // --

  private lazy val spark = SparkSession.builder
    .master("local[*]")
    .appName("Spark Data Parser")
    .getOrCreate()

  private lazy val rawData = spark.read.parquet(configFilePathResult + configFileNameResult)
  private lazy val crimeTypes = rawData.select(CrimeType).dropDuplicates(CrimeType)
  private lazy val districts = rawData.select(DistrictName).dropDuplicates(DistrictName)

  def getSparkAddress: String = spark.sparkContext.uiWebUrl.get
}

class ProcessedDataParser() {
    import assignment.spark.ProcessedDataParser._

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

  def writeCrimeTypesToJSON(path: String = configFilePathResultCrimeTypes): Unit = {
    // To write to a single partition as to have only one json file
    crimeTypes.coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeDistrictsToJSON(path: String = configFilePathResultDistricts): Unit = {
    // To write to a single partition as to have only one json file
    districts.coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeCrimesByDistrictToJSON(path: String = configFilePathResultCrimesByDistrict): Unit = {
    val dataByDistrictCrime = rawData.groupBy(rawData(DistrictName), rawData(CrimeType)).count()
    dataByDistrictCrime.orderBy(dataByDistrictCrime(DistrictName), dataByDistrictCrime("count").desc)
      .coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

  def writeCrimesByCrimeTypeToJSON(path: String = configFilePathResultCrimesByCrimeType): Unit = {
    val dataByCrimeCount = rawData.groupBy(rawData(CrimeType)).count()
    dataByCrimeCount.orderBy(dataByCrimeCount("count").desc)
      .coalesce(1).write.mode(SaveMode.Overwrite).json(path)
  }

}