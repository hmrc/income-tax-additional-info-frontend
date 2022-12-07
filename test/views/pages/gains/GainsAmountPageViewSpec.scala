/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.gains.GainsAmountForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.GainsAmountPageView

class GainsAmountPageViewSpec extends ViewUnitTest {

  private val page: GainsAmountPageView = inject[GainsAmountPageView]

  object Selectors {
    val paragraph1 = "#gains-amount-paragraph-1"
    val paragraph2 = "#gains-amount-paragraph-2"
    val gainsAmountNumberHint = "#gainAmountNumber-hint"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val gainsAmountErrorHref = "#gainAmountNumber"
    val summaryText = "#gains-amount-summary-text"
    val detailsText = "#gains-amount-details-text"
    val bullet1 = "#gains-amount-bullet-1"
    val bullet2 = "#gains-amount-bullet-2"
    val urlLinkText = "#gains-amount-url-text"

  }

  trait SpecificExpectedResults {
    val expectedParagraph1: String
    val expectedParagraph2: String
    val expectedErrorText: String
    val expectedLabel: String
  }

  trait CommonExpectedResults {
    val expectedHint: String
    val expectedTitle: String
    val expectedCaption: Int => String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedSummaryText: String
    val expectedDetailsText: String
    val expectedBullet1: String
    val expectedBullet2: String
    val expectedURLLinkText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHint: String = "For example, £193.54"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedTitle: String = "Chargeable event gain"
    override val expectedSummaryText: String = "When to reduce the gain"
    override val expectedDetailsText: String = "You will need to calculate a reduction if:"
    override val expectedBullet1: String = "your policy qualifies for restricted relief"
    override val expectedBullet2: String = "you were a non-UK resident while you were a beneficial owner of the policy"
    override val expectedURLLinkText: String = "Find out more about reducing chargeable event gains (opens in new tab)"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedHint: String = "Er enghraifft, £193.54"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedTitle: String = "Enillion ar ddigwyddiad trethadwy"
    override val expectedSummaryText: String = "Pryd i ostwng yr elw"
    override val expectedDetailsText: String = "Bydd angen i chi gyfrifo gostyngiad os yw’r canlynol yn wir:"
    override val expectedBullet1: String = "Mae’ch polisi yn gymwys ar gyfer rhyddhad cyfyngedig"
    override val expectedBullet2: String = "Roeddech yn berson nad oedd yn breswyl yn y DU tra roeddech yn berchennog llesol ar y polisi"
    override val expectedURLLinkText: String = "Dysgwch ragor am ostwng enillion ar ddigwyddiad trethadwy (yn agor tab newydd)"

  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedParagraph1: String = "Enter the amount shown on the chargeable event certificate provided by your insurer."
    override val expectedParagraph2: String = "If you are a joint owner of the policy or annuity, enter your share of the gain."
    override val expectedErrorText: String = "Enter the gain you made. For example, £193.54"
    override val expectedLabel: String = "How much gain did you make?"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedParagraph1: String = "Nodwch y swm a ddangosir ar y dystysgrif digwyddiad trethadwy a ddarperir gan eich yswiriwr."
    override val expectedParagraph2: String = "Os ydych yn gydberchennog ar y polisi neu’r blwydd-dal, nodwch eich cyfran o’r elw."
    override val expectedErrorText: String = "Nodwch eich ennill. Er enghraifft, £193.54"
    override val expectedLabel: String = "Faint o ennill gwnaethoch chi?"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedParagraph1: String = "Enter the amount shown on the chargeable event certificate provided by your client’s insurer."
    override val expectedParagraph2: String = "If your client is a joint owner of the policy or annuity, enter their share of the gain."
    override val expectedErrorText: String = "Enter the gain your client made. For example, £193.54"
    override val expectedLabel: String = "How much gain did your client make?"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedParagraph1: String = "Nodwch y swm a ddangosir ar y dystysgrif digwyddiad trethadwy a ddarperir gan yswiriwr neu reolwr ISA eich cleient."
    override val expectedParagraph2: String = "Os yw’ch cleient yn berchen y polisi neu’r blwydd-dal ar y cyd, nodwch ei ran o’r enillion."
    override val expectedErrorText: String = "Nodwch enillion eich cleient. Er enghraifft, £193.54"
    override val expectedLabel: String = "Faint oedd enillion eich cleient?"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render gains amount page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, GainsAmountForm.gainsAmountForm(userScenario.isAgent)).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedTitle)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.gainsAmountNumberHint)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryText, Selectors.summaryText)
        textOnPageCheck(userScenario.commonExpectedResults.expectedDetailsText, Selectors.detailsText)
        textOnPageCheck(userScenario.commonExpectedResults.expectedBullet1, Selectors.bullet1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedBullet2, Selectors.bullet2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedURLLinkText, Selectors.urlLinkText)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render gain amount page with errors if submitted form is invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, GainsAmountForm.gainsAmountForm(userScenario.isAgent).bind(Map(GainsAmountForm.GainAmount -> ""))).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedTitle)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.gainsAmountNumberHint)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryText, Selectors.summaryText)
        textOnPageCheck(userScenario.commonExpectedResults.expectedDetailsText, Selectors.detailsText)
        textOnPageCheck(userScenario.commonExpectedResults.expectedBullet1, Selectors.bullet1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedBullet2, Selectors.bullet2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedURLLinkText, Selectors.urlLinkText)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.gainsAmountErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }
}
