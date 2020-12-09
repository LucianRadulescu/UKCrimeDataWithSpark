//#data-viewer-actor
import scala.util.{Success => ScalaSuccess, Failure => ScalaFailure}
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import assignment.spark.{DataParserActor, ProcessedDataParser}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.DurationInt

object DataViewerActor {

  // -- enumeration holding the queries that the user can run
  object UserQuery extends Enumeration {
    type UserQuery = Value
    val GetQueries,
    GetCrimeTypes,
    GetDistricts,
    GetCrimesForDistrict = Value
  }

  def getQueries : String = {
    val serverInterface = ConfigFactory.load().getString("app.localServer.interface")
    val serverPort = ConfigFactory.load().getInt("app.localServer.port")
    val localAddress = "http://" + serverInterface + ":" + serverPort + "/"
    localAddress + "view/" + UserQuery.values.mkString("\n" + localAddress + "view/")
  }

  // actor protocol - these are the different messages that the actor knows how to handle
  sealed trait Command

  final case class RunCommand(command: String, district : Option[String], replyTo: ActorRef[String]) extends Command
  private case class AdaptedResponse(message: String, replyTp: ActorRef[String]) extends Command

  def apply(parser : ActorRef[DataParserActor.Command]): Behavior[Command] = {
    Behaviors.receive {
      case (context, RunCommand(command, district, replyToActor)) =>
        try {
          UserQuery.withName(command) match {
            case UserQuery.GetQueries => replyToActor ! ("Showing available queries below: \n\n" + getQueries)

            case UserQuery.GetCrimeTypes =>
              implicit val timeout: Timeout = Timeout(15.seconds)
              val result = context.ask(parser, DataParserActor.Parse(command, _)) {
                case ScalaSuccess(DataParserActor.Response(message)) => AdaptedResponse(message, replyToActor)
                case ScalaFailure(_) => AdaptedResponse("FAILED", replyToActor)
              }
//              replyToActor ! ("Received command and showing crimes...\n\n"
//              + ProcessedDataParser.getCrimeTypes )

//            case UserQuery.GetDistricts => replyToActor ! ("Received command and showing districts...\n\n"
//              + ProcessedDataParser.getDistricts )
//
//            case UserQuery.GetCrimesForDistrict => replyToActor ! ("Received command and showing crimes for district...\n\n"
//              + ProcessedDataParser.getCrimesForDistrict(district) )
          }
        } catch {
          case e : NoSuchElementException => replyToActor ! (
            "Received unknown command \n" +
              command +
              "\nTry one of the available queries from below: \n\n" + getQueries)
          case other => throw other
        }
        Behaviors.same

      case (context, AdaptedResponse(message, replyToActor)) =>
        replyToActor ! message
        Behaviors.same
    }
  }
}
//#data-viewer-actor