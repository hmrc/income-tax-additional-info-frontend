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
import views.html.pages.gains.PolicyEventPageView

class PolicyEventPageViewSpec extends ViewUnitTest {

  private val page: PolicyEventPageView = inject[PolicyEventPageView]

  object Selectors {
    val paragraph = "#main-content > div > div > p"
    val listItemOne = "#main-content > div > div > ul > li:nth-child(1)"
    val listItemTwo = "#main-content > div > div > ul > li:nth-child(2)"
    val listItemThree = "#main-content > div > div > ul > li:nth-child(3)"
    val listItemFour = "#main-content > div > div > ul > li:nth-child(4)"
    val causedGainsHint = "#value-hint"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val causedGainsErrorHref = "#value"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedHeading: String
    val expectedParagraph: String
    val expectedListItemOne: String
    val expectedListItemTwo: String
    val expectedListItemThree: String
    val expectedListItemFour: String
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
    override val expectedHint: String = "For example, policy matured, part surrender of policy or death of spouse."
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedHint: String = "Er enghraifft, aeddfedodd polisi, ildiodd y polisi’n rhannol neu bu farw briod."
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Policy event"
    override val expectedErrorTitle: String = "Error: Policy event"
    override val expectedHeading: String = "Policy event"
    override val expectedParagraph: String = "The most common causes of gains are if:"
    override val expectedListItemOne: String = "cash or other benefits were received on a full or part surrender of a policy"
    override val expectedListItemTwo: String = "a policy matured or ended by the death of the life insured"
    override val expectedListItemThree: String = "there was a sale of assignment of a UK policy, or part of a policy, for value"
    override val expectedListItemFour: String = "the policy was a Personal Portfolio Bond, even if the insurer had not paid cash or other benefits during the year"
    override val expectedErrorText: String = "Enter the reason for this gain. For example, policy matured, part surrender of the policy or death of spouse"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Digwyddiad polisi"
    override val expectedErrorTitle: String = "Error: Digwyddiad polisi"
    override val expectedHeading: String = "Digwyddiad polisi"
    override val expectedParagraph: String = "Yr achosion mwyaf cyffredin o enillion yw:"
    override val expectedListItemOne: String = "os cafwyd arian parod neu fudd-daliadau eraill wrth ildio polisi’n llawn neu’n rhannol"
    override val expectedListItemTwo: String = "gwnaeth polisi aeddfedu neu ddod i ben oherwydd marwolaeth y bywyd a yswiriwyd"
    override val expectedListItemThree: String = "gwerthwyd neu neilltuwyd polisi yn y DU, neu ran o bolisi, ar gyfer gwerth"
    override val expectedListItemFour: String = "roedd y polisi yn Bondiau Portffolio Personol, hyd yn oed os nad oedd yr yswiriwr wedi talu arian parod na budd-daliadau eraill yn ystod y flwyddyn"
    override val expectedErrorText: String = "Nodwch y rheswm dros yr enillion hyn. Er enghraifft, aeddfedodd polisi, ildiad rhannol o’r polisi neu farwolaeth y priod"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Policy event"
    override val expectedErrorTitle: String = "Error: Policy event"
    override val expectedHeading: String = "Policy event"
    override val expectedParagraph: String = "The most common causes of gains are if:"
    override val expectedListItemOne: String = "cash or other benefits were received on a full or part surrender of a policy"
    override val expectedListItemTwo: String = "a policy matured or ended by the death of the life insured"
    override val expectedListItemThree: String = "there was a sale of assignment of a UK policy, or part of a policy, for value"
    override val expectedListItemFour: String = "the policy was a Personal Portfolio Bond, even if the insurer had not paid cash or other benefits during the year"
    override val expectedErrorText: String = "Enter the reason for this gain. For example, policy matured, part surrender of the policy or death of spouse"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Digwyddiad polisi"
    override val expectedErrorTitle: String = "Error: Digwyddiad polisi"
    override val expectedHeading: String = "Digwyddiad polisi"
    override val expectedParagraph: String = "Yr achosion mwyaf cyffredin o enillion yw:"
    override val expectedListItemOne: String = "os cafwyd arian parod neu fudd-daliadau eraill wrth ildio polisi’n llawn neu’n rhannol"
    override val expectedListItemTwo: String = "gwnaeth polisi aeddfedu neu ddod i ben oherwydd marwolaeth y bywyd a yswiriwyd"
    override val expectedListItemThree: String = "gwerthwyd neu neilltuwyd polisi yn y DU, neu ran o bolisi, ar gyfer gwerth"
    override val expectedListItemFour: String = "roedd y polisi yn Bondiau Portffolio Personol, hyd yn oed os nad oedd yr yswiriwr wedi talu arian parod na budd-daliadau eraill yn ystod y flwyddyn"
    override val expectedErrorText: String = "Nodwch y rheswm dros yr enillion hyn. Er enghraifft, aeddfedodd polisi, ildiad rhannol o’r polisi neu farwolaeth y priod"
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

        implicit val document: Document = Jsoup.parse(page(taxYear, InputFieldForm.inputFieldForm(userScenario.isAgent,
          s"gains.policy-event.question.error-message")).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph, Selectors.paragraph)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.causedGainsHint)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedListItemOne, Selectors.listItemOne)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedListItemTwo, Selectors.listItemTwo)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedListItemThree, Selectors.listItemThree)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedListItemFour, Selectors.listItemFour)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy event page with errors if submitted form is invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputFieldForm.inputFieldForm(userScenario.isAgent,
          s"gains.policy-event.question.error-message").bind(Map(InputFieldForm.value -> ""))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph, Selectors.paragraph)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHint, Selectors.causedGainsHint)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedListItemOne, Selectors.listItemOne)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedListItemTwo, Selectors.listItemTwo)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedListItemThree, Selectors.listItemThree)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedListItemFour, Selectors.listItemFour)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.causedGainsErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }
}
