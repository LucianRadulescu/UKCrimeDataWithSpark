import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.testkit.ScalatestRouteTest
import assignment.spark.ProcessedDataParser
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito.{reset, times, verify, when}


class DataViewerActorTest extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with MockitoSugar {

  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem


  // todo move mock and dataViewerActor to each test to make them independent
  val mockParser = mock[ProcessedDataParser]
  val dataViewerActor = testKit.spawn(new DataViewerActor(mockParser)())

  // -- routing tests
  "DataViewerActor" should {

    "reply with list of available queries if the command is not recognized" in {
      val replyProbe = testKit.createTestProbe[String]()
      dataViewerActor ! DataViewerActor.RunCommand("SomeCommand", None, replyProbe.ref)
      replyProbe.expectMessage(TestConstants.UnknownCommandViewerText)
    }

    "reply with list of available queries for GetQueries command" in {
      val replyProbe = testKit.createTestProbe[String]()
      dataViewerActor ! DataViewerActor.RunCommand("GetQueries", None, replyProbe.ref)
      replyProbe.expectMessage(TestConstants.ShowQueriesViewerText)
    }

    "call the correct parser method for GetCrimeTypes command" in {
      when(mockParser.getCrimeTypes) thenReturn TestConstants.MockCrimeTypes

      val replyProbe = testKit.createTestProbe[String]()
      dataViewerActor ! DataViewerActor.RunCommand("GetCrimeTypes", None, replyProbe.ref)
      replyProbe.expectMessage(TestConstants.ViewerResponseGetCrimeTypes)

      verify(mockParser, times(1)).getCrimeTypes
    }

    "call the correct parser method for GetDistricts command" in {
      when(mockParser.getDistricts) thenReturn TestConstants.MockDistricts

      val replyProbe = testKit.createTestProbe[String]()
      dataViewerActor ! DataViewerActor.RunCommand("GetDistricts", None, replyProbe.ref)
      replyProbe.expectMessage(TestConstants.ViewerResponseGetDistricts)

      verify(mockParser, times(1)).getDistricts
    }

    "call the correct parser method for GetCrimesForDistrict command" +
      " with parameter ?district=northumbria" in {
      val district = Some("northumbria")
      when(mockParser.getCrimesForDistrict(district)) thenReturn TestConstants.MockCrimesForDistrict

      val replyProbe = testKit.createTestProbe[String]()
      dataViewerActor ! DataViewerActor.RunCommand("GetCrimesForDistrict", district, replyProbe.ref)
      replyProbe.expectMessage(TestConstants.ViewerResponseGetCrimesForDistrict)

      verify(mockParser, times(1)).getCrimesForDistrict(_)
    }

    "handle exceptions thrown by the parser" in {
      reset(mockParser)
      val e: Throwable = new RuntimeException("Test exception")
      when(mockParser.getDistricts) thenThrow e

      val replyProbe = testKit.createTestProbe[String]()
      dataViewerActor ! DataViewerActor.RunCommand("GetDistricts", None, replyProbe.ref)
      replyProbe.expectMessage("Encountered exception\n" + e.getMessage)

      verify(mockParser, times(1)).getDistricts
    }
  }
}
