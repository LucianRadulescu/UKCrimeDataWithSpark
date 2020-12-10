import assignment.spark.ProcessedDataParser
import org.scalatest.{Matchers, WordSpec}
import java.io._
import scala.reflect.io.Directory
import com.typesafe.config.ConfigFactory


class ProcessedDataParserTest extends WordSpec with Matchers {

  val dataParser = new ProcessedDataParser()

  def getTempDir: File = {
    val dir = new File(ConfigFactory.load().getString("app.spark.filePathTestTemp")+"_ProcessedDataParserTest", "")
    dir.delete
    dir.mkdir
    dir
  }

  // loan pattern for setting up and cleaning tests
  // here we create a temp directory that will be deleted after the test
  def withDir(testCode: (File) => Any) {
    val tempDirectory: File = getTempDir
    val directory = new Directory(tempDirectory)
    try {
          testCode(tempDirectory) // "loan" the fixture to the test
    } finally directory.deleteRecursively()
  }


  // -- Spark integration tests
  "ProcessedDataParser" should {

    "retrieve the crime types" in {

      val crimeTypes: String = dataParser.getCrimeTypes
      crimeTypes should ===(TestConstants.CrimeTypes)

    }

    "retrieve the districts" in {

      val districts: String = dataParser.getDistricts
      districts should ===(TestConstants.Districts)

    }

    "retrieve the crimes for all districts if no district is provided" in {

      val allCrimes: String = dataParser.getCrimesForDistrict(None)
      allCrimes should ===(TestConstants.AllCrimes)

    }

    "retrieve the crimes for a specific district" in {

      val crimesInDistrict: String = dataParser.getCrimesForDistrict(Some("bedfordshire"))
      crimesInDistrict should ===(TestConstants.BedfordshireCrimes)

    }

    "write districts to a file" in withDir { (dir) =>
      val testFileName = dir.getAbsoluteFile + "/districts_temp.json"

      dataParser.writeDistrictsToJSON(testFileName)

      val created = new File(testFileName)
      assert(true, created.exists())
      assert(true, created.isFile)
    }

    "write crime types to a file" in withDir { (dir) =>
      val testFileName = dir.getAbsoluteFile + "/crimeTypes_temp.json"

      dataParser.writeCrimeTypesToJSON(testFileName)

      val created = new File(testFileName)
      assert(true, created.exists())
      assert(true, created.isFile)
    }

    "write crimes by crime type to a file" in withDir { (dir) =>
      val testFileName = dir.getAbsoluteFile + "/crimesByCrimeType_temp.json"

      dataParser.writeCrimesByCrimeTypeToJSON(testFileName)

      val created = new File(testFileName)
      assert(true, created.exists())
      assert(true, created.isFile)
    }

    "write crimes by district to a file" in withDir { (dir) =>
      val testFileName = dir.getAbsoluteFile + "/crimesByDistrict_temp.json"

      dataParser.writeCrimesByDistrictToJSON(testFileName)

      val created = new File(testFileName)
      assert(true, created.exists())
      assert(true, created.isFile)
    }

    "return the correct spark address" in {

      // default spark address
      ProcessedDataParser.getSparkAddress should ===("http://host.docker.internal:4040")

    }

    }

}
