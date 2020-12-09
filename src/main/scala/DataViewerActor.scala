//#data-viewer-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment.spark.ProcessedDataParser
import com.typesafe.config.ConfigFactory

object DataViewerActor {

  // available user queries
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

  def getDataParser = ProcessedDataParser

  // actor protocol - these are the different messages that the actor knows how to handle
  sealed trait Command
  final case class RunCommand(command: String, district : Option[String], replyTo: ActorRef[String]) extends Command

  def apply(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case RunCommand(command, district, replyToActor) =>
        try {
          UserQuery.withName(command) match {
            case UserQuery.GetQueries => replyToActor ! ("Showing available queries below: \n\n" + getQueries)

            case UserQuery.GetCrimeTypes => replyToActor ! ("Received command and showing crimes...\n\n"
              + getDataParser.getCrimeTypes )

            case UserQuery.GetDistricts => replyToActor ! ("Received command and showing districts...\n\n"
              + getDataParser.getDistricts )

            case UserQuery.GetCrimesForDistrict => replyToActor ! ("Received command and showing crimes for district...\n\n"
              + getDataParser.getCrimesForDistrict(district) )
          }
        } catch {
          case e : NoSuchElementException => replyToActor ! (
            "Received unknown command \n" +
              command +
              "\nTry one of the available queries from below: \n\n" + getQueries)
          case other => throw other
        }
        Behaviors.same
    }
  }
}
//#data-viewer-actor