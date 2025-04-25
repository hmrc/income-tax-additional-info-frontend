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

package viewmodels.businessTaxReliefs

import models.requests.JourneyDataRequest
import pages.businessTaxReliefs.PostCessationTradeReliefPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.ViewUtils.bigDecimalCurrency

object CheckAnswersPostCessationTradeReliefSummary {

  def summary(taxYear: Int)(implicit request: JourneyDataRequest[_], messages: Messages): SummaryList =
    SummaryList(Seq(
      request.userAnswers.get(PostCessationTradeReliefPage).map { answer =>
        SummaryListRow(
          key = Key(
            content = Text(messages("postCessationTradeRelief.check-answers.question")),
            classes = "govuk-!-width-two-thirds"
          ),
          value = Value(
            content = Text(bigDecimalCurrency(answer.toString())),
            classes = "govuk-!-width-one-third"
          ),
          actions =
            Some(Actions(items = Seq(
              ActionItem(
                href = controllers.businessTaxReliefs.routes.PostCessationTradeReliefController.show(taxYear).url,
                content = Text(messages("common.change")),
                visuallyHiddenText = Some(messages("postCessationTradeRelief.check-answers.question"))
              )
            )))
        )
      }
    ).flatten)
}
