package actors

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}
import play.api.libs.json._

import models._
import models.Messages._

class PlayerActorSpec
    extends TestKit(ActorSystem("PlayerSpec"))
    with WordSpecLike
    with ImplicitSender
    with MustMatchers
    with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val playerActorRef: TestActorRef[PlayerActor] = TestActorRef[PlayerActor]
  val playerActor: PlayerActor = playerActorRef.underlyingActor
  val fakeVenueActor = TestProbe()
  val fakeVenueID =
    VenueID(UUID.fromString("127e8292-1afd-4cf7-87db-ec49a3ed93b1"))

  "Player Actor" when {
    "initially started" must {
      val players = playerActor.players.values

      """have exactly these two players (player1, 500) and
        | (player2, 2000) registered""".stripMargin in {
        players must contain.allOf(
          Player(PlayerID("player1"), 500),
          Player(PlayerID("player2"), 2000)
        )
      }

      "send back a JsArray containing the two hard coded players" in {
        playerActorRef ! GetAll

        expectMsg(Json.toJson(players))
      }
    }

    "trying to make a venue purchase with a non-existent player" must {
      "fail" in {
        val fakePlayerID = PlayerID("player0")
        val fakePurchase = PurchaseID(fakePlayerID, fakeVenueID)
        val fakePurchaseRef = PurchaseRef(fakeVenueActor.ref, fakePurchase)

        playerActorRef ! fakePurchaseRef

        expectMsgType[FailureNotification]
      }
    }

    "trying to make a purchase with an existent player" when {
      "dealing with hardcoded player" must {
        val fakePlayerID = PlayerID("player2")
        val fakePurchase = PurchaseID(fakePlayerID, fakeVenueID)
        val fakePurchaseRef = PurchaseRef(fakeVenueActor.ref, fakePurchase)

        "fail upon receiving a PurchaseFailed from venue Actor" in {
          playerActorRef ! fakePurchaseRef

          fakeVenueActor.expectMsgType[PurchaseInfo]

          fakeVenueActor.reply(PurchaseFailed(""))

          expectMsgType[FailureNotification]
        }

        """succeed upon receiving a PurchaseSuccess,
          | and player's budget must be decreased by
          | the price of the venue""".stripMargin in {
          val fakeVenue = Venue(fakeVenueID, "Rynek Główny", 1000)

          playerActorRef ! fakePurchaseRef

          fakeVenueActor.expectMsgType[PurchaseInfo]

          fakeVenueActor.reply(PurchaseSucceeded(fakeVenue))

          expectMsgType[SuccessNotification]

          playerActor.players(fakePlayerID).money mustEqual 1000
        }
      }
    }
  }
}
