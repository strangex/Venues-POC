package controllers

import java.util.UUID
import javax.inject._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.concurrent.Future

import play.api.mvc._
import play.api.libs.json._
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import models.Messages._
import models.{Venue, VenueID}

/** This controller is responsible for defining actions to Create, Update,
  * and get all venues.
  */
@Singleton
class VenueController @Inject()(
    @Named("venue-actor") venueActor: ActorRef,
    components: ControllerComponents
)(implicit ec: ExecutionContext) extends AbstractController(components) {

  implicit val timeout: Timeout = 5.seconds

  /** Add/update venue. */
  def update(): Action[JsValue] = Action(parse.json).async { request =>
    val venueResult: JsResult[Venue] = request.body.validate[Venue]

    venueResult.fold(
      errors => {
        Future {
          BadRequest(
            Json.obj("status" -> BAD_REQUEST, "message" -> JsError.toJson(errors))
          )
        }
      },
      venue => {
        (venueActor ? venue).mapTo[SuccessNotification].map { notifier =>
          Ok(
            Json.obj("status" -> OK, "message" -> notifier.message)
          )
        }
      }
    )
  }

  /** List all venues. */
  def getAll() = Action.async {
    (venueActor ? GetAll).mapTo[JsValue].map { data =>
      Ok(
        Json.obj("status" -> OK, "data" -> data)
      )
    }
  }

  /** Delete venue. */
  def delete(id: UUID) = Action.async {
    (venueActor ? DeleteVenue(VenueID(id))).mapTo[Notification].map {
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
}
