import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.testkit.ScalatestRouteTest
import assignment.spark.ProcessedDataParser
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{doNothing, times, verify, when}

//#set-up
class DataWriterActorTest extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with MockitoSugar {
  //#test-top

  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val mockParser = mock[ProcessedDataParser]
  val dataWriterActor = testKit.spawn(new DataWriterActor(mockParser)())

  // -- routing tests
  "DataViewerActor" should {

    "reply with list of available queries if the command is not recognized" in {
      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("SomeCommand", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.unknownCommandWriterText)
    }

    "call the correct parser method for WriteCrimeTypes command" in {
      // default mockito behavior will be to do nothing for void methods
      // doNothing().when(mockParser).writeCrimeTypesToJSON

      when(mockParser.getSparkAddress) thenReturn TestConstants.sparkAddress

      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("WriteCrimeTypes", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.writerResponseWriteCrimeTypes)


      verify(mockParser, times(1)).writeCrimeTypesToJSON
    }

    "call the correct parser method for WriteDistricts command" in {
      // default mockito behavior will be to do nothing for void methods
      // doNothing().when(mockParser).writeDistrictsToJSON

      when(mockParser.getSparkAddress) thenReturn TestConstants.sparkAddress

      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("WriteDistricts", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.writerResponseWriteDistricts)


      verify(mockParser, times(1)).writeDistrictsToJSON
    }

    "call the correct parser method for WriteCrimesByDistrict command" in {
      // default mockito behavior will be to do nothing for void methods
      // doNothing().when(mockParser).writeCrimesByDistrictToJSON

      when(mockParser.getSparkAddress) thenReturn TestConstants.sparkAddress

      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("WriteCrimesByDistrict", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.writerResponseWriteCrimesByDistrict)


      verify(mockParser, times(1)).writeCrimesByDistrictToJSON
    }

    "call the correct parser method for WriteCrimesByCrimeType command" in {
      // default mockito behavior will be to do nothing for void methods
      // doNothing().when(mockParser).writeCrimesByCrimeTypeToJSON

      when(mockParser.getSparkAddress) thenReturn TestConstants.sparkAddress

      val replyProbe = testKit.createTestProbe[String]()
      dataWriterActor ! DataWriterActor.RunCommand("WriteCrimesByCrimeType", replyProbe.ref)
      replyProbe.expectMessage(TestConstants.writerResponseWriteCrimesByCrimeType)


      verify(mockParser, times(1)).writeCrimesByCrimeTypeToJSON
    }
  }
}
