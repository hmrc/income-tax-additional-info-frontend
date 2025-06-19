/*
 * Copyright 2025 HM Revenue & Customs
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

package models.businessTaxReliefs

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class OtherReliefsSpec extends AnyFreeSpec with Matchers {
  "JSON Writes" - {
    "when all values are defined" in {
      val otherReliefs = OtherReliefs(
        Some(BigDecimal(1)),
        Some(BigDecimal(2)),
        Some(BigDecimal(3))
      )

      val expected = Json.obj(
        "qualifyingLoanInterestPayments" -> Json.arr(Json.obj("reliefClaimed" -> BigDecimal(1))),
        "postCessationTradeReliefAndCertainOtherLosses" -> Json.arr(Json.obj("reliefClaimed" -> BigDecimal(2))),
        "nonDeductableLoanInterest" -> Json.arr(Json.obj("reliefClaimed" -> BigDecimal(3)))
      )

      Json.toJson(otherReliefs) mustEqual expected
    }

    "when some values are defined" - {
      "qualifyingLoanInterestPayments" in {
        val otherReliefs = OtherReliefs(
          Some(BigDecimal(1)),
          None,
          None
        )

        val expected = Json.obj(
          "qualifyingLoanInterestPayments" -> Json.arr(Json.obj("reliefClaimed" -> BigDecimal(1)))
        )

        Json.toJson(otherReliefs) mustEqual expected
      }

      "postCessationTradeReliefAndCertainOtherLosses" in {
        val otherReliefs = OtherReliefs(
          None,
          Some(BigDecimal(2)),
          None
        )

        val expected = Json.obj(
          "postCessationTradeReliefAndCertainOtherLosses" -> Json.arr(Json.obj("reliefClaimed" -> BigDecimal(2)))
        )

        Json.toJson(otherReliefs) mustEqual expected
      }

      "nonDeductableLoanInterest" in {
        val otherReliefs = OtherReliefs(
          None,
          None,
          Some(BigDecimal(3))
        )

        val expected = Json.obj(
          "nonDeductableLoanInterest" -> Json.arr(Json.obj("reliefClaimed" -> BigDecimal(3)))
        )

        Json.toJson(otherReliefs) mustEqual expected
      }
    }
  }
}
