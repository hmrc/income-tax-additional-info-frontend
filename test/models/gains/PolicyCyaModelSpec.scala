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

import java.util.UUID

class PolicyCyaModelSpec extends UnitTest {

  val sessionId: String = UUID.randomUUID().toString
  val modelMax: PolicyCyaModel = PolicyCyaModel(
    sessionId, "Life Insurance", Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11)
  )

  val modelMin: PolicyCyaModel = PolicyCyaModel(sessionId, "")

  val jsonMax: JsObject = Json.obj(
    "sessionId" -> sessionId,
    "policyType" -> "Life Insurance",
    "policyNumber" -> "123",
    "amountOfGain" -> 0,
    "policyEvent" -> "",
    "previousGain" -> true,
    "yearsPolicyHeld" -> 0,
    "yearsPolicyHeldPrevious" -> 0,
    "treatedAsTaxPaid" -> true,
    "taxPaidAmount" -> 123.11,
    "entitledToDeficiencyRelief" -> true,
    "deficiencyReliefAmount" -> 123.11
  )

  val jsonMin: JsObject = Json.obj("sessionId" -> sessionId, "policyType" -> "")

  "PolicyCyaModel" should {

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
        jsonMax.as[PolicyCyaModel] shouldBe modelMax
      }

      "the json contains no data" in {
        jsonMin.as[PolicyCyaModel] shouldBe modelMin
      }

    }

    "return true when model is full for voided isa" in {
      modelMax.copy(policyType = "Voided ISA").isFinished shouldBe true
    }

  }

}
