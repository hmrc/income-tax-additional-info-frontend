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

import forms.gains.RadioButtonPolicyTypeForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.PolicyTypePageView

class PolicyTypePageViewSpec extends ViewUnitTest {

  private val page: PolicyTypePageView = inject[PolicyTypePageView]

  object Selectors {
    val paragraph = "#main-content > div > div > p"
    val radioHeading = "#main-content > div > div > form > div > fieldset > legend > h1"
    val radioItemOne = "#main-content > div > div > form > div > fieldset > div > div:nth-child(1) > label"
    val radioItemTwo = "#main-content > div > div > form > div > fieldset > div > div:nth-child(2) > label"
    val radioItemThree = "#main-content > div > div > form > div > fieldset > div > div:nth-child(3) > label"
    val radioItemFour = "#main-content > div > div > form > div > fieldset > div > div:nth-child(4) > label"
    val radioItemFive = "#main-content > div > div > form > div > fieldset > div > div:nth-child(5) > label"

    val helpOneTitle = "#policy-type-details1 > summary > span"
    val helpOneParagraph = "#policy-type-details1 > div > p"
    val helpOneListItemOne = "#policy-type-details1  > div > ul > li:nth-child(1)"
    val helpOneListItemTwo = "#policy-type-details1 > div > ul > li:nth-child(2)"

    val helpTwoTitle = "#policy-type-details2 > summary > span"
    val helpTwoParagraph = "#policy-type-details2 > div > p"
    val helpTwoListItemOne = "#policy-type-details2 > div > ul > li:nth-child(1)"
    val helpTwoListItemTwo = "#policy-type-details2 > div > ul > li:nth-child(2)"
    val helpTwoListItemThree = "#policy-type-details2 > div > ul > li:nth-child(3)"

