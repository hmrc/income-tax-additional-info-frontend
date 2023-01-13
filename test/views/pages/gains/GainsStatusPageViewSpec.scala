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

import forms.RadioButtonYearForm
import forms.RadioButtonYearForm.{yearInput, yesNo}
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.GainsStatusPageView

class GainsStatusPageViewSpec extends ViewUnitTest {

  private val page: GainsStatusPageView = inject[GainsStatusPageView]

  object Selectors {
    val yesSelector = "#value"
    val noSelector = "#value-no"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val errorSummary = "#value"
    val radioError = "#value-error"
    val inputLabel = "#conditional-value > div > label"
    val inputHint = "#last-gain-hint"
    val inputError = "#last-gain"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedRadioHeading: String
    val expectedInputLabel: String
    val expectedInputHint: String
    val expectedErrorText1: String
    val expectedErrorText2: String
    val expectedErrorText3: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val yesText: String
    val noText: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedErrorText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val yesText: String = "Yes"
    override val noText: String = "No"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedErrorText: String = "Enter the number of years in the correct format. For example, '3'"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val yesText: String = "Iawn"
    override val noText: String = "Na"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedErrorText: String = "Nodwch nifer y blynyddoedd yn y fformat cywir. Er enghraifft, '3'"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Have you made a gain from this policy before?"
    override val expectedErrorTitle: String = "Error: Have you made a gain from this policy before?"
    override val expectedRadioHeading: String = "Have you made a gain from this policy before?"
    override val expectedInputLabel: String = "How many years since your last gain?"
    override val expectedInputHint: String = "If your last gain was less than a year ago, enter 0."
    override val expectedErrorText1: String = "Select Yes if you have made a gain from this policy before"
    override val expectedErrorText2: String = "Enter the number of years since your last gain. If your last gain was less than a year ago, enter 0"
    override val expectedErrorText3: String = "Your last gain must be less than 100 years"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A ydych wedi gwneud ennill o’r polisi hwn o’r blaen?"
    override val expectedErrorTitle: String = "Error: A ydych wedi gwneud ennill o’r polisi hwn o’r blaen?"
    override val expectedRadioHeading: String = "A ydych wedi gwneud ennill o’r polisi hwn o’r blaen?"
    override val expectedInputLabel: String = "Sawl blwyddyn sydd wedi bod ers eich ennill diwethaf?"
    override val expectedInputHint: String = "Os oedd eich ennill diwethaf lai na blwyddyn yn ôl, nodwch 0"
    override val expectedErrorText1: String = "Dewiswch ‘Iawn’ os ydych wedi gwneud ennill ar y polisi hwn o’r blaen"
    override val expectedErrorText2: String = "Nodwch nifer y blynyddoedd ers eich ennill diwethaf. Os oedd eich ennill diwethaf llai na blwyddyn yn ôl, nodwch 0"
    override val expectedErrorText3: String = "Mae’n rhaid i’ch enillion diwethaf fod yn llai na 100 mlynedd yn ôl"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Has your client made a gain from this policy before?"
    override val expectedErrorTitle: String = "Error: Has your client made a gain from this policy before?"
    override val expectedRadioHeading: String = "Has your client made a gain from this policy before?"
    override val expectedInputLabel: String = "How many years since your client's last gain?"
    override val expectedInputHint: String = "If your client's last gain was less than a year ago, enter 0."
    override val expectedErrorText1: String = "Select yes if your client has made a gain from this policy before"
    override val expectedErrorText2: String = "Enter the number of years since your client's last gain. If your client's last gain was less than a year ago, enter 0"
    override val expectedErrorText3: String = "Your client's last gain must be less than 100 years"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A yw’ch cleient wedi gwneud enillion ar y polisi hwn o’r blaen?"
    override val expectedErrorTitle: String = "Error: A yw’ch cleient wedi gwneud enillion ar y polisi hwn o’r blaen?"
    override val expectedRadioHeading: String = "A yw’ch cleient wedi gwneud enillion ar y polisi hwn o’r blaen?"
    override val expectedInputLabel: String = "Sawl blwyddyn sydd wedi mynd heibio ers enillion diwethaf eich cleient?"
    override val expectedInputHint: String = "Os oedd enillion diwethaf eich cleient llai na blwyddyn yn ôl, nodwch 0."
    override val expectedErrorText1: String = "Dewiswch ‘Iawn’ os yw’ch cleient wedi gwneud enillion ar y polisi hwn o’r blaen"
    override val expectedErrorText2: String = "Nodwch nifer y blynyddoedd ers enillion diwethaf eich cleient. Os oedd enillion diwethaf eich cleient llai na blwyddyn yn ôl, nodwch 0"
    override val expectedErrorText3: String = "Mae’n rhaid i enillion diwethaf eich cleient fod yn llai na 100 mlynedd yn ôl"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    val form = RadioButtonYearForm.radioButtonAndYearForm(
      s"gains.status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}",
      s"gains.status.question.input.year.error.noeEntry.${if (userScenario.isAgent) "agent" else "individual"}",
      s"common.error.invalid_Year_Format",
      s"gains.status.question.input.year.error.maxLimit.${if (userScenario.isAgent) "agent" else "individual"}"
    )
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render gains status page with empty form and no value selected" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form, taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }

