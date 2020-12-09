//#data-viewer-actor
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment.spark.ProcessedDataParser
import com.typesafe.config.ConfigFactory

object DataViewerActor {

  // -- enumeration holding the queries that the user can run
  object UserQuery extends Enumeration {
    type UserQuery = Value
    val GetQueries,
    GetCrimeTypes,
    GetDistricts,
    GetCrimesForDistrict = Value
  }

  def getQueries: String = {
    val serverInterface = ConfigFactory.load().getString("app.localServer.interface")
    val serverPort = ConfigFactory.load().getInt("app.localServer.port")
    val localAddress = "http://" + serverInterface + ":" + serverPort + "/"
    localAddress + "view/" + UserQuery.values.mkString("\n" + localAddress + "view/")
  }

  // actor protocol - these are the different messages that the actor knows how to handle
  sealed trait Command

  final case class RunCommand(command: String, district: Option[String], replyTo: ActorRef[String]) extends Command

}
class DataViewerActor(dataParser : ProcessedDataParser) {

  def apply(): Behavior[DataViewerActor.Command] = {
    Behaviors.receive {
      case (context, DataViewerActor.RunCommand(command, district, replyToActor)) =>
        try {
          DataViewerActor.UserQuery.withName(command) match {
            case DataViewerActor.UserQuery.GetQueries => replyToActor ! ("Showing available queries below:\n\n" +
              DataViewerActor.getQueries)

            case DataViewerActor.UserQuery.GetCrimeTypes => replyToActor ! ("Received command and showing crime types...\n\n" +
              dataParser.getCrimeTypes )

            case DataViewerActor.UserQuery.GetDistricts => replyToActor ! ("Received command and showing districts...\n\n" +
              dataParser.getDistricts )

            case DataViewerActor.UserQuery.GetCrimesForDistrict => replyToActor ! ("Received command and showing crimes for district...\n\n" +
              dataParser.getCrimesForDistrict(district) )
          }
        } catch {
          case e : NoSuchElementException => replyToActor ! (
            "Received unknown command \n" +
              command +
              "\nTry one of the available queries from below: \n\n" +
              DataViewerActor.getQueries)
          case other => throw other
        }
        Behaviors.same
    }
  }
}
//#data-viewer-actor