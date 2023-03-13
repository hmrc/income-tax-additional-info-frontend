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

import forms.gains.RadioButtonPolicyEventForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.PolicyEventPageView

class PolicyEventPageViewSpec extends ViewUnitTest {

  private val page: PolicyEventPageView = inject[PolicyEventPageView]

  object Selectors {
    val legendText = "#main-content > div > div > form > div > fieldset > legend"
    val subTitle = "#main-content > div > div > p"

    val radioItemOne = "#main-content > div > div > form > div > fieldset > div > div:nth-child(1) > label"
    val radioItemTwo = "#main-content > div > div > form > div > fieldset > div > div:nth-child(2) > label"
    val radioItemThree = "#main-content > div > div > form > div > fieldset > div > div:nth-child(3) > label"
    val radioItemFour = "#main-content > div > div > form > div > fieldset > div > div:nth-child(4) > label"
    val radioItemFive = "#main-content > div > div > form > div > fieldset > div > div:nth-child(5) > label"

    val helpTitle = "#main-content > div > div > details > summary > span"
    val helpParagraph = "#main-content > div > div > details > div > p"
    val helpListItemOne = "#main-content > div > div > details > div > ul > li:nth-child(1)"
    val helpListItemTwo = "#main-content > div > div > details > div > ul > li:nth-child(2)"
    val helpListItemThree = "#main-content > div > div > details > div > ul > li:nth-child(3)"
    val helpListItemFour = "#main-content > div > div > details > div > ul > li:nth-child(4)"

