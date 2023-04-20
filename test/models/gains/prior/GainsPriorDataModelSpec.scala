/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.gains.prior

import models.gains.{CapitalRedemptionModel, ForeignModel, LifeAnnuityModel, LifeInsuranceModel, PolicyCyaModel, VoidedIsaModel}
import play.api.libs.json.{JsObject, Json}
import support.UnitTest

class GainsPriorDataModelSpec extends UnitTest {

  val modelMax: GainsPriorDataModel = GainsPriorDataModel(
    "",
    Seq[LifeInsuranceModel](LifeInsuranceModel(gainAmount = 123.11)),
    Some(Seq(CapitalRedemptionModel(gainAmount = 123.11))),
    Some(Seq(LifeAnnuityModel(gainAmount = 123.11))),
    Some(Seq(VoidedIsaModel(gainAmount = 123.11))),
    Some(Seq(ForeignModel(gainAmount = 123.11)))
  )

  val modelMin: GainsPriorDataModel = GainsPriorDataModel("")

  val jsonMax: JsObject = Json.obj(
    "submittedOn" -> "",
    "lifeInsurance" -> Seq[LifeInsuranceModel](LifeInsuranceModel(gainAmount = 123.11)),
    "capitalRedemption" -> Some(Seq(CapitalRedemptionModel(gainAmount = 123.11))),
    "lifeAnnuity" -> Some(Seq(LifeAnnuityModel(gainAmount = 123.11))),
    "voidedIsa" -> Some(Seq(VoidedIsaModel(gainAmount = 123.11))),
    "foreign" -> Some(Seq(ForeignModel(gainAmount = 123.11)))
  )

  val jsonMin: JsObject = Json.obj("submittedOn" -> "", "lifeInsurance" -> Seq[LifeInsuranceModel]())

  "GainsPriorDataModel" should {

    "correctly parse to Json" when {

      "the model is fully filled out" in {
        Json.toJson(modelMax) shouldBe jsonMax
      }

      "the model is empty" in {
        Json.toJson(modelMin) shouldBe jsonMin
      }

    }

    "correctly parse to a model" when {

      "the json contains all the data for the model" in {
        jsonMax.as[GainsPriorDataModel] shouldBe modelMax
      }

      "the json contains no data" in {
        jsonMin.as[GainsPriorDataModel] shouldBe modelMin
      }

    }

    "correctly map to PolicyCyaModel" when {
      "each prior model has data" in {
        val result = modelMax.toPolicyCya
        result shouldBe
          List(
            PolicyCyaModel(result.head.sessionId, "Life Insurance", None, Some(123.11), Some(""), Some(false), None, None, Some(false), None, Some(false), None),
            PolicyCyaModel(result(1).sessionId, "Capital Redemption", None, Some(123.11), Some(""), Some(false), None, Some(0), Some(false), None, Some(false), None),
            PolicyCyaModel(result(2).sessionId, "Life Annuity", Some(""), Some(123.11), Some(""), Some(false), None, Some(0), Some(false), None, Some(false), None),
            PolicyCyaModel(result(3).sessionId, "Foreign Policy", None, Some(123.11), Some(""), Some(false), None, Some(0), Some(false), None, Some(false), Some(0))          )
      }
    }
  }

}
