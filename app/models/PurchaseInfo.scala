package models

import akka.actor.ActorRef
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/** PurchaseID class identifying the player and venue, which he
  * desires to purchase.
  *
  * @constructor create new PurchaseID with playerID and venueID.
  * @param playerID player ID
  * @param venueID venue ID
  */
case class PurchaseID(playerID: PlayerID, venueID: VenueID)

/** PurchaseRef class including both the IDs of the purchase and a
  * reference of the [[actors.VenueActor]] actor, which will process
  * the venue.
  *
  * @note An instance of this class is a message, sent from a
  *      [[controllers.PlayerController]] to [[actors.PlayerActor]].
  *      It contains the ActorRef of a [[actors.VenueActor]], which
  *      will further process the purchase.
  *
  * @constructor create new PurchaseRef with ActorRef and purchase IDs.
  * @param ref VenueActor ActorRef
  * @param purchaseID purchase IDs
  */
case class PurchaseRef(ref: ActorRef, purchaseID: PurchaseID)

/** PurchaseInfo class containing the player and venue ID.
  *
  * @note This class presents a message, sent from [[actors.PlayerActor]]
  *      to [[actors.VenueActor]]. It contains the player ID and budget,
  *      which are gonna be used to check if player can afford the desired
  *      venue or not.
  *
  * @constructor create new player with id and money.
  * @param player player
  * @param venueID venue ID
  */
case class PurchaseInfo(player: Player, venueID: VenueID)

object PurchaseID {

  import Venue._
  import Player._

  /** Construct a PurchaseID from json. */
  implicit val purchaseReads: Reads[PurchaseID] = (
    (JsPath \ "playerID").read[PlayerID] and
    (JsPath \ "venueID").read[VenueID]
  )(PurchaseID.apply _)
}
