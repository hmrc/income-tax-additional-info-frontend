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
import views.html.pages.gains.PolicyHeldPageView

class PolicyHeldPageViewSpec extends ViewUnitTest {

  private val page: PolicyHeldPageView = inject[PolicyHeldPageView]

  object Selectors {
    val paragraph = "#para1"
    val paragraph2 = "#para2"
    val paragraph3 = "#para3"
    val subTitle = "#main-content > div > div > form > p"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val yearErrorHref = "#year"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedHeading: String
    val expectedParagraph: String
    val expectedParagraph2: String
    val expectedParagraph3: String
    val expectedSubTitle: String
    val expectedEmptyErrorText: String
    val expectedIncorrectFormatErrorText: String
    val expectedYearsExceedErrorText: String

  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Policy held"
    override val expectedErrorTitle: String = "Error: Policy held"
    override val expectedHeading: String = "Policy held"
    override val expectedParagraph: String =
      "If you were a UK resident while you were the beneficial owner of the policy, enter the 'number of years' from the chargeable event certificate you received from your insurer."
    override val expectedParagraph2: String = "If you lived outside the UK while you were the beneficial owner of the policy, refer to how to reduce your tax amount (opens in new tab)"
    override val expectedParagraph3: String = "If you've held the policy for less than a year, enter 0."
    override val expectedSubTitle: String = "How many years have you held this policy?"
    override val expectedEmptyErrorText: String = "Enter the number of years you have held this policy. If less than a year, enter 0"
    override val expectedIncorrectFormatErrorText: String = "Enter the number of years you have held this policy in the correct format. If less than a year, enter 0"
    override val expectedYearsExceedErrorText: String = "The number of years must be less than 100"

  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Policy held"
    override val expectedErrorTitle: String = "Error: Policy held"
    override val expectedHeading: String = "Policy held"
    override val expectedParagraph: String =
      "If you were a UK resident while you were the beneficial owner of the policy, enter the 'number of years' from the chargeable event certificate you received from your insurer."
    override val expectedParagraph2: String = "If you lived outside the UK while you were the beneficial owner of the policy, refer to how to reduce your tax amount (opens in new tab)"
    override val expectedParagraph3: String = "Os ydych wedi dal y polisi ers llai na blwyddyn, nodwch 0."
    override val expectedSubTitle: String = "Ers sawl blwyddyn rydych wedi dal y polisi hwn?"
    override val expectedEmptyErrorText: String = "Nodwch nifer y blynyddoedd yr ydych wedi dal y polisi hwn. Os yw’n llai na blwyddyn, nodwch 0"
    override val expectedIncorrectFormatErrorText: String = "Nodwch nifer y blynyddoedd rydych chi wedi dal y polisi hwn yn y fformat cywir. Os yw’n llai na blwyddyn, nodwch 0"
    override val expectedYearsExceedErrorText: String = "Mae’n rhaid i nifer y blynyddoedd fod yn llai na 100"

  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Policy held"
    override val expectedErrorTitle: String = "Error: Policy held"
    override val expectedHeading: String = "Policy held"
    override val expectedParagraph: String =
      "If your client was a UK resident while they were the beneficial owner of the policy, enter the 'number of years' from the chargeable event certificate they received from their insurer."
    override val expectedParagraph2: String = "If your client lived outside the UK while they were the beneficial owner of the policy, refer to how to reduce their tax amount (opens in new tab)"
    override val expectedParagraph3: String = "If your client has held the policy for less than a year, enter 0."
    override val expectedSubTitle: String = "How many years has your client held this policy?"
    override val expectedEmptyErrorText: String = "Enter the number of years your client has held this policy. If less than a year, enter 0"
    override val expectedIncorrectFormatErrorText: String = "Enter the number of years your client has held this policy in the correct format. If less than a year, enter 0"
    override val expectedYearsExceedErrorText: String = "The number of years must be less than 100"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Policy held"
    override val expectedErrorTitle: String = "Error: Policy held"
    override val expectedHeading: String = "Policy held"
    override val expectedParagraph: String =
      "If your client was a UK resident while they were the beneficial owner of the policy, enter the 'number of years' from the chargeable event certificate they received from their insurer."
    override val expectedParagraph2: String = "If your client lived outside the UK while they were the beneficial owner of the policy, refer to how to reduce their tax amount (opens in new tab)"
    override val expectedParagraph3: String = "Os yw’ch cleient wedi dal y polisi am lai na blwyddyn, nodwch 0."
    override val expectedSubTitle: String = "Ers sawl blwyddyn y mae’ch cleient wedi dal y polisi hwn?"
    override val expectedEmptyErrorText: String = "Nodwch nifer y blynyddoedd y mae’ch cleient wedi dal y polisi hwn. Os yw’n llai na blwyddyn, nodwch 0"
    override val expectedIncorrectFormatErrorText: String = "Nodwch nifer y blynyddoedd y mae’ch cleient wedi dal y polisi hwn yn y fformat cywir. Os yw’n llai na blwyddyn, nodwch 0"
    override val expectedYearsExceedErrorText: String = "Mae’n rhaid i nifer y blynyddoedd fod yn llai na 100"

  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy held page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputYearForm.inputYearsForm(
          s"gains.policy-held.question.error-empty.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.policy-held.question.error-incorrect.format.${if (userScenario.isAgent) "agent" else "individual"}",
          "common.gains.policy.question.error-yearsExceedsMaximum")).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph, Selectors.paragraph)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph3, Selectors.paragraph3)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedSubTitle, Selectors.subTitle)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy held page with errors if submitted form is empty" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputYearForm.inputYearsForm(
          s"gains.policy-held.question.error-empty.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.policy-held.question.error-incorrect.format.${if (userScenario.isAgent) "agent" else "individual"}",
          "common.gains.policy.question.error-yearsExceedsMaximum"
        ).bind(Map(InputYearForm.numberOfYears -> ""))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph, Selectors.paragraph)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph3, Selectors.paragraph3)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedSubTitle, Selectors.subTitle)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedEmptyErrorText, Selectors.yearErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedEmptyErrorText)
      }

      "render policy held page with errors if submitted form exceeds max years" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputYearForm.inputYearsForm(
          s"gains.policy-held.question.error-empty.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.policy-held.question.error-incorrect.format.${if (userScenario.isAgent) "agent" else "individual"}",
          "common.gains.policy.question.error-yearsExceedsMaximum"
        ).bind(Map(InputYearForm.numberOfYears -> "100"))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph, Selectors.paragraph)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph3, Selectors.paragraph3)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedSubTitle, Selectors.subTitle)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedYearsExceedErrorText, Selectors.yearErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedYearsExceedErrorText)
      }

      "render policy held page with errors if submitted form has invalid input" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, InputYearForm.inputYearsForm(
          s"gains.policy-held.question.error-empty.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.policy-held.question.error-incorrect.format.${if (userScenario.isAgent) "agent" else "individual"}",
          "common.gains.policy.question.error-yearsExceedsMaximum"
        ).bind(Map(InputYearForm.numberOfYears -> "100.100.100"))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph, Selectors.paragraph)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph2, Selectors.paragraph2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph3, Selectors.paragraph3)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedSubTitle, Selectors.subTitle)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedIncorrectFormatErrorText, Selectors.yearErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedIncorrectFormatErrorText)
      }
    }
  }
}
