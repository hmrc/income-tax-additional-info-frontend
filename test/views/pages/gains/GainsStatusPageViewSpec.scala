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
import forms.RadioButtonYearForm.{yearInput, yesNo}
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.GainsStatusPageView

class GainsStatusPageViewSpec extends ViewUnitTest {

  private val page: GainsStatusPageView = inject[GainsStatusPageView]

  object Selectors {
    val yesSelector = "#value"
    val noSelector = "#value-no"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val errorSummary = "#value"
    val radioError = "#value-error"
    val inputLabel = "#conditional-value > div > label"
    val inputError = "#last-gain"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedRadioHeading: String
    val expectedErrorText1: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val yesText: String
    val noText: String
    val expectedButtonText: String
    val expectedHelpLinkText: String

  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val yesText: String = "Yes"
    override val noText: String = "No"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val yesText: String = "Iawn"
    override val noText: String = "Na"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Has your policy paid you a gain in an earlier tax year?"
    override val expectedErrorTitle: String = "Error: Has your policy paid you a gain in an earlier tax year?"
    override val expectedRadioHeading: String = "Has your policy paid you a gain in an earlier tax year?"
    override val expectedErrorText1: String = "Select Yes if your policy paid you a gain in an earlier tax year"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Has your policy paid you a gain in an earlier tax year?"
    override val expectedErrorTitle: String = "Error: Has your policy paid you a gain in an earlier tax year?"
    override val expectedRadioHeading: String = "Has your policy paid you a gain in an earlier tax year?"
    override val expectedErrorText1: String = "Select Yes if your policy paid you a gain in an earlier tax year"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Has your client's policy paid them a gain in an earlier tax year?"
    override val expectedErrorTitle: String = "Error: Has your client's policy paid them a gain in an earlier tax year?"
    override val expectedRadioHeading: String = "Has your client's policy paid them a gain in an earlier tax year?"
    override val expectedErrorText1: String = "Select Yes if your client's policy paid them a gain in an earlier tax year"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Has your client's policy paid them a gain in an earlier tax year?"
    override val expectedErrorTitle: String = "Error: Has your client's policy paid them a gain in an earlier tax year?"
    override val expectedRadioHeading: String = "Has your client's policy paid them a gain in an earlier tax year?"
    override val expectedErrorText1: String = "Select Yes if your client's policy paid them a gain in an earlier tax year"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    val form = YesNoForm.yesNoForm(
      s"gains.status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}"
    )
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render gains status page with empty form and no value selected" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form, taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }

      "render gains status page with filled in form using selected 'yes' value and year as '20'" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.fill(true), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = true)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }

      "render gains status page with filled in form using selected 'no' value" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.fill(false), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = true)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    val form = YesNoForm.yesNoForm(
      s"gains.status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}"
    )

    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render gains status page with error if empty form is submitted and invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.bind(Map(yesNo -> "", yearInput -> "None")), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText1, Selectors.errorSummary)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText1)
      }
    }
  }
}