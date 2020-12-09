import akka.actor.Props
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{complete, onSuccess}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import assignment.spark.DataParserActor
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

//#set-up
class DataViewerActorTest extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  //#test-top

  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  val dataParserActor = testKit.spawn(new DataParserActor()())
  val dataViewerActor = testKit.spawn(DataViewerActor(dataParserActor))
  private implicit val timeout = Timeout(15.seconds)
  // -- routing tests
  "DataViewerActor" should {

    "reply with list of available queries if the command is not recognized" in {
      val replyProbe = testKit.createTestProbe[String]()
      dataViewerActor ! DataViewerActor.RunCommand("SomeCommand", Some("param"), replyProbe.ref)
      replyProbe.expectMessage(TestConstants.unknownCommandViewerText)
    }
  }
}
