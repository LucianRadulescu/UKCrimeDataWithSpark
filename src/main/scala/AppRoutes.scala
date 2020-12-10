import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

class AppRoutes(dataViewerActor: ActorRef[DataViewerActor.Command],
                dataWriterActor: ActorRef[DataWriterActor.Command])(implicit val system: ActorSystem[_]) {

  // If ask takes more time than this to complete then the request is failed
  private implicit val timeout = Timeout(15.seconds)

  def dispatchCommandToDataViewerActor(command: String, district : Option[String] = None): Future[String] = {
    dataViewerActor.ask(DataViewerActor.RunCommand(command, district, _))
  }

  def dispatchCommandToDataWriterActor(command: String): Future[String] = {
    dataWriterActor.ask(DataWriterActor.RunCommand(command, _))
  }

  lazy val topLevelRoute: Route =
    concat(
      pathPrefix("view")(viewRoutes),
      pathPrefix("write")(writeRoutes),
      pathEndOrSingleSlash { complete("Oh, hello there... you can try to run some of the queries below. Cheers, Lucian !\n\n" +
        DataViewerActor.getViewQueries +
        " \n\nor\n\n" +
        DataWriterActor.getWriteQueries) }
    )

  lazy val writeRoutes: Route =
    concat(
      path(Segment) { command =>
        get {
              onSuccess(dispatchCommandToDataWriterActor(command)) { response => complete(response) }
        }
      },
      pathEnd {
        get {
          onSuccess(dispatchCommandToDataWriterActor("")) { response => complete(response) }
        }
      }
    )

  lazy val viewRoutes: Route =
    concat(
      path(Segment) { command =>
          get {
            parameter("district".optional) {
              district =>
              onSuccess(dispatchCommandToDataViewerActor(command, district)) { response => complete(response) }
            }
          }
      },
      pathEnd {
        get {
          onSuccess(dispatchCommandToDataViewerActor("")) { response => complete(response) }
        }
      }
    )
}
