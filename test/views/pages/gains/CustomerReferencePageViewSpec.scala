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

import forms.gains.InputFieldForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.CustomerReferencePageView

class CustomerReferencePageViewSpec extends ViewUnitTest {

  private val page: CustomerReferencePageView = inject[CustomerReferencePageView]
  private val inputFormat = "policyNumber"

  object Selectors {
    val paragraph1 = "#p1"
    val paragraph2 = "#p2"
    val label = "#main-content > div > div > form > div > label"
    val customerReferenceNumberHint = "#value-hint"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val customerReferenceErrorHref = "#value"
  }

  trait SpecificExpectedResults {
    val expectedErrorTitle: String
    val expectedHeading: String
    val expectedParagraph1: String
    val expectedParagraph2: String
    val expectedLabel: String
    val expectedErrorText: String
    val expectedErrorText1: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedHint: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHint: String = "For example, 'P8879' or 'LA/2881/07'."
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedHint: String = "For example, 'P8879' or 'LA/2881/07'."
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedErrorTitle: String = "Error: Policy number"
    override val expectedHeading: String = "Policy number"
    override val expectedParagraph1: String = "Your insurer or ISA manager should have given you a policy number, also known as a 'customer reference', for your policy or contract."
    override val expectedParagraph2: String = "Your policy number can include numbers, letters and special characters '/' or '-'."
    override val expectedLabel: String = "What's your policy number?"
    override val expectedErrorText: String = "Enter your policy number"
    override val expectedErrorText1: String = "Enter your policy number in the correct format. For example, 'P8879' or 'LA/2881/07'."
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedErrorTitle: String = "Error: Policy number"
    override val expectedHeading: String = "Policy number"
    override val expectedParagraph1: String = "Your insurer or ISA manager should have given you a policy number, also known as a 'customer reference', for your policy or contract."
    override val expectedParagraph2: String = "Your policy number can include numbers, letters and special characters '/' or '-'."
    override val expectedLabel: String = "What's your policy number?"
    override val expectedErrorText: String = "Enter your policy number"
    override val expectedErrorText1: String = "Enter your policy number in the correct format. For example, 'P8879' or 'LA/2881/07'."
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedErrorTitle: String = "Error: Policy number"
    override val expectedHeading: String = "Policy number"
    override val expectedParagraph1: String = "Your client's insurer or ISA manager should have given them a policy number, also known as a 'customer reference', for their policy or contract."
    override val expectedParagraph2: String = "Your client's policy number can include numbers, letters and special characters '/' or '-'."
    override val expectedLabel: String = "What's your client's policy number?"
    override val expectedErrorText: String = "Enter your client's policy number"
    override val expectedErrorText1: String = "Enter your client's policy number in the correct format. For example, 'P8879' or 'LA/2881/07'."
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedErrorTitle: String = "Error: Policy number"
    override val expectedHeading: String = "Policy number"
    override val expectedParagraph1: String = "Your client's insurer or ISA manager should have given them a policy number, also known as a 'customer reference', for their policy or contract."
    override val expectedParagraph2: String = "Your client's policy number can include numbers, letters and special characters '/' or '-'."
    override val expectedLabel: String = "What's your client's policy number?"
    override val expectedErrorText: String = "Enter your client's policy number"
    override val expectedErrorText1: String = "Enter your client's policy number in the correct format. For example, 'P8879' or 'LA/2881/07'."
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render customer reference page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputFieldForm.inputFieldForm(userScenario.isAgent, inputFormat,
          s"gains.customer-reference.question.error-message.1.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.customer-reference.question.error-message.2.${if (userScenario.isAgent) "agent" else "individual"}")).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedLabel, Selectors.label)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.customerReferenceNumberHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render customer reference page with errors if submitted form is empty" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputFieldForm.inputFieldForm(userScenario.isAgent, inputFormat,
          s"gains.customer-reference.question.error-message.1.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.customer-reference.question.error-message.2.${if (userScenario.isAgent) "agent" else "individual"}")
          .bind(Map(InputFieldForm.value -> ""))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedLabel, Selectors.label)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.customerReferenceNumberHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.customerReferenceErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render customer reference page with errors if submitted form is invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputFieldForm.inputFieldForm(userScenario.isAgent, inputFormat,
          s"gains.customer-reference.question.error-message.1.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.customer-reference.question.error-message.2.${if (userScenario.isAgent) "agent" else "individual"}")
          .bind(Map(InputFieldForm.value -> "aaa"))).body)

        welshToggleCheck(userScenario.isWelsh)

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText1, Selectors.customerReferenceErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText1)
      }
    }
  }
}