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

import forms.RadioButtonAmountForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.PaidTaxStatusPageView

class PaidTaxStatusPageViewSpec extends ViewUnitTest {

  private val page: PaidTaxStatusPageView = inject[PaidTaxStatusPageView]

  private val exceedsMaxLimit = "1000000000000"

  object Selectors {
    val label = "#conditional-value > div > label"
    val hint = "#amount-hint"
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
    val expectedLabel: String
    val expectedErrorText1: String
    val expectedErrorText2: String
    val expectedErrorText3: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedHint: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedYesText: String
    val expectedNoText: String
    val expectedErrorText4: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHint: String = "For example, £193.54"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
    override val expectedErrorText4: String = "The amount of tax paid must be less than £100,000,000,000"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedHint: String = "Er enghraifft, £193.54"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
    override val expectedErrorText4: String = "Mae’n rhaid i swm y dreth a dalwyd fod yn llai na £100,000,000,000"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Did you pay tax on your gain?"
    override val expectedErrorTitle: String = "Error: Did you pay tax on your gain?"
    override val expectedHeading: String = "Did you pay tax on your gain?"
    override val expectedLabel: String = "How much tax did you pay?"
    override val expectedErrorText1: String = "Select yes if you paid tax on your gain"
    override val expectedErrorText2: String = "Enter the amount of tax you paid. For example, £193.54"
    override val expectedErrorText3: String = "Enter the amount of tax you paid in the correct format. For example, £193.54"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A wnaethoch dalu treth ar eich ennill?"
    override val expectedErrorTitle: String = "Error: A wnaethoch dalu treth ar eich ennill?"
    override val expectedHeading: String = "A wnaethoch dalu treth ar eich ennill?"
    override val expectedLabel: String = "Faint o dreth wnaethoch ei dalu?"
    override val expectedErrorText1: String = "Dewiswch ‘Iawn’ os oes gennych hawl i ryddhad am ddiffyg"
    override val expectedErrorText2: String = "Nodwch swm y dreth a daloch. Er enghraifft, £193.54"
    override val expectedErrorText3: String = "Nodwch swm y dreth a daloch yn y fformat cywir. Er enghraifft, £193.54"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Did your client pay tax on their gain?"
    override val expectedErrorTitle: String = "Error: Did your client pay tax on their gain?"
    override val expectedHeading: String = "Did your client pay tax on their gain?"
    override val expectedLabel: String = "How much tax did your client pay?"
    override val expectedErrorText1: String = "Select yes if your client paid tax on their gain"
    override val expectedErrorText2: String = "Enter the amount of tax your client paid. For example, £193.54"
    override val expectedErrorText3: String = "Enter the amount of tax your client paid in the correct format. For example, £193.54"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A wnaeth eich cleient dalu treth ar ei enillion?"
    override val expectedErrorTitle: String = "Error: A wnaeth eich cleient dalu treth ar ei enillion?"
    override val expectedHeading: String = "A wnaeth eich cleient dalu treth ar ei enillion?"
    override val expectedLabel: String = "Faint o dreth a dalwyd gan eich cleient?"
    override val expectedErrorText1: String = "Dewiswch ‘Iawn’ os gwnaeth eich cleient dalu treth ar ei enillion"
    override val expectedErrorText2: String = "Nodwch swm y dreth a dalodd eich cleient. Er enghraifft, £193.54"
    override val expectedErrorText3: String = "Nodwch swm y dreth a dalodd eich cleient yn y fformat cywir. Er enghraifft, £193.54"
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

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.paid-tax-status.question.error.1.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.2.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.3.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.4",
          s"")).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedLabel, Selectors.label)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.hint)
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

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.paid-tax-status.question.error.1.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.2.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.3.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.4",
          s"").bind(Map(
          RadioButtonAmountForm.amount -> "",
          RadioButtonAmountForm.yesNo -> ""
        ))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedLabel, Selectors.label)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.hint)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, 2, checked = false)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText1, Selectors.causedErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText1)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax status page with errors when input field empty" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.paid-tax-status.question.error.1.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.2.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.3.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.4",
          s"").bind(Map(
          RadioButtonAmountForm.amount -> "",
          RadioButtonAmountForm.yesNo -> "true"
        ))).body)

        welshToggleCheck(userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, 1, checked = true)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, 2, checked = false)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText2, Selectors.causedErrorInputHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText2)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax status page with errors when input has invalid format" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.paid-tax-status.question.error.1.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.2.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.3.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.4",
          s"").bind(Map(
          RadioButtonAmountForm.amount -> "f",
          RadioButtonAmountForm.yesNo -> "true"
        ))).body)

        welshToggleCheck(userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, 1, checked = true)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, 2, checked = false)
        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText3, Selectors.causedErrorInputHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText3)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax status page with errors when input exceeds max value" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.paid-tax-status.question.error.1.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.2.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.3.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-status.question.error.4",
          s"").bind(Map(
          RadioButtonAmountForm.amount -> exceedsMaxLimit,
          RadioButtonAmountForm.yesNo -> "true"
        ))).body)

        welshToggleCheck(userScenario.isWelsh)
        radioButtonCheck(userScenario.commonExpectedResults.expectedYesText, 1, checked = true)
        radioButtonCheck(userScenario.commonExpectedResults.expectedNoText, 2, checked = false)
        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText4, Selectors.causedErrorInputHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText4)
      }
    }
  }
}
