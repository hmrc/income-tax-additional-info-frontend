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
    val paragraph1 = "#main-content > div > div > p.govuk-body.govuk-p1"
    val paragraph2 = "#main-content > div > div > p.govuk-body.govuk-p2"
    val bullet1 = "#main-content > div > div > ul > li:nth-child(1)"
    val bullet2 = "#main-content > div > div > ul > li:nth-child(2)"
    val bullet3 = "#main-content > div > div > ul > li:nth-child(3)"
    val radioHeading = "#main-content > div > div > form > div > fieldset > legend"
    val yesSelector = "#value"
    val noSelector = "#value-no"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val emptySelectionError = "#main-content > div > div > div.govuk-error-summary > div > ul > li > a"
    val errorLink = "#value"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedHeading: String
    val expectedRadioHeading: String
    val expectedParagraph1: String
    val expectedParagraph2: String
    val expectedErrorText: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val bullet1: String
    val bullet2: String
    val bullet3: String
    val yesText: String
    val noText: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val bullet1: String = "life insurance policy"
    override val bullet2: String = "life annuity"
    override val bullet3: String = "capital redemption policy"
    override val yesText: String = "Yes"
    override val noText: String = "No"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val bullet1: String = "polisi yswiriant bywyd"
    override val bullet2: String = "blwydd-dal bywyd"
    override val bullet3: String = "polisi adbryniant cyfalaf"
    override val yesText: String = "Iawn"
    override val noText: String = "Na"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Gains from policies and contracts"
    override val expectedErrorTitle: String = "Error: Gains from policies and contracts"
    override val expectedRadioHeading: String = "Did you make a gain on a UK policy or contract?"
    override val expectedHeading: String = "Gains from policies and contracts"
    override val expectedParagraph1: String = "Your insurer will have sent you a chargeable event certificate if you made a gain on a:"
    override val expectedParagraph2: String = "You can tell us if your life insurance company or ISA manager have cancelled your ISA or life policy later."
    override val expectedErrorText: String = "Select Yes if you made a gain on a UK policy or contract"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Enillion o bolisïau a chontractau"
    override val expectedErrorTitle: String = "Error: Enillion o bolisïau a chontractau"
    override val expectedRadioHeading: String = "A wnaethoch chi ennill ar bolisi neu gytundeb y DU?"
    override val expectedHeading: String = "Enillion o bolisïau a chontractau"
    override val expectedParagraph1: String = "Bydd eich yswiriwr wedi anfon tystysgrif digwyddiad trethadwy, os gwnaethoch ennill ar un o’r canlynol:"
    override val expectedParagraph2: String = "Gallwch roi gwybod i ni os yw’ch cwmni yswiriant bywyd neu reolwr ISA wedi canslo’ch ISA neu bolisi bywyd nes ymlaen."
    override val expectedErrorText: String = "Dewiswch ‘Iawn’ os gwnaethoch enillion ar bolisi neu gontract yn y DU"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Gains from policies and contracts"
    override val expectedErrorTitle: String = "Error: Gains from policies and contracts"
    override val expectedRadioHeading: String = "Did your client make a gain on a UK policy or contract?"
    override val expectedHeading: String = "Gains from policies and contracts"
    override val expectedParagraph1: String = "Your client's insurer will have sent them a chargeable event certificate if they made a gain on a:"
    override val expectedParagraph2: String = "You can tell us if your client's life insurance company or ISA manager has cancelled their ISA or life policy later."
    override val expectedErrorText: String = "Select yes if your client made a gain on a UK policy or contract"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Enillion o bolisïau a chontractau"
    override val expectedErrorTitle: String = "Error: Enillion o bolisïau a chontractau"
    override val expectedRadioHeading: String = "A wnaeth eich cleient enillion ar bolisi neu gontract yn y DU?"
    override val expectedHeading: String = "Enillion o bolisïau a chontractau"
    override val expectedParagraph1: String = "Bydd yswiriwr eich cleient wedi anfon tystysgrif digwyddiad trethadwy ato, os gwnaeth enillion ar un o’r canlynol:"
    override val expectedParagraph2: String = "Gallwch roi gwybod i ni os yw cwmni yswiriant bywyd eich cleient, neu reolwr ISA eich cleient, wedi canslo ei ISA neu bolisi bywyd nes ymlaen."
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

        implicit val document: Document = Jsoup.parse(page(form,taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedRadioHeading, Selectors.radioHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.commonExpectedResults.bullet1, Selectors.bullet1)
        textOnPageCheck(userScenario.commonExpectedResults.bullet2, Selectors.bullet2)
        textOnPageCheck(userScenario.commonExpectedResults.bullet3, Selectors.bullet3)
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

        implicit val document: Document = Jsoup.parse(page(form = form.bind(Map(YesNoForm.yesNo -> "")), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedRadioHeading, Selectors.radioHeading)
        textOnPageCheck(userScenario.commonExpectedResults.bullet1, Selectors.bullet1)
        textOnPageCheck(userScenario.commonExpectedResults.bullet2, Selectors.bullet2)
        textOnPageCheck(userScenario.commonExpectedResults.bullet3, Selectors.bullet3)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.emptySelectionError)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.errorLink)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }
}
