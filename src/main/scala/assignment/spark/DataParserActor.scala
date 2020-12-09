package assignment.spark

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors

object DataParserActor {
  sealed trait Command
  case class Parse(command : String, replyTo: ActorRef[Response]) extends Command
  case class Response(message: String)
}

class DataParserActor() {

  def apply() : Behaviors.Receive[DataParserActor.Command] = {
    Behaviors.receiveMessage[DataParserActor.Command] {
      case DataParserActor.Parse(command, replyTo) => {
        replyTo ! DataParserActor.Response("I'm sorry, Dave. I'm afraid I can't do that.")
        Behaviors.same
      }
    }
  }

}
