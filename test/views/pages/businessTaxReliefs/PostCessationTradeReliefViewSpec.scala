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

import fixtures.messages.businessTaxReliefs.PostCessationTradeReliefMessages
import fixtures.messages.i18n
import forms.businessTaxReliefs.PostCessationTradeReliefForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.businessTaxReliefs.PostCessationTradeReliefView
import views.utils.BaseSelectors

class PostCessationTradeReliefViewSpec extends ViewUnitTest {

  private val page: PostCessationTradeReliefView = inject[PostCessationTradeReliefView]

  object Selectors extends BaseSelectors {
    val expensesSummary = "#detailExpenses summary"
    def expensesP(nth: Int): String = s"#detailExpenses div ${Selectors.p(nth)}"
    def expensesBullet(nth: Int): String = s"#detailExpenses div ${Selectors.bullet(nth)}"
    val liabilitiesSummary = "#detailLiabilities summary"
    def liabilitiesP(nth: Int): String = s"#detailLiabilities div ${Selectors.p(nth)}"
    val lossSummary = "#detailLoss summary"
    def lossP(nth: Int): String = s"#detailLoss div ${Selectors.p(nth)}"
  }

  override protected val userScenarios: Seq[UserScenario[PostCessationTradeReliefMessages.Messages with i18n, _]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, PostCessationTradeReliefMessages.English),
    UserScenario(isWelsh = false, isAgent = true, PostCessationTradeReliefMessages.EnglishAgent),
    UserScenario(isWelsh = true, isAgent = false, PostCessationTradeReliefMessages.Welsh),
    UserScenario(isWelsh = true, isAgent = true, PostCessationTradeReliefMessages.WelshAgent)
  )

  userScenarios.foreach { userScenario =>

    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {

      "render post-cessation trade relief amount page" which {

        implicit val request: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(
          taxYear = taxYear,
          form = PostCessationTradeReliefForm(),
          action = controllers.businessTaxReliefs.routes.PostCessationTradeReliefController.submit(taxYear)
        ).body)

        val expectedMessages = userScenario.commonExpectedResults

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedMessages.headingAndTitle, userScenario.isWelsh)
        h1Check(expectedMessages.headingAndTitle)
        textOnPageCheck(expectedMessages.p1, "main " + Selectors.p(1))
        textOnPageCheck(expectedMessages.p2, "main " + Selectors.p(2))
        textOnPageCheck(expectedMessages.expensesSummaryHeading, Selectors.expensesSummary)
        textOnPageCheck(expectedMessages.expensesSummaryP1, Selectors.expensesP(1))
        textOnPageCheck(expectedMessages.expensesSummaryBullet1, Selectors.expensesBullet(1))
        textOnPageCheck(expectedMessages.expensesSummaryBullet2, Selectors.expensesBullet(2))
        textOnPageCheck(expectedMessages.expensesSummaryBullet3, Selectors.expensesBullet(3))
        textOnPageCheck(expectedMessages.expensesSummaryBullet4, Selectors.expensesBullet(4))
        textOnPageCheck(expectedMessages.liabilitiesSummaryHeading, Selectors.liabilitiesSummary)
        textOnPageCheck(expectedMessages.liabilitiesSummaryP1, Selectors.liabilitiesP(1))
        textOnPageCheck(expectedMessages.lossSummaryHeading, Selectors.lossSummary)
        textOnPageCheck(expectedMessages.lossSummaryP1, Selectors.lossP(1))
        buttonCheck(expectedMessages.continue)
      }
    }
  }
}