    val continueButton = "#continue"
    val getHelpLink = "#help"
    val errorHref = "#policy-event"
    val errorHrefWrongFormat = "#other-text"
    val idErrorAboveElement = "policy-event-error"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedWrongFormatErrorTitleText: String
    val expectedHeading: String
    val expectedErrorText: String
    val expectedWrongFormatErrorText: String

  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedLegendText: String
    val expectedRadioItemOne: String
    val expectedRadioItemTwo: String
    val expectedRadioItemThree: String
    val expectedRadioItemFour: String
    val expectedRadioItemFive: String
    val expectedButtonText: String
    val expectedSubTitle: String
    val expectedHelpLinkText: String
    val expectedHelpDropdownTitle: String
    val expectedHelpContentParagraph: String
    val expectedHelpContentBulletOne: String
    val expectedHelpContentBulletTwo: String
    val expectedHelpContentBulletThree: String
    val expectedHelpContentBulletFour: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedSubTitle: String = "What event caused this gain?"
    override val expectedLegendText: String = "Select one option."
    override val expectedHelpDropdownTitle: String = "More about gains events"
    override val expectedRadioItemOne: String = "Full or part surrender"
    override val expectedRadioItemTwo: String = "Policy matured or a death"
    override val expectedRadioItemThree: String = "Sale or assignment of a policy"
    override val expectedRadioItemFour: String = "Personal Portfolio Bond"
    override val expectedRadioItemFive: String = "Other"
    override val expectedHelpContentParagraph: String = "The most common causes of gains are if:"
    override val expectedHelpContentBulletOne: String = "cash or other benefits were received on a full or part surrender of a policy"
    override val expectedHelpContentBulletTwo: String = "a policy matured or ended by the death of the life insured"
    override val expectedHelpContentBulletThree: String = "there was a sale of assignment of a UK policy, or part of a policy, for value"
    override val expectedHelpContentBulletFour: String = "the policy was a Personal Portfolio Bond, even if the insurer had not paid cash or other benefits during the year"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedSubTitle: String = "What event caused this gain?"
    override val expectedLegendText: String = "Select one option."
    override val expectedHelpDropdownTitle: String = "More about gains events"
    override val expectedRadioItemOne: String = "Full or part surrender"
    override val expectedRadioItemTwo: String = "Policy matured or a death"
    override val expectedRadioItemThree: String = "Sale or assignment of a policy"
    override val expectedRadioItemFour: String = "Personal Portfolio Bond"
    override val expectedRadioItemFive: String = "Other"
    override val expectedHelpContentParagraph: String = "The most common causes of gains are if:"
    override val expectedHelpContentBulletOne: String = "os cafwyd arian parod neu fudd-daliadau eraill wrth ildio polisi’n llawn neu’n rhannol"
    override val expectedHelpContentBulletTwo: String = "gwnaeth polisi aeddfedu neu ddod i ben oherwydd marwolaeth y bywyd a yswiriwyd"
    override val expectedHelpContentBulletThree: String = "gwerthwyd neu neilltuwyd polisi yn y DU, neu ran o bolisi, ar gyfer gwerth"
    override val expectedHelpContentBulletFour: String = "roedd y polisi yn Bondiau Portffolio Personol, hyd yn oed os nad oedd yr yswiriwr wedi talu arian parod na budd-daliadau eraill yn ystod y flwyddyn"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Policy event"
    override val expectedErrorTitle: String = "Error: Policy event"
    override val expectedHeading: String = "Policy event"
    override val expectedErrorText: String = "Enter the cause of the gain."
    override val expectedWrongFormatErrorTitleText: String = "Error: Enter the cause of the gain in the correct format. For example: Sale of policy"
    override val expectedWrongFormatErrorText: String = "Enter the cause of the gain in the correct format. For example: Sale of policy"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Digwyddiad polisi"
    override val expectedErrorTitle: String = "Error: Digwyddiad polisi"
    override val expectedHeading: String = "Digwyddiad polisi"
    override val expectedErrorText: String = "Enter the cause of the gain."
    override val expectedWrongFormatErrorTitleText: String = "Error: Enter the cause of the gain in the correct format. For example: Sale of policy"
    override val expectedWrongFormatErrorText: String = "Enter the cause of the gain in the correct format. For example: Sale of policy"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Policy event"
    override val expectedErrorTitle: String = "Error: Policy event"
    override val expectedHeading: String = "Policy event"
    override val expectedErrorText: String = "Enter the cause of the gain."
    override val expectedWrongFormatErrorTitleText: String = "Error: Enter the cause of the gain in the correct format. For example: Sale of policy"
    override val expectedWrongFormatErrorText: String = "Enter the cause of the gain in the correct format. For example: Sale of policy"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Digwyddiad polisi"
    override val expectedErrorTitle: String = "Error: Digwyddiad polisi"
    override val expectedHeading: String = "Digwyddiad polisi"
    override val expectedErrorText: String = "Enter the cause of the gain."
    override val expectedWrongFormatErrorTitleText: String = "Error: Enter the cause of the gain in the correct format. For example: Sale of policy"
    override val expectedWrongFormatErrorText: String = "Enter the cause of the gain in the correct format. For example: Sale of policy"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy event page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear,
          RadioButtonPolicyEventForm.radioButtonCustomOptionForm(
            messages(s"gains.policy-event.question.error-message"),
            messages(s"gains.policy-event.question.error-message"),
            messages(s"gains.policy-event.question.incorrect-format.error-message"))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSubTitle, Selectors.subTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedLegendText, Selectors.legendText)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemOne, Selectors.radioItemOne)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemTwo, Selectors.radioItemTwo)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemThree, Selectors.radioItemThree)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemFour, Selectors.radioItemFour)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemFive, Selectors.radioItemFive)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpDropdownTitle, Selectors.helpTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentParagraph, Selectors.helpParagraph)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentBulletOne, Selectors.helpListItemOne)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentBulletTwo, Selectors.helpListItemTwo)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentBulletThree, Selectors.helpListItemThree)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentBulletFour, Selectors.helpListItemFour)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy event page with errors if submitted form is empty" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear,
          RadioButtonPolicyEventForm.radioButtonCustomOptionForm(
            messages(s"gains.policy-event.question.error-message"),
            messages(s"gains.policy-event.question.error-message"),
            messages(s"gains.policy-event.question.incorrect-format.error-message")).bind(
            Map(RadioButtonPolicyEventForm.selectedOption -> "")
          ).withError("", "")
        ).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSubTitle, Selectors.subTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedLegendText, Selectors.legendText)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemOne, Selectors.radioItemOne)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemTwo, Selectors.radioItemTwo)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemThree, Selectors.radioItemThree)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemFour, Selectors.radioItemFour)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemFive, Selectors.radioItemFive)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpDropdownTitle, Selectors.helpTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentParagraph, Selectors.helpParagraph)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentBulletOne, Selectors.helpListItemOne)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentBulletTwo, Selectors.helpListItemTwo)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentBulletThree, Selectors.helpListItemThree)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentBulletFour, Selectors.helpListItemFour)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.errorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy event page with errors if submitted form has the wrong format" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear,
          RadioButtonPolicyEventForm.radioButtonCustomOptionForm(
            messages(s"gains.policy-event.question.error-message"),
            messages(s"gains.policy-event.question.error-message"),
            messages(s"gains.policy-event.question.incorrect-format.error-message")).bind(
            Map(RadioButtonPolicyEventForm.selectedOption -> "Other",
              RadioButtonPolicyEventForm.input -> "55 55 55")
          ).withError("", "")
        ).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedWrongFormatErrorText, Selectors.errorHrefWrongFormat)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedWrongFormatErrorText,Option("other-text-input"))
      }
    }
  }


}