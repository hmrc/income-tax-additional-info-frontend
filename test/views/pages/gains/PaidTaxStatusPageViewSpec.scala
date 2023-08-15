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
import views.html.pages.gains.PaidTaxStatusPageView

import java.util.UUID

class PaidTaxStatusPageViewSpec extends ViewUnitTest {

  private val page: PaidTaxStatusPageView = inject[PaidTaxStatusPageView]
  private val sessionId: String = UUID.randomUUID().toString


  object Selectors {
    val hint = "#value-hint"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val yesSelector = "#value"
    val noSelector = "#value-no"
    val causedErrorHref = "#value"
    val causedErrorInputHref = "#amount"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedHeading: String
    val expectedHint: String
    val expectedErrorText1: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedYesText: String
    val expectedNoText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Was your gain treated as tax paid?"
    override val expectedErrorTitle: String = "Error: Was your gain treated as tax paid?"
    override val expectedHeading: String = "Was your gain treated as tax paid?"
    override val expectedHint: String = "Select Yes if tax was deducted from your gain. This will be shown on your chargeable event certificate."
    override val expectedErrorText1: String = "Select Yes if tax was deducted from your gain"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A gafodd eich enillion eu trin fel treth a dalwyd?"
    override val expectedErrorTitle: String = "Gwall: A gafodd eich enillion eu trin fel treth a dalwyd?"
    override val expectedHeading: String = "A gafodd eich enillion eu trin fel treth a dalwyd?"
    override val expectedHint: String = "Dewiswch ‘Iawn’ os cafodd treth ei didynnu oddi wrth eich enillion. Bydd hyn i’w weld ar eich tystysgrif digwyddiad trethadwy."
    override val expectedErrorText1: String = "Dewiswch ‘Iawn’ os cafodd treth ei didynnu oddi wrth eich enillion"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Was your client's gain treated as tax paid?"
    override val expectedErrorTitle: String = "Error: Was your client's gain treated as tax paid?"
    override val expectedHeading: String = "Was your client's gain treated as tax paid?"
    override val expectedHint: String = "Select Yes if tax was deducted from your client's gain. This will be shown on your client's chargeable event certificate."
    override val expectedErrorText1: String = "Select Yes if tax was deducted from your client's gain"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A gafodd enillion eich cleient eu trin fel treth a dalwyd?"
    override val expectedErrorTitle: String = "Gwall: A gafodd enillion eich cleient eu trin fel treth a dalwyd?"
    override val expectedHeading: String = "A gafodd enillion eich cleient eu trin fel treth a dalwyd?"
    override val expectedHint: String = "Dewiswch ‘Iawn’ os cafodd treth ei didynnu oddi wrth enillion eich cleient. Bydd hyn i’w weld ar dystysgrif digwyddiad trethadwy eich cleient."
    override val expectedErrorText1: String = "Dewiswch ‘Iawn’ os cafodd treth ei didynnu oddi wrth enillion eich cleient"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax status page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, YesNoForm.yesNoForm(
          s"gains.paid-tax-status.question.error.1.${if (userScenario.isAgent) "agent" else "individual"}"), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHint, Selectors.hint)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, 2, checked = false)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax status page with errors if submitted form has invalid radio selection" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, YesNoForm.yesNoForm(
          s"gains.paid-tax-status.question.error.1.${if (userScenario.isAgent) "agent" else "individual"}").bind(Map(
          YesNoForm.yesNo -> ""
        )), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHint, Selectors.hint)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, 2, checked = false)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText1, Selectors.causedErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText1, userScenario.isWelsh)
      }
    }
  }
}