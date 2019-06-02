package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

import actors.PlayerActor

class PlayerActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure: Unit = {
    bindActor[PlayerActor]("player-actor")
  }
}