      "render gains status page with filled in form using selected 'yes' value and year as '20'" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.fill(true, Some(20)), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = true)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedInputLabel, Selectors.inputLabel)
        hintTextCheck(userScenario.specificExpectedResults.get.expectedInputHint, Selectors.inputHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }

      "render gains status page with filled in form using selected 'no' value" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.fill(false, None), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = true)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    val form = RadioButtonYearForm.radioButtonAndYearForm(
      s"gains.status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}",
      s"gains.status.question.input.year.error.noeEntry.${if (userScenario.isAgent) "agent" else "individual"}",
      s"common.error.invalid_Year_Format",
      s"gains.status.question.input.year.error.maxLimit.${if (userScenario.isAgent) "agent" else "individual"}"
    )

    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render gains status page with error if empty form is submitted and invalid" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.bind(Map(yesNo -> "", yearInput -> "None")), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = false)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedInputLabel, Selectors.inputLabel)
        hintTextCheck(userScenario.specificExpectedResults.get.expectedInputHint, Selectors.inputHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText1, Selectors.errorSummary)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText1)
      }

      "render gains status page with error if form is submitted invalid with 'yes' and empty year" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.bind(Map(yesNo -> "true", yearInput -> "")), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = true)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedInputLabel, Selectors.inputLabel)
        hintTextCheck(userScenario.specificExpectedResults.get.expectedInputHint, Selectors.inputHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText2, Selectors.inputError)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText2)
      }

      "render gains status page with error if form is submitted invalid with 'yes' and incorrect year" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.bind(Map(yesNo -> "true", yearInput -> "wrongYear")), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = true)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedInputLabel, Selectors.inputLabel)
        hintTextCheck(userScenario.specificExpectedResults.get.expectedInputHint, Selectors.inputHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText, Selectors.inputError)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText)
      }

      "render gains status page with error if form is submitted invalid with 'yes' and exceeding max year 100" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(form.bind(Map(yesNo -> "true", yearInput -> "100")), taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedErrorTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedRadioHeading)
        radioButtonCheck(userScenario.commonExpectedResults.yesText, radioNumber = 1, checked = true)
        radioButtonCheck(userScenario.commonExpectedResults.noText, radioNumber = 2, checked = false)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedInputLabel, Selectors.inputLabel)
        hintTextCheck(userScenario.specificExpectedResults.get.expectedInputHint, Selectors.inputHint)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText3, Selectors.inputError)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText3)
      }
    }
  }
}