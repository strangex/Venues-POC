package models

import java.util.UUID

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json._

class VenueSpec extends WordSpec with MustMatchers {

  "Venue model" when {
    "trying to be constructed from a json" must {
      "fail if json is mal formatted" in {
        val json = Json.parse(
          """
            |{
            | "id" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
            | "price" : 1000
            |}
          """.stripMargin
        )

        json.validate[Venue] mustBe 'error
      }

      "be successfully validated if json has no owner key" in {
        val json = Json.parse(
          """
            |{
            | "id" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
            | "name" : "Rynek Główny",
            | "price" : 1000
            |}
          """.stripMargin
        )

        json.validate[Venue] mustBe 'success
      }

      "be successfully validated if json has an owner key" in {
        val json = Json.parse(
          """
            |{
            | "id" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
            | "name" : "Rynek Główny",
            | "price" : 1000,
            | "owner" : "player2"
            |}
          """.stripMargin
        )

        json.validate[Venue] mustBe 'success
      }
    }

    "has instance" must {
      val id = VenueID(UUID.fromString("687e8292-1afd-4cf7-87db-ec49a3ed93b1"))

      "return a json without owner key if venue has no owner" in {
        val venue = Venue(id, "Rynek Główny", 1000)
        val json = Json.toJson(venue)

        (json \ "owner") mustBe a[JsUndefined]
      }

      """return a json with owner key, which doesn't contain a nested id,
        | if venue has an owner""".stripMargin in {
        val venue = Venue(id, "Rynek Główny", 1000, Some(PlayerID("player2")))
        val json = Json.toJson(venue)

        (json \ "owner") mustBe a[JsDefined]
        (json \ "owner" \ "id") mustBe a[JsUndefined]
      }
    }
  }
}
