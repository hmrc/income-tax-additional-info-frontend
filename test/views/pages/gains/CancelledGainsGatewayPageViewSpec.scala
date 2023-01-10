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

package views.pages.gains

import forms.YesNoForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.CancelledGainsGatewayPageView

class CancelledGainsGatewayPageViewSpec extends ViewUnitTest {

  private val page: CancelledGainsGatewayPageView = inject[CancelledGainsGatewayPageView]

  object Selectors {

    val title = "#main-content > div > div > h1"
    val yesSelector = "#value"
    val noSelector = "#value-no"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val emptySelectionError = "#main-content > div > div > div.govuk-error-summary > div > ul > li > a"
    val errorLink = "#value"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorText: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val yesText: String
    val noText: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from cancelled policies for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val yesText: String = "Yes"
    override val noText: String = "No"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau a ganslwyd ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val yesText: String = "Iawn"
    override val noText: String = "Na"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Did you make a gain from a cancelled Individual Savings Account or life policy?"
    override val expectedErrorText: String = "Select yes if you made a gain from a cancelled Individual Savings Account or life policy"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A wnaethoch ennill o Gyfrif Cynilo Unigol (ISA) neu bolisi bywyd a ganslwyd?"
    override val expectedErrorText: String = "Dewiswch ‘Iawn’ os gwnaethoch ennill o Gyfrif Cynilo Unigol (ISA) neu bolisi bywyd a ganslwyd"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Did your client make a gain from a cancelled Individual Savings Account or life policy?"
    override val expectedErrorText: String = "Select yes if your client made a gain from a cancelled Individual Savings Account or life policy"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A wnaeth eich cleient enillion ar Gyfrif Cynilo Unigol wedi’i ganslo neu ar yswiriant bywyd wedi’i ganslo?"
    override val expectedErrorText: String = "Dewiswch ‘Iawn’ os gwnaeth eich cleient enillion ar Gyfrif Cynilo Unigol wedi’i ganslo neu ar yswiriant bywyd wedi’i ganslo"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    val form = YesNoForm.yesNoForm(
      missingInputError = s"cancelled.gains.gateway.question.error.${if (userScenario.isAgent) "agent" else "individual"}"
    )
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render cancelled gains gateway page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form,taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    val form = YesNoForm.yesNoForm(
      missingInputError = s"cancelled.gains.gateway.question.error.${if (userScenario.isAgent) "agent" else "individual"}"
    )
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render cancelled gains gateway page with errors if submitted form is invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form = form.bind(Map(YesNoForm.yesNo -> "")), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.errorLink)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }
}
