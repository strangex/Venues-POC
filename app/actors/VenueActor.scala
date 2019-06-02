package actors

import scala.collection.mutable

import akka.actor._
import play.api.libs.json._

import models.Messages._
import models._

class VenueActor extends Actor {

  val venues: mutable.HashMap[VenueID, Venue] = mutable.HashMap()

  def receive: PartialFunction[Any, Unit] = {
    case GetAll =>
      sender() ! Json.toJson(venues.values)

    case venue @ Venue(venueID, _, _, _) =>
      val operation = if (venues.contains(venueID)) "updated" else "created"

      venues.put(venueID, venue)

      sender() ! SuccessNotification(
        s"Venue $venueID $operation successfully!!"
      )

    case DeleteVenue(venueID) =>
      if (venues contains venueID) {
        venues -= venueID
        sender() ! SuccessNotification(
          s"Venue $venueID has been deleted!!"
        )
      } else {
        sender() ! FailureNotification(
          s"Venue $venueID does not exist!!"
        )
      }

    case venueID: VenueID =>
      sender() ! venues.get(venueID)

    case PurchaseInfo(player, venueID) =>
      venues.get(venueID) match {
        case None =>
          sender() ! PurchaseFailed(s"Venue $venueID doesn't exist!!")

        case Some(venue) =>
          venue.ownerID match {
            case Some(otherID) =>
              sender() ! purchaseFailureReason(player, otherID, venueID)

            case None =>
              sender() ! purchase(player, venue)
          }
      }
  }

  private def purchaseFailureReason(
      player: Player,
      otherID: PlayerID,
      venueID: VenueID
  ): PurchaseFailed = {
    val reason =
      if (otherID == player.id) {
        s"Player ${player.id} has already bought venue $venueID!!"
      } else {
        s"Venue $venueID is already bought by $otherID!!"
      }

    PurchaseFailed(reason)
  }

  private def purchase(player: Player, venue: Venue): PurchaseStatus = {
    if (venue.price > player.money) {
      PurchaseFailed(
        s"Player ${player.id} can't afford ${venue.name}!!"
      )
    } else {
      venues.put(venue.id, venue.copy(ownerID = Some(player.id)))

      PurchaseSucceeded(venue)
    }
  }
}
