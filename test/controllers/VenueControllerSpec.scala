package controllers

import java.util.UUID

import scala.concurrent.Future

import akka.testkit.TestProbe
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc._
import play.api.test._
import play.api.libs.json._
import play.api.test.Helpers._

import models.Messages._
import models.Venue

class VenueControllerSpec
    extends PlaySpec
    with Results
    with GuiceOneAppPerSuite {

  implicit val system = app.actorSystem

  val components = Helpers.stubControllerComponents()

  implicit lazy val executionContext = components.executionContext

  val probe = TestProbe()
  val controller = new VenueController(probe.ref, components)

  def putRequest(json: JsValue): Future[Result] = {
    val request = FakeRequest(PUT, "/venues")
      .withHeaders("Content-type" -> "application/json")
      .withBody(json)

    controller.update()(request)
  }

  "Venue Controller" when {
    "executing update" must {
      "fail upon receiving an invalid json" in {
        val fakeJson = Json.parse(
          """
            |{
            | "id" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1"
            |}
          """.stripMargin)

        val response: Future[Result] = putRequest(fakeJson)

        status(response) mustEqual BAD_REQUEST
      }

      "succeed upon receiving a well formatted json" in {
        val fakeJson = Json.parse(
          """
            |{
            | "id" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
            | "name" : "Rynek Główny",
            | "price" : 1000
            |}
          """.stripMargin)

        val response: Future[Result] = putRequest(fakeJson)

        probe.expectMsgType[Venue]

        probe.reply(
          SuccessNotification(
            "Venue 687e8292-1afd-4cf7-87db-ec49a3ed93b1 has been created!!"
          )
        )

        status(response) mustEqual OK
      }
    }

    "executing GetAll" must {
      "succeed" in {
        val request = FakeRequest(GET, "/venues")

        val response = controller.getAll()(request)

        probe.expectMsg(GetAll)

        probe.reply(JsArray())

        status(response) mustEqual OK
      }
    }

    "executing delete" must {
      val url = "/venues/687e8292-1afd-4cf7-87db-ec49a3ed93b1"
      val id = UUID.fromString("687e8292-1afd-4cf7-87db-ec49a3ed93b1")
      val request = FakeRequest(DELETE, url)

      def getResponse() = controller.delete(id)(request)

      "succeed on upon receiving a SuccessNotification" in {
        val response = getResponse()

        probe.expectMsgType[DeleteVenue]

        probe.reply(SuccessNotification(""))

        status(response) mustEqual OK
      }

      "fail on upon receiving a FailureNotification" in {
        val response = getResponse()

        probe.expectMsgType[DeleteVenue]

        probe.reply(FailureNotification(""))

        status(response) mustEqual BAD_REQUEST
      }
    }
  }
}
