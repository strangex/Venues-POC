package models

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

/** VenueID class.
  *
  * @constructor create new VenueID with an ID.
  * @param id venue ID
  */
case class VenueID(id: UUID) extends AnyVal {

  override def toString(): String = id.toString
}

/** Venue class.
  *
  * @constructor create new Venue with id, name, price, and owner ID.
  * @param id venue ID
  * @param name venue name
  * @param price venue cost
  * @param ownerID ID of the player that owns this venue
  */
case class Venue(id: VenueID, name: String, price: Int, ownerID: Option[PlayerID] = None)

object Venue {

  import Player.playerIDReads

  /** Custom PlayerID writes. Used to create owner key in json. */
  implicit val ownerIDWrites: Writes[PlayerID] =
    (JsPath \ "owner").write[String].contramap(_.id)

  /** Write a json from VenueID. */
  implicit val venueIDWrites: Writes[VenueID] =
    (JsPath \ "id").write[UUID].contramap(_.id)

  /** Write a json from Venue. */
  implicit val venueWrites: Writes[Venue] = (
    (JsPath).write[VenueID] and
    (JsPath \ "name").write[String] and
    (JsPath \ "price").write[Int] and
    JsPath.writeNullable[PlayerID]
  )(unlift(Venue.unapply))

  /** Construct a VenueID from json. */
  implicit val venueIDReads: Reads[VenueID] =
    JsPath.read[UUID].map(VenueID.apply)

  /** Construct a Venue from json. */
  implicit val venueReads: Reads[Venue] = (
    (JsPath \ "id").read[VenueID] and
    (JsPath \ "name").read[String] and
    (JsPath \ "price").read[Int] and
    (JsPath \ "owner").readNullable[PlayerID]
  )(Venue.apply _)
}
