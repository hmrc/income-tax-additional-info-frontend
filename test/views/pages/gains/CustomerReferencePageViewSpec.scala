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

  object Selectors {
    val paragraph = "#main-content > div > div > p"
    val customerReferenceNumberHint = "#value-hint"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val customerReferenceErrorHref = "#value"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedHeading: String
    val expectedParagraph: String
    val expectedErrorText: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedHint: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHint: String = "For example, 'INPOLY123A’."
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedHint: String = "Er enghraifft, 'INPOLY123A’."
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "What's your customer reference?"
    override val expectedErrorTitle: String = "Error: What's your customer reference?"
    override val expectedHeading: String = "What's your customer reference?"
    override val expectedParagraph: String = "Your insurer or ISA manager should have given you a customer reference for your policy or contract."
    override val expectedErrorText: String = "Enter your customer reference"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Beth yw’ch cyfeirnod cwsmer?"
    override val expectedErrorTitle: String = "Error: Beth yw’ch cyfeirnod cwsmer?"
    override val expectedHeading: String = "Beth yw’ch cyfeirnod cwsmer?"
    override val expectedParagraph: String = "Dylai’ch yswiriwr neu reolwr ISA fod wedi rhoi cyfeirnod cwsmer i chi ar gyfer eich ISA neu’ch polisi bywyd a ganslwyd."
    override val expectedErrorText: String = "Nodwch eich cyfeirnod cwsmer"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "What's your client's customer reference?"
    override val expectedErrorTitle: String = "Error: What's your client's customer reference?"
    override val expectedHeading: String = "What's your client's customer reference?"
    override val expectedParagraph: String = "Your client's insurer or ISA manager should have given them a customer reference for their policy or contract."
    override val expectedErrorText: String = "Enter your client's customer reference"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Beth yw cyfeirnod cwsmer eich cleient?"
    override val expectedErrorTitle: String = "Error: Beth yw cyfeirnod cwsmer eich cleient?"
    override val expectedHeading: String = "Beth yw cyfeirnod cwsmer eich cleient?"
    override val expectedParagraph: String = "Dylai yswiriwr neu reolwr ISA eich cleient fod wedi rhoi cyfeirnod cwsmer iddo ar gyfer ei bolisi neu ei gontract."
    override val expectedErrorText: String = "Nodwch gyfeirnod cwsmer eich cleient"
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

        implicit val document: Document = Jsoup.parse(page(taxYear, InputFieldForm.inputFieldForm(userScenario.isAgent,
          s"gains.customer-reference.question.error-message.${if (userScenario.isAgent) "agent" else "individual"}")).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph, Selectors.paragraph)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.customerReferenceNumberHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render customer reference page with errors if submitted form is invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputFieldForm.inputFieldForm(userScenario.isAgent,
          s"gains.customer-reference.question.error-message.${if (userScenario.isAgent) "agent" else "individual"}").bind(Map(InputFieldForm.value -> ""))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph, Selectors.paragraph)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.customerReferenceNumberHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.customerReferenceErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }
}
