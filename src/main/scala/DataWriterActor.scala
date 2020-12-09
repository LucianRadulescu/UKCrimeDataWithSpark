//#data-writer-actor
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment.spark.{InputDataParser, ProcessedDataParser}
import com.typesafe.config.ConfigFactory

object DataWriterActor {

  // -- enumeration holding the queries that the user can run
  object UserQuery extends Enumeration {
    type UserQuery = Value
    val ParseInitialFilesAndWriteParquetFile,
    WriteCrimeTypes,
    WriteDistricts,
    WriteCrimesByDistrict,
    WriteCrimesByCrimeType,
    GetQueries = Value
  }

  def getQueries: String = {
    val serverInterface = ConfigFactory.load().getString("app.localServer.interface")
    val serverPort = ConfigFactory.load().getInt("app.localServer.port")
    val localAddress = "http://" + serverInterface + ":" + serverPort + "/"
    localAddress + "write/" + UserQuery.values.mkString("\n" + localAddress + "write/")
  }

  // actor protocol - these are the different messages that the actor knows how to handle
  sealed trait Command
  final case class RunCommand(command: String, replyTo: ActorRef[String]) extends Command

}

class DataWriterActor(dataParser : ProcessedDataParser) {

  def apply(): Behavior[DataWriterActor.Command] = {
    Behaviors.receiveMessage {
      case DataWriterActor.RunCommand(command, replyToActor) =>
        try {
          DataWriterActor.UserQuery.withName(command) match {
            case DataWriterActor.UserQuery.GetQueries => replyToActor ! ("Showing available queries below:\n\n" + DataWriterActor.getQueries)

            case DataWriterActor.UserQuery.ParseInitialFilesAndWriteParquetFile => replyToActor ! "Started parsing of input files, check out the jobs at " + InputDataParser.spark.sparkContext.uiWebUrl.get
              InputDataParser.parseInputFiles

            case DataWriterActor.UserQuery.WriteCrimeTypes => replyToActor ! ("Writing crime types to JSON file...\n\n" +
              "Check started jobs at " +
              dataParser.getSparkAddress)
              dataParser.writeCrimeTypesToJSON

            case DataWriterActor.UserQuery.WriteDistricts => replyToActor ! ("Writing districts names to JSON file...\n\n" +
              "Check started jobs at " +
              dataParser.getSparkAddress)
              dataParser.writeDistrictsToJSON

            case DataWriterActor.UserQuery.WriteCrimesByDistrict => replyToActor ! ("Writing crimes by district to JSON file...\n\n" +
              "Check started jobs at " +
              dataParser.getSparkAddress)
              dataParser.writeCrimesByDistrictToJSON

            case DataWriterActor.UserQuery.WriteCrimesByCrimeType => replyToActor ! ("Writing crimes by crime type to JSON file...\n\n" +
              "Check started jobs at " +
              dataParser.getSparkAddress)
              dataParser.writeCrimesByCrimeTypeToJSON
          }
        } catch {
          case e : NoSuchElementException => replyToActor ! (
            "Received unknown command \n" +
              command +
              "\nTry one of the available queries from below: \n\n" + DataWriterActor.getQueries)
          case other => throw other
        }
        Behaviors.same
    }
  }
}
//#data-viewer-actor