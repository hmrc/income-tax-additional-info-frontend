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
import views.html.pages.gains.SectionCompletedStateView

class SectionCompletedStateViewSpec extends ViewUnitTest {

  private val page: SectionCompletedStateView = inject[SectionCompletedStateView]

  object Selectors {
    val radioHeading = "#main-content > div > div > h1 > span"
    val yesSelector = "#value"
    val noSelector = "#value-no"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val emptySelectionError = "#value-error"
    val errorLink = "#value"
    val expectedHint = "#value-hint"
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedRadioHeading: String
    val expectedErrorText: String
    val expectedCaption: Int => String
    val yesText: String
    val noText: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedHint: String
  }

  trait SpecificExpectedResults

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedTitle: String = "Have you finished this section?"
    override val expectedErrorTitle: String = "Error: Have you finished this section?"
    override val expectedRadioHeading: String = "Have you finished this section?"
    override val yesText: String = "Yes"
    override val noText: String = "No"
    override val expectedErrorText: String = "Select if you’ve completed this section"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedHint: String = "You’ll still be able to go back and review the information that you’ve given us."
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedTitle: String = "A ydych wedi gorffen yr adran hon?"
    override val expectedErrorTitle: String = "Gwall: A ydych wedi gorffen yr adran hon?"
    override val expectedRadioHeading: String = "A ydych wedi gorffen yr adran hon?"
    override val yesText: String = "Iawn"
    override val noText: String = "Na"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedErrorText: String = "Dewiswch a ydych wedi llenwi’r adran hon"
    override val expectedHint: String = "Byddwch yn dal i allu mynd yn ôl ac adolygu’r wybodaeth rydych wedi’i rhoi i ni."
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY),
  )

  userScenarios.foreach { userScenario =>
    val form = YesNoForm.yesNoForm(
      missingInputError = "gains.sectionCompletedState.error.required"
    )
    s"language is ${welshTest(userScenario.isWelsh)}" should {
      "render gains section completed state page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form, taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedRadioHeading)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    val form = YesNoForm.yesNoForm(
      missingInputError = "gains.sectionCompletedState.error.required"
    )
    s"language is ${welshTest(userScenario.isWelsh)}" should {
      "render gains section completed state with errors if submitted form is invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.bind(Map(YesNoForm.yesNo -> "")),taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedRadioHeading)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText, Selectors.errorLink)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText, userScenario.isWelsh)
      }
    }
  }
}