    val continueButton = "#continue"
    val getHelpLink = "#help"
    val errorHref = "#policy-type"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedParagraphText: String
    val expectedRadioHeadingText: String
    val expectedRadioItemFour: String
    val expectedHelpContentOneBulletOne: String
    val expectedHelpContentOneBulletTwo: String
    val expectedHelpContentTwoBulletOne: String
    val expectedHelpContentTwoBulletThree: String
    val expectedErrorText: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedHeading: String
    val expectedRadioItemOne: String
    val expectedRadioItemTwo: String
    val expectedRadioItemThree: String
    val expectedRadioItemFive: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedHelpDropdownTitleOne: String
    val expectedHelpDropdownTitleTwo: String
    val expectedHelpContentParagraphOne: String
    val expectedHelpContentParagraphTwo: String
    val expectedHelpContentTwoBulletTwo: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHeading: String = "Policy type"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedHelpDropdownTitleOne: String = "Help with voided ISAs and cancelled policies"
    override val expectedHelpDropdownTitleTwo: String = "Help with foreign policies"
    override val expectedRadioItemOne: String = "Life Insurance"
    override val expectedRadioItemTwo: String = "Life Annuity"
    override val expectedRadioItemThree: String = "Capital Redemption"
    override val expectedRadioItemFive: String = "A foreign policy"
    override val expectedHelpContentParagraphOne: String = "This can include:"
    override val expectedHelpContentParagraphTwo: String = "A foreign policy is one:"
    override val expectedHelpContentTwoBulletTwo: String = "where premiums were paid in another country"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedHeading: String = "Policy type"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedHelpDropdownTitleOne: String = "Help with voided ISAs and cancelled policies"
    override val expectedHelpDropdownTitleTwo: String = "Help with foreign policies"
    override val expectedRadioItemOne: String = "Life Insurance"
    override val expectedRadioItemTwo: String = "Life Annuity"
    override val expectedRadioItemThree: String = "Capital Redemption"
    override val expectedRadioItemFive: String = "A foreign policy"
    override val expectedHelpContentParagraphOne: String = "This can include:"
    override val expectedHelpContentParagraphTwo: String = "A foreign policy is one:"
    override val expectedHelpContentTwoBulletTwo: String = "where premiums were paid in another country"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "What type of policy gave you a gain?"
    override val expectedErrorTitle: String = "Error: What type of policy gave you a gain?"
    override val expectedParagraphText: String = "Tell us the type of policy that gave you a gain. If you had a gain from more than one policy you can add this later."
    override val expectedRadioHeadingText: String = "What type of policy gave you a gain?"
    override val expectedRadioItemFour: String = "Voided ISA - a policy cancelled by your ISA manager"
    override val expectedHelpContentOneBulletOne: String = "life insurance policies cancelled by your life insurance company"
    override val expectedHelpContentOneBulletTwo: String = "individual Savings Accounts cancelled by your ISA manager, known as voided ISAs."
    override val expectedHelpContentTwoBulletOne: String = "started while you were a non-UK resident"
    override val expectedHelpContentTwoBulletThree: String = "surrendered, paid-out or matured, while you were a non-UK resident or after your return to the UK"
    override val expectedErrorText: String = "Select the type of policy that gave you a gain"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "What type of policy gave you a gain?"
    override val expectedErrorTitle: String = "Error: What type of policy gave you a gain?"
    override val expectedParagraphText: String = "Tell us the type of policy that gave you a gain. If you had a gain from more than one policy you can add this later."
    override val expectedRadioHeadingText: String = "What type of policy gave you a gain?"
    override val expectedRadioItemFour: String = "Voided ISA - a policy cancelled by your ISA manager"
    override val expectedHelpContentOneBulletOne: String = "life insurance policies cancelled by your life insurance company"
    override val expectedHelpContentOneBulletTwo: String = "individual Savings Accounts cancelled by your ISA manager, known as voided ISAs."
    override val expectedHelpContentTwoBulletOne: String = "started while you were a non-UK resident"
    override val expectedHelpContentTwoBulletThree: String = "surrendered, paid-out or matured, while you were a non-UK resident or after your return to the UK"
    override val expectedErrorText: String = "Select the type of policy that gave you a gain"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "What type of policy gave your client a gain?"
    override val expectedErrorTitle: String = "Error: What type of policy gave your client a gain?"
    override val expectedParagraphText: String = "Tell us the type of policy that gave your client a gain. If your client had a gain from more than one policy you can add this later."
    override val expectedRadioHeadingText: String = "What type of policy gave your client a gain?"
    override val expectedRadioItemFour: String = "Voided ISA - a policy cancelled by your client's ISA manager"
    override val expectedHelpContentOneBulletOne: String = "life insurance policies cancelled by your client's life insurance company"
    override val expectedHelpContentOneBulletTwo: String = "individual Savings Accounts cancelled by your client's ISA manager, known as voided ISAs."
    override val expectedHelpContentTwoBulletOne: String = "started while your client was a non-UK resident"
    override val expectedHelpContentTwoBulletThree: String = "surrendered, paid-out or matured, while your client was a non-UK resident or after their return to the UK"
    override val expectedErrorText: String = "Select the type of policy that gave your client a gain"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "What type of policy gave your client a gain?"
    override val expectedErrorTitle: String = "Error: What type of policy gave your client a gain?"
    override val expectedParagraphText: String = "Tell us the type of policy that gave your client a gain. If your client had a gain from more than one policy you can add this later."
    override val expectedRadioHeadingText: String = "What type of policy gave your client a gain?"
    override val expectedRadioItemFour: String = "Voided ISA - a policy cancelled by your client's ISA manager"
    override val expectedHelpContentOneBulletOne: String = "life insurance policies cancelled by your client's life insurance company"
    override val expectedHelpContentOneBulletTwo: String = "individual Savings Accounts cancelled by your client's ISA manager, known as voided ISAs."
    override val expectedHelpContentTwoBulletOne: String = "started while your client was a non-UK resident"
    override val expectedHelpContentTwoBulletThree: String = "surrendered, paid-out or matured, while your client was a non-UK resident or after their return to the UK"
    override val expectedErrorText: String = "Select the type of policy that gave your client a gain"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy type page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear,
          RadioButtonPolicyTypeForm.radioButtonCustomOptionForm(messages(s"gains.policy-type.error.missing-input.${if (userScenario.isAgent) "agent" else "individual"}"))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraphText, Selectors.paragraph)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedRadioHeadingText, Selectors.radioHeading)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemOne, Selectors.radioItemOne)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemTwo, Selectors.radioItemTwo)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemThree, Selectors.radioItemThree)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedRadioItemFour, Selectors.radioItemFour)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemFive, Selectors.radioItemFive)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpDropdownTitleOne, Selectors.helpOneTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpDropdownTitleTwo, Selectors.helpTwoTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentParagraphOne, Selectors.helpOneParagraph)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentParagraphTwo, Selectors.helpTwoParagraph)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHelpContentOneBulletOne, Selectors.helpOneListItemOne)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHelpContentOneBulletTwo, Selectors.helpOneListItemTwo)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHelpContentTwoBulletOne, Selectors.helpTwoListItemOne)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentTwoBulletTwo, Selectors.helpTwoListItemTwo)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHelpContentTwoBulletThree, Selectors.helpTwoListItemThree)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy type page with errors if submitted form is empty" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear,
          RadioButtonPolicyTypeForm.radioButtonCustomOptionForm(messages(s"gains.policy-type.error.missing-input.${if (userScenario.isAgent) "agent" else "individual"}")).bind(
            Map(RadioButtonPolicyTypeForm.selectedOption -> "error")
          ).withError("", "")
        ).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraphText, Selectors.paragraph)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedRadioHeadingText, Selectors.radioHeading)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemOne, Selectors.radioItemOne)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemTwo, Selectors.radioItemTwo)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemThree, Selectors.radioItemThree)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedRadioItemFour, Selectors.radioItemFour)
        textOnPageCheck(userScenario.commonExpectedResults.expectedRadioItemFive, Selectors.radioItemFive)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpDropdownTitleOne, Selectors.helpOneTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpDropdownTitleTwo, Selectors.helpTwoTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentParagraphOne, Selectors.helpOneParagraph)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentParagraphTwo, Selectors.helpTwoParagraph)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHelpContentOneBulletOne, Selectors.helpOneListItemOne)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHelpContentOneBulletTwo, Selectors.helpOneListItemTwo)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHelpContentTwoBulletOne, Selectors.helpTwoListItemOne)
        textOnPageCheck(userScenario.commonExpectedResults.expectedHelpContentTwoBulletTwo, Selectors.helpTwoListItemTwo)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedHelpContentTwoBulletThree, Selectors.helpTwoListItemThree)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.errorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText)
      }
    }
  }
}