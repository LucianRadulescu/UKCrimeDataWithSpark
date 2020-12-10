import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment.spark.ProcessedDataParser
import com.typesafe.config.ConfigFactory

object DataViewerActor {

  // enumeration holding the queries that the user can run
  object UserQuery extends Enumeration {
    type UserQuery = Value
    val GetQueries,
    GetCrimeTypes,
    GetDistricts,
    GetCrimesForDistrict = Value
  }

  def getViewQueries: String = {
    val serverInterface = ConfigFactory.load().getString("app.localServer.interface")
    val serverPort = ConfigFactory.load().getInt("app.localServer.port")
    val localAddress = "http://" + serverInterface + ":" + serverPort + "/"

    localAddress + "view/" + UserQuery.values.mkString("\n" + localAddress + "view/")
  }

  // actor protocol - these are the different messages that the actor knows how to handle
  sealed trait Command
  final case class RunCommand(command: String, district: Option[String], replyTo: ActorRef[String]) extends Command

}
class DataViewerActor(processedDataParser : ProcessedDataParser = new ProcessedDataParser()) {
  import DataViewerActor.UserQuery._

  def apply(): Behavior[DataViewerActor.Command] = {
    Behaviors.receive {
      case (context, DataViewerActor.RunCommand(command, district, replyToActor)) =>
        try {
          DataViewerActor.UserQuery.withName(command) match {
            case GetQueries => replyToActor ! ("Showing available queries below:\n\n" +
              DataViewerActor.getViewQueries)

            case GetCrimeTypes => replyToActor ! ("Received command and showing crime types...\n\n" +
              processedDataParser.getCrimeTypes )

            case GetDistricts => replyToActor ! ("Received command and showing districts...\n\n" +
              processedDataParser.getDistricts )

            case GetCrimesForDistrict => replyToActor ! ("Received command and showing crimes for district...\n\n" +
              processedDataParser.getCrimesForDistrict(district) )
          }
        } catch {
          case e : NoSuchElementException => replyToActor ! (
            "Received unknown command \n" +
              command +
              "\nTry one of the available queries from below: \n\n" +
              DataViewerActor.getViewQueries)
          case other: Throwable => replyToActor ! (
            "Encountered exception\n" +
              other.getMessage())
        }
        Behaviors.same
    }
  }
}