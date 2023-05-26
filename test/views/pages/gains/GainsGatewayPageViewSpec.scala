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
import views.html.pages.gains.GainsGatewayPageView

class GainsGatewayPageViewSpec extends ViewUnitTest {

  private val page: GainsGatewayPageView = inject[GainsGatewayPageView]

  object Selectors {
    val paragraph1 = "#gains-gateway-paragraph-1"
    val paragraph2 = "#gains-gateway-paragraph-2"
    val bullet1 = "#main-content > div > div > ul > li:nth-child(1)"
    val bullet2 = "#main-content > div > div > ul > li:nth-child(2)"
    val radioHeading = "#main-content > div > div > form > div > fieldset > legend"
    val yesSelector = "#value"
    val noSelector = "#value-no"
    val continueButton = "#continue"
    val findOutMoreLink = "#gains-gateway-link-1"
    val getHelpLink = "#help"
    val emptySelectionError = "#main-content > div > div > div.govuk-error-summary > div > div > ul > li > a"
    val errorLink = "#value"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedHeading: String
    val expectedRadioHeading: String
    val expectedParagraph1: String
    val expectedBullet1: String
    val expectedBullet2: String
    val expectedErrorText: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val yesText: String
    val noText: String
    val expectedParagraph2: String
    val expectedLink: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val yesText: String = "Yes"
    override val noText: String = "No"
    override val expectedParagraph2: String = "You will need:"
    override val expectedLink: String = "Find out more about Gains from life insurance policies and contracts (opens in new tab)"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val yesText: String = "Iawn"
    override val noText: String = "Na"
    override val expectedParagraph2: String = "Bydd angen y canlynol arnoch:"
    override val expectedLink: String = "Dysgwch ragor am ‘Enillion o bolisïau yswiriant bywyd a chontractau’ (yn agor tab newydd)"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Gains from life insurance policies and contracts"
    override val expectedErrorTitle: String = "Error: Gains from life insurance policies and contracts"
    override val expectedRadioHeading: String = "Did you make a gain on a UK policy or contract?"
    override val expectedHeading: String = "Gains from life insurance policies and contracts"
    override val expectedParagraph1: String = "We will ask about gains you have made on life insurance policies, life annuities and capital redemption policies"
    override val expectedBullet1: String = "your policy number, given to you by your insurer or ISA manager"
    override val expectedBullet2: String = "the chargeable event certificate, sent to you by your insurer"
    override val expectedErrorText: String = "Select Yes if you made a gain on a UK policy or contract"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Enillion o bolisïau yswiriant bywyd a chontractau"
    override val expectedErrorTitle: String = "Gwall: Enillion o bolisïau yswiriant bywyd a chontractau"
    override val expectedRadioHeading: String = "A wnaethoch chi enillion ar bolisi neu gontract yn y DU?"
    override val expectedHeading: String = "Enillion o bolisïau yswiriant bywyd a chontractau"
    override val expectedParagraph1: String = "Byddwn yn gofyn am enillion a wnaethoch ar bolisïau yswiriant bywyd, blwydd-dal bywyd a pholisïau adbrynu cyfalaf"
    override val expectedBullet1: String = "rhif eich polisi, a roddodd eich yswiriwr neu reolwr eich ISA i chi"
    override val expectedBullet2: String = "tystysgrif y digwyddiad trethadwy, a anfonodd eich yswiriwr atoch"
    override val expectedErrorText: String = "Dewiswch ‘Iawn’ os gwnaethoch enillion ar bolisi neu gontract yn y DU"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Gains from life insurance policies and contracts"
    override val expectedErrorTitle: String = "Error: Gains from life insurance policies and contracts"
    override val expectedRadioHeading: String = "Did your client make a gain on a UK policy or contract?"
    override val expectedHeading: String = "Gains from life insurance policies and contracts"
    override val expectedParagraph1: String = "We will ask about gains your client has made on life insurance policies, life annuities and capital redemption policies"
    override val expectedBullet1: String = "your client's policy number, given to you by their insurer or ISA manager"
    override val expectedBullet2: String = "the chargeable event certificate, sent to your client by their insurer"
    override val expectedErrorText: String = "Select yes if your client made a gain on a UK policy or contract"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Enillion o bolisïau yswiriant bywyd a chontractau"
    override val expectedErrorTitle: String = "Gwall: Enillion o bolisïau yswiriant bywyd a chontractau"
    override val expectedRadioHeading: String = "A wnaeth eich cleient enillion ar bolisi neu gontract yn y DU?"
    override val expectedHeading: String = "Enillion o bolisïau yswiriant bywyd a chontractau"
    override val expectedParagraph1: String = "Byddwn yn gofyn am enillion a wnaeth eich cleient ar bolisïau yswiriant bywyd, blwydd-dal bywyd a pholisïau adbrynu cyfalaf"
    override val expectedBullet1: String = "rhif polisi eich cleient, a roddodd ei yswiriwr neu reolwr ei ISA i chi"
    override val expectedBullet2: String = "tystysgrif y digwyddiad trethadwy, a anfonodd yswiriwr eich cleient ato"
    override val expectedErrorText: String = "Dewiswch ‘Iawn’ os gwnaeth eich cleient enillion ar bolisi neu gontract yn y DU"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    val form = YesNoForm.yesNoForm(
      missingInputError = s"gains.gateway.question.error.${if (userScenario.isAgent) "agent" else "individual"}"
    )
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render gains gateway page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, form).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedRadioHeading, Selectors.radioHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedBullet1, Selectors.bullet1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedBullet2, Selectors.bullet2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedLink, Selectors.findOutMoreLink)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    val form = YesNoForm.yesNoForm(
      missingInputError = s"gains.gateway.question.error.${if (userScenario.isAgent) "agent" else "individual"}"
    )
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render gains gateway page with errors if submitted form is invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, form = form.bind(Map(YesNoForm.yesNo -> ""))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedRadioHeading, Selectors.radioHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedBullet1, Selectors.bullet1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedBullet2, Selectors.bullet2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedLink, Selectors.findOutMoreLink)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.emptySelectionError)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.errorLink)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }
}
