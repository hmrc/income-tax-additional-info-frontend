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

import forms.RadioButtonAmountForm
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.GainsDeficiencyReliefPageView

import java.util.UUID

class DeficiencyReliefPageViewSpec extends ViewUnitTest {

  private val page: GainsDeficiencyReliefPageView = inject[GainsDeficiencyReliefPageView]
  private val sessionId: String = UUID.randomUUID().toString


  private val exceedsMaxLimit = "1000000000000"

  object Selectors {
    val label = "#conditional-value > div > label"
    val hint = "#amount-hint"
    val continueButton = "#continue"
    val getHelpLink = "#help"
    val yesSelector = "#value"
    val noSelector = "#value-no"
    val expectedParagraph1 = "#para1"
    val expectedBullet1 = "#main-content > div > div > form > ul > li:nth-child(1)"
    val expectedBullet2 = "#main-content > div > div > form > ul > li:nth-child(2)"
    val expectedBullet3 = "#main-content > div > div > form > ul > li:nth-child(3)"
    val causedErrorHref = "#value"
    val causedErrorInputHref = "#amount"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
    val expectedErrorTitle: String
    val expectedHeading: String
    val expectedLabel: String
    val expectedErrorText: String
    val expectedParagraph1: String
    val expectedBullet1: Int => String
    val expectedBullet2: String
    val expectedBullet3: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedHint: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
    val expectedYesText: String
    val expectedNoText: String
    val expectedErrorText1: String
    val expectedErrorText2: String
    val expectedErrorText3: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedHint: String = "For example, £193.54"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
    override val expectedYesText: String = "Yes"
    override val expectedNoText: String = "No"
    override val expectedErrorText1: String = "Enter the amount available for relief"
    override val expectedErrorText2: String = "Enter the amount available for relief in the correct format. For example, '£193.54'"
    override val expectedErrorText3: String = "The amount of Deficiency Relief must be less than £100,000,000,000"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedHint: String = "Er enghraifft, £193.54"
    override val expectedButtonText: String = "Yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
    override val expectedYesText: String = "Iawn"
    override val expectedNoText: String = "Na"
    override val expectedErrorText1: String = "Nodwch y swm sydd ar gael ar gyfer rhyddhad"
    override val expectedErrorText2: String = "Nodwch y swm sydd ar gael ar gyfer rhyddhad yn y fformat cywir. Er enghraifft, £193.54"
    override val expectedErrorText3: String = "Mae’n rhaid i swm y Rhyddhad am Ddiffyg fod yn llai na £100,000,000,000"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Are you entitled to deficiency relief?"
    override val expectedErrorTitle: String = "Error: Are you entitled to deficiency relief?"
    override val expectedHeading: String = "Deficiency relief"
    override val expectedParagraph1: String = "You may be entitled to this relief if:"
    override val expectedBullet1: Int => String = (taxYear: Int) => s"your policy or annuity ended between 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedBullet2: String = "you made gains on the policy or annuity in an earlier tax year"
    override val expectedBullet3: String = "you pay higher rate tax"
    override val expectedLabel: String = "What amount is available for relief?"
    override val expectedErrorText: String = "Select Yes if you are entitled to deficiency relief"

  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A oes gennych hawl i Ryddhad am Ddiffyg?"
    override val expectedErrorTitle: String = "Gwall: A oes gennych hawl i Ryddhad am Ddiffyg?"
    override val expectedHeading: String = "Rhyddhad am ddiffyg"
    override val expectedParagraph1: String = "Mae’n bosibl y bydd hawl gennych i’r rhyddhad hwn:"
    override val expectedBullet1: Int => String = (taxYear: Int) => s"os daeth eich polisi neu flwydd-dal i ben rhwng 6 Ebrill ${taxYear - 1} a 5 Ebrill $taxYear"
    override val expectedBullet2: String = "os gwnaethoch enillion ar y polisi neu’r blwydd-dal mewn blwyddyn dreth gynharach"
    override val expectedBullet3: String = "os ydych yn talu treth ar y gyfradd uwch"
    override val expectedLabel: String = "Beth yw’r swm sydd ar gael ar gyfer rhyddhad?"
    override val expectedErrorText: String = "Dewiswch ‘Iawn’ os oes gennych hawl i ryddhad am ddiffyg"

  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle: String = "Is your client entitled to Deficiency relief?"
    override val expectedErrorTitle: String = "Error: Is your client entitled to Deficiency relief?"
    override val expectedHeading: String = "Deficiency relief"
    override val expectedParagraph1: String = "Your client may be entitled to this relief if:"
    override val expectedBullet1: Int => String = (taxYear: Int) => s"your client's policy or annuity ended between 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedBullet2: String = "your client made gains on the policy or annuity in an earlier tax year"
    override val expectedBullet3: String = "your client pays higher rate tax"
    override val expectedLabel: String = "What amount is available for relief?"
    override val expectedErrorText: String = "Select yes if your client is entitled to deficiency relief"

  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "A oes gan eich cleient yr hawl i Ryddhad am Ddiffyg?"
    override val expectedErrorTitle: String = "Gwall: A oes gan eich cleient yr hawl i Ryddhad am Ddiffyg?"
    override val expectedHeading: String = "Rhyddhad am ddiffyg"
    override val expectedParagraph1: String = "Mae’n bosibl y bydd gan eich cleient hawl i’r rhyddhad hwn:"
    override val expectedBullet1: Int => String = (taxYear: Int) => s"os daeth ei bolisi neu’i flwydd-dal i ben rhwng 6 Ebrill ${taxYear - 1} a 5 Ebrill $taxYear"
    override val expectedBullet2: String = "os gwnaeth eich cleient enillion ar y polisi neu’r blwydd-dal mewn blwyddyn dreth gynharach"
    override val expectedBullet3: String = "os yw’ch cleient yn talu treth ar y gyfradd uwch"
    override val expectedLabel: String = "Beth yw’r swm sydd ar gael ar gyfer rhyddhad?"
    override val expectedErrorText: String = "Dewiswch ‘Iawn’ os oes gan eich cleient yr hawl i Ryddhad am Ddiffyg"

  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render deficiency relief page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.deficiency-relief-status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}",
          "gains.deficiency-relief-status.question.input.error.noEntry",
          "gains.deficiency-relief-status.question.input.error.incorrectFormat",
          "gains.deficiency-relief-status.question.input.error.amountExceedsMax"
        ), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedHeading)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedParagraph1, Selectors.expectedParagraph1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedBullet1(taxYear), Selectors.expectedBullet1)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedBullet2, Selectors.expectedBullet2)
        textOnPageCheck(userScenario.specificExpectedResults.get.expectedBullet3, Selectors.expectedBullet3)
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
      "render deficiency relief page with errors if submitted form has invalid radio selection" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.deficiency-relief-status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}",
          "gains.deficiency-relief-status.question.input.error.noEntry",
          "gains.deficiency-relief-status.question.input.error.incorrectFormat",
          "gains.deficiency-relief-status.question.input.error.amountExceedsMax"
        ).bind(Map(
          RadioButtonAmountForm.amount -> "",
          RadioButtonAmountForm.yesNo -> ""
        )), sessionId).body)

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

        errorSummaryCheck(userScenario.specificExpectedResults.get.expectedErrorText, Selectors.causedErrorHref)
        errorAboveElementCheck(userScenario.specificExpectedResults.get.expectedErrorText, userScenario.isWelsh)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render deficiency relief page with errors when input field empty" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.deficiency-relief-status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}",
          "gains.deficiency-relief-status.question.input.error.noEntry",
          "gains.deficiency-relief-status.question.input.error.incorrectFormat",
          "gains.deficiency-relief-status.question.input.error.amountExceedsMax"
        ).bind(Map(
          RadioButtonAmountForm.amount -> "",
          RadioButtonAmountForm.yesNo -> "true"
        )), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText1, Selectors.causedErrorInputHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText1, userScenario.isWelsh)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render deficiency relief page with errors when input has invalid format" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.deficiency-relief-status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}",
          "gains.deficiency-relief-status.question.input.error.noEntry",
          "gains.deficiency-relief-status.question.input.error.incorrectFormat",
          "gains.deficiency-relief-status.question.input.error.amountExceedsMax"
        ).bind(Map(
          RadioButtonAmountForm.amount -> "f",
          RadioButtonAmountForm.yesNo -> "true"
        )), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText2, Selectors.causedErrorInputHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText2, userScenario.isWelsh)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render deficiency relief page with errors when input exceeds max value" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, RadioButtonAmountForm.radioButtonAndAmountForm(
          s"gains.deficiency-relief-status.question.radio.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.deficiency-relief-status.question.input.error.noEntry.${if (userScenario.isAgent) "agent" else "individual"}",
          s"gains.deficiency-relief-status.question.input.error.incorrectFormat.${if (userScenario.isAgent) "agent" else "individual"}",
          "gains.deficiency-relief-status.question.input.error.amountExceedsMax"
        ).bind(Map(
          RadioButtonAmountForm.amount -> exceedsMaxLimit,
          RadioButtonAmountForm.yesNo -> "true"
        )), sessionId).body)

        welshToggleCheck(userScenario.isWelsh)

        errorSummaryCheck(userScenario.commonExpectedResults.expectedErrorText3, Selectors.causedErrorInputHref)
        errorAboveElementCheck(userScenario.commonExpectedResults.expectedErrorText3, userScenario.isWelsh)
      }
    }
  }
}
