package controllers

import javax.inject._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json._
import play.api.mvc._
import akka.pattern.ask
import akka.actor.ActorRef
import akka.util.Timeout

import models.Messages._
import models.{PurchaseID, PurchaseRef}

/** This controller is responsible for defining an action to purchase a venue
  * by a specific player.
  */
@Singleton
class PlayerController @Inject()(
    @Named("player-actor") playerActor: ActorRef,
    @Named("venue-actor") venueActor: ActorRef,
    components: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(components) {

  implicit val timeout: Timeout = 10.seconds

  /** List all players. */
  def getAll() = Action.async {
    (playerActor ? GetAll).mapTo[JsValue].map { data =>
      Ok(
        Json.obj("status" -> OK, "data" -> data)
      )
    }
  }

  /** Purchase venue. */
  def purchase() = Action(parse.json).async { request =>
    val purchaseResult: JsResult[PurchaseID] = request.body.validate[PurchaseID]

    purchaseResult.fold(
      errors => {
        Future {
          BadRequest(
            Json.obj("status" -> BAD_REQUEST, "message" -> JsError.toJson(errors))
          )
        }
      },
      purchase => {
        val message = PurchaseRef(venueActor, purchase)

        (playerActor ? message).mapTo[Notification].map {
          case SuccessNotification(message) =>
            Ok(
              Json.obj("status" -> OK, "message" -> message)
            )

          case FailureNotification(message) =>
            BadRequest(
              Json.obj("status" -> BAD_REQUEST, "message" -> message)
            )
        }
      }
    )
  }
}
