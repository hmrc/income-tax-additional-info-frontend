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

import models.UserAnswersModel
import pages.businessTaxReliefs.{NonDeductibleReliefsPage, PostCessationTradeReliefPage, QualifyingLoanReliefPage}
import play.api.libs.json.Json.{arr, obj}
import play.api.libs.json.OWrites

case class OtherReliefs(qualifyingLoanInterestPayments: Option[BigDecimal],
                        postCessationTradeReliefAndCertainOtherLosses: Option[BigDecimal],
                        nonDeductableLoanInterest: Option[BigDecimal])

object OtherReliefs {

  def apply(userAnswersModel: UserAnswersModel): Option[OtherReliefs] =
    (
      userAnswersModel.get(QualifyingLoanReliefPage),
      userAnswersModel.get(PostCessationTradeReliefPage),
      userAnswersModel.get(NonDeductibleReliefsPage)
    ) match {
      case (None, None, None) => None
      case (q, p, n) =>
        Some(OtherReliefs(
          qualifyingLoanInterestPayments = q,
          postCessationTradeReliefAndCertainOtherLosses = p,
          nonDeductableLoanInterest = n
        ))
    }

  implicit val writes: OWrites[OtherReliefs] = {
    case OtherReliefs(q, p, n) =>
      q.fold(obj())(value => obj("qualifyingLoanInterestPayments" -> arr(obj("reliefClaimed" -> value)))) ++
      p.fold(obj())(value => obj("postCessationTradeReliefAndCertainOtherLosses" -> arr(obj("amount" -> value)))) ++
      n.fold(obj())(value => obj("nonDeductableLoanInterest" -> obj("reliefClaimed" -> value)))
  }

}
