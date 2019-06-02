package actors

import java.util.UUID

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.actor.ActorSystem
import play.api.libs.json.{JsArray, Json}

import models.{Venue, VenueID}
import models.Messages._

class VenueActorSpec
    extends TestKit(ActorSystem("VenueSpec"))
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {

  val venueActorRef = TestActorRef[VenueActor]
  val venueActor = venueActorRef.underlyingActor
  val id = VenueID(UUID.fromString("687e8292-1afd-4cf7-87db-ec49a3ed93b1"))
  val venue = Venue(id, "Rynek Główny", 1000)

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
        venueActorRef ! venue

        expectMsgType[SuccessNotification]
      }

      "send back a JsArray containing the added venue on receiving GetAll" in {
        venueActorRef ! GetAll

        expectMsg(JsArray(Json.toJson(venue) :: Nil))
      }
    }

    "containing a single venue " must {
      "send back that venue if requested" in {
        venueActorRef ! id

        expectMsg(Some(venue))
      }

      "send back a FailureNotification on a different venue ID" in {
        val fakeID = VenueID(UUID.fromString("127e8292-1afd-4cf7-87db-ec49a3ed93b1"))

        venueActorRef ! DeleteVenue(fakeID)

        expectMsgType[FailureNotification]
      }

      "send back a SuccessNotification on receiving a delete request" in {
        venueActorRef ! DeleteVenue(id)

        expectMsgType[SuccessNotification]
      }

      "send back a FailureNotification on receiving the same request again" in {
        venueActorRef ! DeleteVenue(id)

        expectMsgType[FailureNotification]
      }
    }
  }
}
