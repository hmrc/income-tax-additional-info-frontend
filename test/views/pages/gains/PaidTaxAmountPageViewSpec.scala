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

import forms.AmountForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.PaidTaxAmountPageView

import java.util.UUID

class PaidTaxAmountPageViewSpec extends ViewUnitTest {

  private val page: PaidTaxAmountPageView = inject[PaidTaxAmountPageView]
  private val sessionId: String = UUID.randomUUID().toString

  object Selectors {
    val gainsAmountNumberHint = "#amount-hint"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val gainsAmountErrorHref = "#amount"
  }

  trait SpecificExpectedResults {
    val expectedNoEntryError: String
    val expectedIncorrectFormatError : String
    val expectedTitle: String
    val expectedHeading: String
    val expectedErrorTitle: String

  }

  trait CommonExpectedResults {
    val expectedHint: String
    val expectedCaption: Int => String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedAmountExceedsMaxError: String

  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHint: String = "For example, £193.54"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedAmountExceedsMaxError= "The amount of tax paid must be less than £100,000,000,000"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedHint: String = "Er enghraifft, £193.54"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedAmountExceedsMaxError= "Mae’n rhaid i swm y dreth a dalwyd fod yn llai na £100,000,000,000"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedNoEntryError: String = "Enter the amount of tax you paid"
    override val expectedIncorrectFormatError = "Enter the amount of tax you paid in the correct format. For example, £193.54"
    override val expectedTitle: String = "How much tax did you pay on your gain?"
    override val expectedHeading: String = "How much tax did you pay on your gain?"
    override val expectedErrorTitle: String = "Error: How much tax did you pay on your gain?"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedNoEntryError: String = "Nodwch swm y dreth a daloch"
    override val expectedIncorrectFormatError = "Nodwch swm y dreth a daloch yn y fformat cywir. Er enghraifft, £193.54"
    override val expectedTitle: String = "Faint o dreth a daloch ar eich enillion?"
    override val expectedHeading: String = "Faint o dreth a daloch ar eich enillion?"
    override val expectedErrorTitle: String = "Gwall: Faint o dreth a daloch ar eich enillion?"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedNoEntryError: String = "Enter the amount of tax your client paid"
    override val expectedIncorrectFormatError = "Enter the amount of tax your client paid in the correct format. For example, £193.54"
    override val expectedTitle: String = "How much tax did your client pay on their gain?"
    override val expectedHeading: String = "How much tax did your client pay on their gain?"
    override val expectedErrorTitle: String = "Error: How much tax did your client pay on their gain?"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedNoEntryError: String = "Nodwch swm y dreth a dalodd eich cleient"
    override val expectedIncorrectFormatError = "Nodwch swm y dreth a dalodd eich cleient yn y fformat cywir. Er enghraifft, £193.54"
    override val expectedTitle: String = "Faint o dreth a dalodd eich cleient ar ei enillion?"
    override val expectedHeading: String = "Faint o dreth a dalodd eich cleient ar ei enillion?"
    override val expectedErrorTitle: String = "Gwall: Faint o dreth a dalodd eich cleient ar ei enillion?"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax amount amount page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, AmountForm.amountForm(
          s"gains.paid-tax-amount.question.error.empty_field.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-amount.question.incorrect-format-error.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-amount.question.amount-exceeds-max-error.${if (userScenario.isAgent) "agent" else "individual"}",
          None,

        ), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.gainsAmountNumberHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax amount page with errors if submitted form is empty" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, AmountForm.amountForm(
          s"gains.paid-tax-amount.question.error.empty_field.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-amount.question.incorrect-format-error.${if (userScenario.isAgent) "agent" else "individual"}",
          "gains.paid-tax-amount.question.amount-exceeds-max-error.",
          None
        ).bind(Map(AmountForm.amount -> "")), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.gainsAmountNumberHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedNoEntryError, Selectors.gainsAmountErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedNoEntryError, userScenario.isWelsh)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax amount page with errors if submitted form is invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, AmountForm.amountForm(
          s"gains.paid-tax-amount.question.error.empty_field.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-amount.question.incorrect-format-error.${if (userScenario.isAgent) "agent" else "individual"}",
          "gains.paid-tax-amount.question.amount-exceeds-max-error",
          None
        ).bind(Map(AmountForm.amount -> "abc")), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.gainsAmountNumberHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedIncorrectFormatError, Selectors.gainsAmountErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedIncorrectFormatError, userScenario.isWelsh)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render paid tax amount page with errors if submitted form value is too high" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, AmountForm.amountForm(
          s"gains.paid-tax-amount.question.error.empty_field.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.paid-tax-amount.question.incorrect-format-error.${if (userScenario.isAgent) "agent" else "individual"}",
          "gains.paid-tax-amount.question.amount-exceeds-max-error",
          None
        ).bind(Map(AmountForm.amount -> "10000000000000")), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.gainsAmountNumberHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedAmountExceedsMaxError, Selectors.gainsAmountErrorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedAmountExceedsMaxError, userScenario.isWelsh)
      }

    }
  }

}