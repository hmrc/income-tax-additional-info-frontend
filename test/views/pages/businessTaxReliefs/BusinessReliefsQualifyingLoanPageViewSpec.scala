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

import fixtures.messages.BusinessReliefsQualifyingLoanMessages
import fixtures.messages.i18n
import forms.AmountForm
import models.BusinessTaxReliefs
import models.requests.{AuthorisationRequest, JourneyDataRequest}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.businessTaxReliefs.QualifyingLoanPageView
import views.utils.BaseSelectors

class BusinessReliefsQualifyingLoanPageViewSpec extends ViewUnitTest {

  private val page: QualifyingLoanPageView = inject[QualifyingLoanPageView]

  object Selectors extends BaseSelectors {
    val expensesSummary = "#detailExpenses summary"
    val expensesP: Int => String = i => s"#detailExpenses div ${Selectors.p(i)}"
    val expensesBullet: Int => String = i => s"#detailExpenses div ${Selectors.bullet(i)}"
    val liabilitiesSummary = "#detailLiabilities summary"
    val liabilitiesP: Int => String = i => s"#detailLiabilities div ${Selectors.p(i)}"
    val lossSummary = "#detailLoss summary"
    val lossP: Int => String = i => s"#detailLoss div ${Selectors.p(i)}"
  }

  override protected val userScenarios: Seq[UserScenario[BusinessReliefsQualifyingLoanMessages.Messages with i18n, _]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, BusinessReliefsQualifyingLoanMessages.English),
    UserScenario(isWelsh = false, isAgent = true, BusinessReliefsQualifyingLoanMessages.EnglishAgent),
    UserScenario(isWelsh = true, isAgent = false, BusinessReliefsQualifyingLoanMessages.Welsh),
    UserScenario(isWelsh = true, isAgent = true, BusinessReliefsQualifyingLoanMessages.WelshAgent)
  )

  userScenarios.foreach { userScenario =>

    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {

      "render post-cessation trade relief amount page" which {

        val authRequest = getAuthRequest(userScenario.isAgent)
        implicit val request: JourneyDataRequest[AnyContent] = JourneyDataRequest(authRequest.user, authRequest, emptyUserAnswers(taxYear, BusinessTaxReliefs))
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        val form: Form[BigDecimal] = AmountForm.amountForm(
          emptyFieldKey = "Enter the value of the non-deductible loan interest",
          wrongFormatKey = "Non-deductible loan interest must only include the numbers 0-9 and a decimal point",
          exceedsMaxAmountKey = s"The amount of ${if (userScenario.isAgent) "your client's" else "your"} non-deductible loan interest must be less than £100,000,000,000",
          underMinAmountKey = Some("Enter a valid amount for the value of the non-deductible loan interest"),
        )

        implicit val document: Document = Jsoup.parse(page(
          taxYear = taxYear,
          form = form
        ).body)

        val expectedMessages = userScenario.commonExpectedResults

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(expectedMessages.headingAndTitle, userScenario.isWelsh)
        h1Check(expectedMessages.headingAndTitle)
        textOnPageCheck(expectedMessages.p1, "main " + Selectors.p(1))
        textOnPageCheck(expectedMessages.b1, "main " + Selectors.bullet(1))
        textOnPageCheck(expectedMessages.b2, "main " + Selectors.bullet(2))
        textOnPageCheck(expectedMessages.b3, "main " + Selectors.bullet(3))
        textOnPageCheck(expectedMessages.p2, "main " + Selectors.p(3))
        buttonCheck(expectedMessages.continue)
      }
    }
  }
}
