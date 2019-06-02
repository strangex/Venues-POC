package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

import actors.VenueActor

class VenueActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure: Unit = {
    bindActor[VenueActor]("venue-actor")
  }
}
