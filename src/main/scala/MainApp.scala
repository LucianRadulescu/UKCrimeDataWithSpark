import com.typesafe.config.ConfigFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import assignment.spark.{InputDataParser, ProcessedDataParser}

import scala.util.Failure
import scala.util.Success

//#main-class
object MainApp {

  //#start-http-server
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    import system.executionContext

    val serverInterface = ConfigFactory.load().getString("app.localServer.interface")
    val serverPort = ConfigFactory.load().getInt("app.localServer.port")

    val futureBinding = Http().newServerAt(serverInterface, serverPort).bind(routes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    //#start spark
    InputDataParser.spark

    //#server-bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val aDataParser = new ProcessedDataParser();
      val dataViewerActor = context.spawn(new DataViewerActor(aDataParser)(), "DataViewerActor")
      val dataWriterActor = context.spawn(new DataWriterActor(aDataParser)(), "DataWriterActor")

      context.watch(dataViewerActor)
      context.watch(dataWriterActor)

      val routes = new AppRoutes(dataViewerActor, dataWriterActor)(context.system)
      startHttpServer(routes.topLevelRoute)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "UKCrimeDataWithSparkActorSystem")
    //#server-bootstrapping

  }
}
//#main-class
