package actors

import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

import akka.actor.Actor
import akka.pattern.ask
import akka.util.Timeout

import models.Messages._
import models._

class PlayerActor extends Actor {

  implicit val executionContext: ExecutionContext = context.dispatcher
  implicit val timeout: Timeout = 5.seconds

  val players: mutable.HashMap[PlayerID, Player] = mutable.HashMap(
    (PlayerID("player1"), Player(PlayerID("player1"), 500)),
    (PlayerID("player2"), Player(PlayerID("player2"), 2000))
  )

  def receive: PartialFunction[Any, Unit] = {
    case PurchaseRef(venueActor, PurchaseID(playerID, venueID)) =>
      players.get(playerID) match {
        case None =>
          sender() ! FailureNotification(
            s"Player $playerID doesn't exist!!"
          )

        case Some(player) =>
          val message = PurchaseInfo(player, venueID)
          val controller = sender()

          (venueActor ? message).mapTo[PurchaseStatus].map {
            case PurchaseFailed(reason) =>
              controller ! FailureNotification(reason)

            case PurchaseSuccess(updatedPlayer, venue) =>
              players.put(updatedPlayer.id, updatedPlayer)

              val message =
                s"""${venue.name} was bought by ${updatedPlayer.id} for
                   |${venue.price}. Player ${updatedPlayer.id} has
                   |${updatedPlayer.money}
                   |left.""".stripMargin.replaceAll("\n", " ")

              controller ! SuccessNotification(message)
          }
      }
  }
}
