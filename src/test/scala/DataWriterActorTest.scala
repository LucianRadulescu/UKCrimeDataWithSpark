import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import assignment.spark.ProcessedDataParser
import org.apache.spark.sql.SparkSession
import org.mockito.ArgumentMatchers.{any, anyString}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{doThrow, times, verify, when}

import scala.concurrent.duration.DurationInt

//#set-up
class DataWriterActorTest extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with MockitoSugar {
  //#test-top

  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val mockParser = mock[ProcessedDataParser]
  val dataWriterActor = testKit.spawn(new DataWriterActor(mockParser)())


  private implicit val timeout = Timeout(15.seconds)

  // -- init spark, otherwise the akka actors might throw timeout while waiting for spark to start
  val spark = SparkSession.builder
    .master("local[*]")
    .appName("Input Parser")
    .getOrCreate()

  "DataWriterActor" should {

    "reply with list of available queries if the command is not recognized" in {
      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("SomeCommand", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.UnknownCommandWriterText)
    }

    "reply with list of available queries for GetQueries command" in {
      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("GetQueries", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.ShowQueriesWriterText)
    }

    "call the correct parser method for WriteCrimeTypes command" in {
      // default mockito behavior will be to do nothing for void methods
      // doNothing().when(mockParser).writeCrimeTypesToJSON

      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("WriteCrimeTypes", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.WriterResponseWriteCrimeTypes)

      verify(mockParser, times(1)).writeCrimeTypesToJSON(_)
    }

    "call the correct parser method for WriteDistricts command" in {
      // default mockito behavior will be to do nothing for void methods
      // doNothing().when(mockParser).writeDistrictsToJSON

      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("WriteDistricts", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.WriterResponseWriteDistricts)

      verify(mockParser, times(1)).writeDistrictsToJSON(_)
    }

    "call the correct parser method for WriteCrimesByDistrict command" in {
      // default mockito behavior will be to do nothing for void methods
      // doNothing().when(mockParser).writeCrimesByDistrictToJSON

      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("WriteCrimesByDistrict", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.WriterResponseWriteCrimesByDistrict)

      verify(mockParser, times(1)).writeCrimesByDistrictToJSON(_)
    }

    "call the correct parser method for WriteCrimesByCrimeType command" in {
      // default mockito behavior will be to do nothing for void methods
      // doNothing().when(mockParser).writeCrimesByCrimeTypeToJSON

      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("WriteCrimesByCrimeType", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.WriterResponseWriteCrimesByCrimeType)

      verify(mockParser, times(1)).writeCrimesByCrimeTypeToJSON(_)
    }

    // todo: check why mocker does not bind to the method so to throw exception
//    "handle exceptions thrown by the parser" in {
//
//      val e: Throwable = new RuntimeException("Test exception")
//      //when(mockParser.writeCrimeTypesToJSON(any())) thenThrow(e)
//      doThrow(e).when(mockParser).writeCrimeTypesToJSON();
//
//      val replyProbe = testKit.createTestProbe[String]()
//      dataWriterActor ! DataWriterActor.RunCommand("WriteCrimeTypes", replyProbe.ref)
//      replyProbe.expectMessage("Encountered exception\n" + e.getMessage)
//    }
  }
}
