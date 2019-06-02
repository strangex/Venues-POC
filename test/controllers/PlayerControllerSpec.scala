package controllers

import scala.concurrent.Future

import akka.testkit.TestProbe
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc._
import play.api.test._
import play.api.libs.json._
import play.api.test.Helpers._

import models.Messages._
import models.PurchaseRef

class PlayerControllerSpec extends PlaySpec
  with Results
  with GuiceOneAppPerSuite {

  implicit val system = app.actorSystem

  val components = Helpers.stubControllerComponents()

  implicit lazy val executionContext = components.executionContext

  val playerProbe = TestProbe()
  val venueProbe = TestProbe()

  val controller = new PlayerController(
    playerProbe.ref,
    venueProbe.ref,
    components)

  val fakeJson = Json.parse(
    """
      |{
      | "venueID" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
      | "playerID" : "player1"
      |}
    """.stripMargin)

  def purchaseRequest(json: JsValue): Future[Result] = {
    val request = FakeRequest(PUT, "/venues/purchase")
      .withHeaders("Content-type" -> "application/json")
      .withBody(json)

    controller.purchase()(request)
  }

  "Player Controller" when {
    "performing purchase" must {
      "fail upon receiving an invalid json" in {
        val corruptedJson = Json.parse(
          """
            |{
            | "venueID" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1"
            |}
          """.stripMargin)

        val response: Future[Result] = purchaseRequest(corruptedJson)

        status(response) mustEqual BAD_REQUEST
      }

      "fail upon receiving a FailureNotification" in {
        val response: Future[Result] = purchaseRequest(fakeJson)

        playerProbe.expectMsgType[PurchaseRef]

        playerProbe.reply(FailureNotification(""))

        status(response) mustEqual BAD_REQUEST
      }

      "succeed upon receiving a SuccessNotification" in {
        val response: Future[Result] = purchaseRequest(fakeJson)

        playerProbe.expectMsgType[PurchaseRef]

        playerProbe.reply(SuccessNotification(""))

        status(response) mustEqual OK
      }
    }
  }
}
