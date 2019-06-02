package actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import models.{Player, PlayerID}

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
  val playerActor = playerActorRef.underlyingActor

  "Player Actor" when {
    "initially started" must {
      "have exactly these two players (player1, 500) and (player2, 2000) registered" in {
        val players = playerActor.players.values

        players must contain.allOf(
          Player(PlayerID("player1"), 500),
          Player(PlayerID("player2"), 2000)
        )
      }
    }
  }
}
