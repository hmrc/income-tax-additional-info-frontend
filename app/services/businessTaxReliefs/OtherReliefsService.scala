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

package services.businessTaxReliefs


import connectors.businessTaxReliefs.OtherReliefsConnector
import models.{Done, UserAnswersModel}
import models.businessTaxReliefs.OtherReliefs
import pages.businessTaxReliefs._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class OtherReliefsService @Inject()(connector: OtherReliefsConnector) {

  def submit(taxYear: Int, userAnswersModel: UserAnswersModel)(implicit hc: HeaderCarrier): Future[Done] = {
    val qualifyingLoanRelief = userAnswersModel.get(QualifyingLoanReliefPage)
    val postCessationTradeRelief = userAnswersModel.get(PostCessationTradeReliefPage)
    val nonDeductibleReliefs = userAnswersModel.get(NonDeductibleReliefsPage)

    if (qualifyingLoanRelief.nonEmpty || postCessationTradeRelief.nonEmpty || nonDeductibleReliefs.nonEmpty) {
      val otherReliefs = OtherReliefs(
        qualifyingLoanInterestPayments = qualifyingLoanRelief,
        postCessationTradeReliefAndCertainOtherLosses = postCessationTradeRelief,
        nonDeductableLoanInterest = nonDeductibleReliefs
      )
      connector.submit(userAnswersModel.nino, taxYear, otherReliefs)
    } else {
        Future.successful(Done)
    }
  }
}
