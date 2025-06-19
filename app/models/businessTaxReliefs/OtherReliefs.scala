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

import play.api.libs.json.{JsObject, Json, OWrites, Writes}

case class OtherReliefs(qualifyingLoanInterestPayments: Option[BigDecimal],:
                        postCessationTradeReliefAndCertainOtherLosses: Option[BigDecimal],
                        nonDeductableLoanInterest: Option[BigDecimal])

object OtherReliefs {

  private def nestedReliefClaimedObj[A: Writes](value: Option[A])(key: String): JsObject =
    value.map(v => Json.obj(key -> Json.arr(Json.obj("reliefClaimed" -> v)))).getOrElse(Json.obj())

  implicit val writes: OWrites[OtherReliefs] = {
    case OtherReliefs(q, p, n) =>
      nestedReliefClaimedObj(q)("qualifyingLoanInterestPayments") ++
      nestedReliefClaimedObj(p)("postCessationTradeReliefAndCertainOtherLosses") ++
      nestedReliefClaimedObj(n)("nonDeductableLoanInterest")
  }

}
