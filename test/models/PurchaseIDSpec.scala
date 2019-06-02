package models

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json._

class PurchaseIDSpec extends WordSpec with MustMatchers {

  "Trying to construct PurchaseID from json" must {
    "fail if json is mal formatted" in {
      val json = Json.parse(
        """
          |{
          | "playerID" : "player2"
          |}
        """.stripMargin
      )

      json.validate[PurchaseID] mustBe 'error
    }

    "success if json is well formatted" in {
      val json = Json.parse(
        """
          |{
          | "venueID" : "687e8292-1afd-4cf7-87db-ec49a3ed93b1",
          | "playerID" : "player2"
          |}
        """.stripMargin
      )

      json.validate[PurchaseID] mustBe 'success
    }
  }
}
