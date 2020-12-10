import assignment.spark.InputDataParser
import org.scalatest.{Matchers, WordSpec}
import java.io._
import scala.reflect.io.Directory
import com.typesafe.config.ConfigFactory


class InputDataParserTest extends WordSpec with Matchers {

  val dataParser = new InputDataParser()

  def getTempDir: File = {
    val dir = new File(ConfigFactory.load().getString("app.spark.filePathTestTemp")+"_InputDataParserTest", "")
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
  "InputDataParser" should {

    "return the correct spark address" in {

      // default spark address
      InputDataParser.getSparkAddress should ===("http://host.docker.internal:4040")

    }

    "process input csv and write to a parquet file" in withDir { (dir) =>
      val testFileName = dir.getAbsoluteFile + "/result_test.parquet"

      dataParser.parseInputFiles(testFileName)

      val created = new File(testFileName)
      assert(true, created.exists())
      assert(true, created.isFile)
    }

  }

}
