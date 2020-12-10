import com.typesafe.config.ConfigFactory
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import org.apache.spark.sql.SparkSession

import scala.util.Failure
import scala.util.Success

object MainApp {

  /**
   * Starts the akka http server
   */
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
    // spark init before starting the server so users won't wait for it when the server is up
    SparkSession.builder
      .master("local[*]")
      .appName("Spark Parser")
      .getOrCreate()

    // server bootstrapping
    val rootBehavior = Behaviors.setup[Nothing] { context =>

      val dataViewerActor = context.spawn(new DataViewerActor()(), "DataViewerActor")
      val dataWriterActor = context.spawn(new DataWriterActor()(), "DataWriterActor")

      context.watch(dataViewerActor)
      context.watch(dataWriterActor)

      val routes = new AppRoutes(dataViewerActor, dataWriterActor)(context.system)
      startHttpServer(routes.topLevelRoute)(context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "UKCrimeDataWithSparkActorSystem")
  }
}
