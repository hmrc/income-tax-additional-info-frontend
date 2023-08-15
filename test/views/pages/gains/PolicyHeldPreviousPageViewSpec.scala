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

import forms.gains.InputYearForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.PolicyHeldPreviousPageView

import java.util.UUID

class PolicyHeldPreviousPageViewSpec extends ViewUnitTest {

  private val page: PolicyHeldPreviousPageView = inject[PolicyHeldPreviousPageView]
  private val sessionId: String = UUID.randomUUID().toString

  object Selectors {
    val title = "#main-content > div > div > h1"
    val paragraph1 = "#p1"
    val paragraph2 = "#p2"
    val heading = "#main-content > div > div > form > div > label"
    val paragraph3 = "#year-hint"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val yearErrorHref = "#year"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedParagraph1: String
    val expectedParagraph2: String
    val expectedParagraph3: String
    val expectedEmptyErrorText: String

  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedIncorrectFormatErrorText: String
    val expectedYearsExceedErrorText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedIncorrectFormatErrorText: String = "Enter the number of years in the correct format, for example 12"
    override val expectedYearsExceedErrorText: String = "The number of years must be less than 100"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedIncorrectFormatErrorText: String = "Nodwch nifer y blynyddoedd yn y fformat cywir, er enghraifft, 12"
    override val expectedYearsExceedErrorText: String = "Mae’n rhaid i nifer y blynyddoedd fod yn llai na 100"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Years since your last gain"
    override val expectedHeading: String = "How many years since your last gain?"
    override val expectedParagraph1: String = "If you were a UK resident while you were the beneficial owner of the policy, enter the 'number of years' from the chargeable event certificate you received from your insurer."
    override val expectedParagraph2: String = "If you lived outside the UK while you were the beneficial owner of the policy, refer to how to reduce your tax amount (opens in new tab)"
    override val expectedParagraph3: String = "If your last gain was less than a year ago, enter 0."
    override val expectedEmptyErrorText: String = "Enter the number of years since your last gain."
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Blynyddoedd ers eich ennill diwethaf"
    override val expectedHeading: String = "Sawl blwyddyn sydd wedi bod ers eich ennill diwethaf?"
    override val expectedParagraph1: String = "Os oeddech yn breswylydd yn y DU tra oeddech yn berchennog llesiannol y polisi, nodwch ‘nifer y blynyddoedd’ sydd ar y dystysgrif digwyddiad trethadwy a gawsoch gan eich yswiriwr."
    override val expectedParagraph2: String = "Os oeddech yn byw y tu allan i’r DU tra oeddech yn berchennog llesiannol y polisi, dysgwch sut i ostwng swm eich treth (yn agor tab newydd)"
    override val expectedParagraph3: String = "Os oedd eich ennill diwethaf llai na blwyddyn yn ôl, nodwch 0."
    override val expectedEmptyErrorText: String = "Nodwch nifer y blynyddoedd ers eich ennill diwethaf."

  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Years since your client's last gain"
    override val expectedHeading: String = "How many years since their last gain?"
    override val expectedParagraph1: String = "If your client was a UK resident while they were the beneficial owner of the policy, enter the 'number of years' from the chargeable event certificate they received from their insurer."
    override val expectedParagraph2: String = "If your client lived outside the UK while they were the beneficial owner of the policy, refer to how to reduce their tax amount (opens in new tab)"
    override val expectedParagraph3: String = "If their last gain was less than a year ago, enter 0."
    override val expectedEmptyErrorText: String = "Enter the number of years since your client's last gain."

  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Blynyddoedd ers ennill diwethaf eich cleient"
    override val expectedHeading: String = "Sawl blwyddyn sydd wedi bod ers ei ennill diwethaf?"
    override val expectedParagraph1: String = "Os oedd eich cleient yn breswylydd yn y DU tra oedd yn berchennog llesiannol y polisi, nodwch ‘nifer y blynyddoedd’ sydd ar y dystysgrif digwyddiad trethadwy a gafodd gan ei yswiriwr."
    override val expectedParagraph2: String = "Os oedd eich cleient yn byw y tu allan i’r DU tra oedd yn berchennog llesiannol y polisi, dysgwch sut i ostwng swm ei dreth (yn agor tab newydd)"
    override val expectedParagraph3: String = "Os oedd ei ennill diwethaf llai na blwyddyn yn ôl, nodwch 0."
    override val expectedEmptyErrorText: String = "Nodwch nifer y blynyddoedd ers enillion diwethaf eich cleient."
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy held previous page successful" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputYearForm.inputYearsForm(
          s"gains.policy-held-previous.question.error-empty.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.policy-held-previous.question.error-incorrect.format",
          "common.gains.policy.question.error-yearsExceedsMaximum"), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph3, Selectors.paragraph3)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy held previous page with errors if submitted form is empty" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputYearForm.inputYearsForm(
          s"gains.policy-held-previous.question.error-empty.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.policy-held-previous.question.error-incorrect.format",
          "common.gains.policy.question.error-yearsExceedsMaximum"
        ).bind(Map(InputYearForm.numberOfYears -> "")), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph3, Selectors.paragraph3)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedEmptyErrorText, Selectors.yearErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedEmptyErrorText, userScenario.isWelsh)
      }

      "render policy held previous page with errors if submitted form exceeds max years" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputYearForm.inputYearsForm(
          s"gains.policy-held-previous.question.error-empty.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.policy-held-previous.question.error-incorrect.format",
          "common.gains.policy.question.error-yearsExceedsMaximum"
        ).bind(Map(InputYearForm.numberOfYears -> "100")), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph3, Selectors.paragraph3)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.commonExpectedResults.expectedYearsExceedErrorText, Selectors.yearErrorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedYearsExceedErrorText, userScenario.isWelsh)
      }

      "render policy held previous page with errors if submitted form has invalid input" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputYearForm.inputYearsForm(
          s"gains.policy-held-previous.question.error-empty.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.policy-held-previous.question.error-incorrect.format",
          "common.gains.policy.question.error-yearsExceedsMaximum"
        ).bind(Map(InputYearForm.numberOfYears -> "100.100.100")), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.paragraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph3, Selectors.paragraph3)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.commonExpectedResults.expectedIncorrectFormatErrorText, Selectors.yearErrorHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedIncorrectFormatErrorText, userScenario.isWelsh)
      }
    }
  }
}
