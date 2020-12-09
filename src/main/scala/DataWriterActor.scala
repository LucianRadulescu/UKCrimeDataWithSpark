//#data-writer-actor
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment.spark.{InputDataParser, ProcessedDataParser}
import com.typesafe.config.ConfigFactory

object DataWriterActor {

  // available user queries
  object UserQuery extends Enumeration {
    type UserQuery = Value
    val ParseInitialFilesAndWriteParquetFile,
    WriteCrimeTypes,
    WriteDistricts,
    WriteCrimesByDistrict,
    WriteCrimesByCrimeType,
    GetQueries = Value
  }

  def getQueries : String = {
    val serverInterface = ConfigFactory.load().getString("app.localServer.interface")
    val serverPort = ConfigFactory.load().getInt("app.localServer.port")
    val localAddress = "http://" + serverInterface + ":" + serverPort + "/"
    localAddress + "write/" + UserQuery.values.mkString("\n" + localAddress + "write/")
  }
  // actor protocol - these are the different messages that the actor knows how to handle
  sealed trait Command
  final case class RunCommand(command: String, replyTo: ActorRef[String]) extends Command

  def apply(): Behavior[Command] = {
    Behaviors.receiveMessage {
      case RunCommand(command, replyToActor) =>
        try {
          UserQuery.withName(command) match {
            case UserQuery.GetQueries => replyToActor ! ("Showing available queries below: \n\n" + getQueries)

            case UserQuery.ParseInitialFilesAndWriteParquetFile => replyToActor ! "Started parsing of input files, check out the jobs at " + InputDataParser.spark.sparkContext.uiWebUrl.get
              InputDataParser.parseInputFiles

            case UserQuery.WriteCrimeTypes => replyToActor ! ("Writing crime types to JSON file...\n\n" +
              "Check started jobs at " +
              ProcessedDataParser.getSparkAddress)
              ProcessedDataParser.writeCrimeTypesToJSON

            case UserQuery.WriteDistricts => replyToActor ! ("Writing districts names to JSON file...\n\n" +
              "Check started jobs at " +
              ProcessedDataParser.getSparkAddress)
              ProcessedDataParser.writeDistrictsToJSON

            case UserQuery.WriteCrimesByDistrict => replyToActor ! ("Writing districts crimes to JSON file...\n\n" +
              "Check started jobs at " +
              ProcessedDataParser.getSparkAddress)
              ProcessedDataParser.writeCrimesByDistrictToJSON

            case UserQuery.WriteCrimesByCrimeType => replyToActor ! ("Writing crimes by crime type to JSON file...\n\n" +
              "Check started jobs at " +
              ProcessedDataParser.getSparkAddress)
              ProcessedDataParser.writeCrimesByCrimeTypeToJSON
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