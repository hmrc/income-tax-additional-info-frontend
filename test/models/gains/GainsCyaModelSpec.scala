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

package models.gains

import play.api.libs.json.{JsObject, Json}
import support.UnitTest

class GainsCyaModelSpec extends UnitTest {

  val modelMax: GainsCyaModel = GainsCyaModel(
    Some(true), Some("123"), Some("cause"), Some(true), Some("2"), Some(123.11), Some("2"), Some(true), Some(123.11), Some(true), Some(123.11)
  )

  val modelMin: GainsCyaModel = GainsCyaModel()

  val jsonMax: JsObject = Json.obj(
    "gatewayQuestion" -> true,
    "customerReference" -> "123",
    "whatCausedThisGain" -> "cause",
    "previousGain" -> true,
    "yearsSinceLastGain" -> "2",
    "howMuchGain" -> 123.11,
    "policyYearsHeld" -> "2",
    "paidTaxOnGain" -> true,
    "taxPaid" -> 123.11,
    "entitledToDeficiencyRelief" -> true,
    "amountAvailableForRelief" -> 123.11
  )

  val jsonMin: JsObject = Json.obj()

  "GainsCyaModel" should {

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
        jsonMax.as[GainsCyaModel] shouldBe modelMax
      }

      "the json contains no data" in {
        jsonMin.as[GainsCyaModel] shouldBe modelMin
      }

    }

  }

}
