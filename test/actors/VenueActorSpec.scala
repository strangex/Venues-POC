package actors

import java.util.UUID

import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json._

import models._
import models.Messages._

class VenueActorSpec
    extends TestKit(ActorSystem("VenueSpec"))
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {

  val venueActorRef: TestActorRef[VenueActor] = TestActorRef[VenueActor]
  val venueActor: VenueActor = venueActorRef.underlyingActor
  val fakePlayerID = PlayerID("player1")
  val fakePlayer = Player(fakePlayerID, 500)
  val fakeVenueID = VenueID(UUID.fromString("687e8292-1afd-4cf7-87db-ec49a3ed93b1"))
  val fakeVenue = Venue(fakeVenueID, "Rynek Główny", 1000)
  val corruptedVenueID =
    VenueID(UUID.fromString("127e8292-1afd-4cf7-87db-ec49a3ed93b1"))

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Venue Actor" when {
    "on its initial state" must {
      "send back an empty JsArray on receiving a GetAll" in {
        venueActorRef ! GetAll

        expectMsg(JsArray.empty)
      }

      "send back a SuccessNotification on receiving Venue " in {
        venueActorRef ! fakeVenue

        expectMsgType[SuccessNotification]
      }

      "send back a JsArray containing the added venue on receiving GetAll" in {
        venueActorRef ! GetAll

        expectMsg(JsArray(Json.toJson(fakeVenue) :: Nil))
      }
    }

    "making a purchase" must {
      "fail if venue doesn't exist" in {
        venueActorRef ! PurchaseInfo(fakePlayer, corruptedVenueID)

        expectMsgType[PurchaseFailed]
      }

      "fail if player can not afford it and venue isn't already owned" in {
        venueActorRef ! PurchaseInfo(fakePlayer, fakeVenueID)

        expectMsgType[PurchaseFailed]
      }

      "succeed if player can afford it" in {
        val fakePlayer = Player(PlayerID("player1"), 1000)

        venueActorRef ! PurchaseInfo(fakePlayer, fakeVenueID)

        expectMsgType[PurchaseSucceeded]
      }

      "fail if venue is already is already purchased" in {
        venueActorRef ! PurchaseInfo(fakePlayer, fakeVenueID)

        expectMsgType[PurchaseFailed]
      }
    }

    "trying to delete the purchased venue " must {
      "send back that venue if requested" in {
        venueActorRef ! fakeVenueID

        expectMsg(Some(fakeVenue.copy(ownerID = Some(fakePlayerID))))
      }

      "send back a FailureNotification on deleting a non-existent venue" in {
        venueActorRef ! DeleteVenue(corruptedVenueID)

        expectMsgType[FailureNotification]
      }

      "send back a SuccessNotification on deleting an existent venue" in {
        venueActorRef ! DeleteVenue(fakeVenueID)

        expectMsgType[SuccessNotification]
      }

      "send back a FailureNotification on receiving the same request again" in {
        venueActorRef ! DeleteVenue(fakeVenueID)

        expectMsgType[FailureNotification]
      }
    }
  }
}
