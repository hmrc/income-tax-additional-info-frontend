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

package models

import models.gains.{CapitalRedemptionModel, ForeignModel, GainsSubmissionModel, LifeAnnuityModel, LifeInsuranceModel, PolicyCyaModel, VoidedIsaModel}
import play.api.libs.json.{JsObject, Json}
import support.UnitTest

class AllGainsSessionModelSpec extends UnitTest {

  val cyaModels: Seq[PolicyCyaModel] = Seq(
    PolicyCyaModel(
      "sessionId", "Life Insurance", Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
    ),
    PolicyCyaModel(
      "sessionId", "Life Annuity", Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
    ),
    PolicyCyaModel(
      "sessionId", "Capital Redemption", Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
    ),
    PolicyCyaModel(
      "sessionId", "Voided ISA", Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
    ),
    PolicyCyaModel(
      "sessionId", "Foreign Policy", Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
    ))

  val modelMax: AllGainsSessionModel = AllGainsSessionModel(cyaModels)

  val modelMin: AllGainsSessionModel = AllGainsSessionModel(Seq[PolicyCyaModel]())

  val submissionModel: GainsSubmissionModel = AllGainsSessionModel(cyaModels).toSubmissionModel

  val jsonMax: JsObject = Json.obj(
    "allGains" -> modelMax.allGains
  )

  val jsonMin: JsObject = Json.obj("allGains" -> Seq[PolicyCyaModel]())

  "AllGainsSessionModel" should {

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
        jsonMax.as[AllGainsSessionModel] shouldBe modelMax
      }

      "the json contains no data" in {
        jsonMin.as[AllGainsSessionModel] shouldBe modelMin
      }

    }
  }

}
