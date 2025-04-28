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

package views.pages.businessTaxReliefs

import fixtures.messages.businessTaxReliefs.BusinessReliefsNonDeductibleMessages
import fixtures.messages.i18n
import models.BusinessTaxReliefs
import models.requests.JourneyDataRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pages.businessTaxReliefs.NonDeductibleReliefsPage
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import utils.ViewUtils.bigDecimalCurrency
import views.html.pages.businessTaxReliefs.CheckAnswersNonDeductiblePageView
import views.utils.BaseSelectors

class CheckAnswersNonDeductiblePageViewSpec extends ViewUnitTest {

  private val page: CheckAnswersNonDeductiblePageView = inject[CheckAnswersNonDeductiblePageView]

  object Selectors extends BaseSelectors

  override protected val userScenarios: Seq[UserScenario[BusinessReliefsNonDeductibleMessages.Messages with i18n, _]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, BusinessReliefsNonDeductibleMessages.English),
    UserScenario(isWelsh = false, isAgent = true, BusinessReliefsNonDeductibleMessages.EnglishAgent),
    UserScenario(isWelsh = true, isAgent = false, BusinessReliefsNonDeductibleMessages.Welsh),
    UserScenario(isWelsh = true, isAgent = true, BusinessReliefsNonDeductibleMessages.WelshAgent)
  )

  userScenarios.foreach { userScenario =>

    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {

      "render the Check Answers for Qualifying Loan Interest page" which {

        val authRequest = getAuthRequest(userScenario.isAgent)
        implicit val request: JourneyDataRequest[AnyContent] = JourneyDataRequest(
          authRequest.user,
          authRequest,
          emptyUserAnswers(taxYear, BusinessTaxReliefs)
            .set(NonDeductibleReliefsPage, BigDecimal("1000.01"))
        )
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(
          taxYear = taxYear,
          action = controllers.businessTaxReliefs.routes.CheckAnswersNonDeductibleController.submit(taxYear)
        ).body)

        val expectedMessages = userScenario.commonExpectedResults

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedMessages.cya, userScenario.isWelsh)
        captionCheck(expectedMessages.taxYearCaption(taxYear))
        h1Check(expectedMessages.cya)
        textOnPageCheck(expectedMessages.cyaLabel, Selectors.summaryKey(row = 1))
        textOnPageCheck(bigDecimalCurrency("1000.01"), Selectors.summaryValue(row = 1))
        textOnPageCheck(expectedMessages.change, Selectors.summaryActions(row = 1, link = 1))
        buttonCheck(expectedMessages.continue)
      }
    }
  }
}
