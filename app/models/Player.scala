package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

/** PlayerID class.
  *
  * @constructor create new player ID with an ID.
  * @param id player ID
  */
case class PlayerID(id: String) extends AnyVal {

  override def toString(): String = id
}

/** Player class.
  *
  * @constructor create new player with id and money.
  * @param id player ID
  * @param money player budget
  */
case class Player(id: PlayerID, money: Int)

object Player {

  /** Write a json from PlayerID. */
  implicit val playerIDWrites: Writes[PlayerID] =
    (JsPath \ "id").write[String].contramap(_.id)

  /** Write a json from PlayerID. */
  implicit val playerWrites: Writes[Player] = (
    JsPath.write[PlayerID] and
    (JsPath \ "money").write[Int]
  )(unlift(Player.unapply))

  /** Construct a PlayerID from json. */
  implicit val playerIDReads: Reads[PlayerID] =
    JsPath.read[String].map(PlayerID.apply)
}
