import DataViewerActor.RunCommand
import akka.actor.Props
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

//#set-up
class AppRoutesTest extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  //#test-top

  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  // Mock the DataViewerActor
  // We don't care for the reply, we're only testing the routes
  // However, see that the Behaviour is of the DataViewerActor.Command type
  object MockViewerActor {

    def apply(): Behavior[DataViewerActor.Command] = {
      Behaviors.receiveMessage {
        case DataViewerActor.RunCommand(command: String, district : Option[String], replyToActor: ActorRef[String]) =>
          replyToActor ! TestConstants.MockReplyViewer
          Behaviors.same
      }
    }
  }
  // Mock the DataWriterActor
  object MockWriterActor {

    def apply(): Behavior[DataWriterActor.Command] = {
      Behaviors.receiveMessage {
        case DataWriterActor.RunCommand(command: String, replyToActor: ActorRef[String]) =>
          replyToActor ! TestConstants.MockReplyWriter
          Behaviors.same
      }
    }
  }

  // bind the mock actors
  val mockDataViewerActor = testKit.spawn(MockViewerActor())
  val mockDataWriterActor = testKit.spawn(MockWriterActor())

  lazy val routes = new AppRoutes(mockDataViewerActor, mockDataWriterActor).topLevelRoute

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  //#set-up

  // -- routing tests
  "AppRoutes" should {

    "print the hello page for http://localhost:8080" in {
      val request = HttpRequest()

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        entityAs[String] should ===(TestConstants.HomeText)
      }
    }

    // -- view routes
    "correctly route http://localhost:8080/view to the mock Viewer" in {
      val request = HttpRequest(uri = "/view")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        entityAs[String] should ===(TestConstants.MockReplyViewer)
      }
    }

    "correctly route command http://localhost:8080/view/Command to the mock Viewer" in {
      val request = HttpRequest(uri = "/view/Command")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        entityAs[String] should ===(TestConstants.MockReplyViewer)
      }
    }

    "correctly route command with parameter http://localhost:8080/view/Command?param=something to the mock Viewer" in {
      val request = HttpRequest(uri = "/view/Command?param=something")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        entityAs[String] should ===(TestConstants.MockReplyViewer)
      }
    }

    // -- write routes
    // -- view routes
    "correctly route command http://localhost:8080/write to the mock Write" in {
      val request = HttpRequest(uri = "/write")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        entityAs[String] should ===(TestConstants.MockReplyWriter)
      }
    }

    "correctly route command http://localhost:8080/view/Command to the mock Writer" in {
      val request = HttpRequest(uri = "/write/Command")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        entityAs[String] should ===(TestConstants.MockReplyWriter)
      }
    }
  }

  //#set-up
}
