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

import models.gains.PolicyCyaModel
import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.PolicySummaryPageView

import java.util.UUID

class PolicySummaryPageViewSpec extends ViewUnitTest {

  private val page: PolicySummaryPageView = inject[PolicySummaryPageView]
  private val sessionId = UUID.randomUUID().toString

  private val policyTypePageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-type/$sessionId"
  private val policyNamePageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-name/$sessionId"
  private val gainsAmountPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-amount/$sessionId"
  private val policyEventPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-event/$sessionId"
  private val policyHeldPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-held/$sessionId"
  private val paidTaxStatusPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/paid-tax-status/$sessionId"
  private val deficiencyReliefStatusUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/deficiency-relief-status/$sessionId"
  private val amountReliefAvailableUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/deficiency-relief-status/$sessionId"

  object Selectors {
    def summaryListItem(i: Int): String = s"#main-content > div > div > dl > div:nth-child($i) > dt"
    def summaryListChangeLink(i: Int): String = s"#main-content > div > div > dl > div:nth-child($i) > dd.govuk-summary-list__actions > a"

    val continueButton = "#continue"
    val getHelpLink = "#help"
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedCaption: Int => String
    val expectedSummaryListItem1: String
    val expectedSummaryListItem2: String
    val expectedSummaryListItem3: String
    val expectedSummaryListItem4: String
    val expectedSummaryListItem5: String
    val expectedSummaryListItem6: String
    val expectedSummaryListItem7: String
    val expectedSummaryListItem8: String
    val expectedSummaryListItem9: String
    val expectedSummaryListChangeLink1: String
    val expectedSummaryListChangeLink2: String
    val expectedSummaryListChangeLink3: String
    val expectedSummaryListChangeLink4: String
    val expectedSummaryListChangeLink5: String
    val expectedSummaryListChangeLink6: String
    val expectedSummaryListChangeLink7: String
    val expectedSummaryListChangeLink8: String
    val expectedSummaryListChangeLink9: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  trait SpecificExpectedResults {
  }
  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Policy summary"
    override val expectedHeading: String = "Policy summary"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedSummaryListItem1: String = "Policy type"
    override val expectedSummaryListItem2: String = "Policy number"
    override val expectedSummaryListItem3: String = "Amount of gain made"
    override val expectedSummaryListItem4: String = "Policy event"
    override val expectedSummaryListItem5: String = "Years policy held"
    override val expectedSummaryListItem6: String = "Gain treated as tax paid"
    override val expectedSummaryListItem7: String = "Deficiency relief"
    override val expectedSummaryListItem8: String = "Amount of relief available"
    override val expectedSummaryListItem9: String = "Tax paid on gain"
    override val expectedSummaryListChangeLink1: String = "Change Change type of policy"
    override val expectedSummaryListChangeLink2: String = "Change Change policy number"
    override val expectedSummaryListChangeLink3: String = "Change Change amount of gain made"
    override val expectedSummaryListChangeLink4: String = "Change Change Policy event"
    override val expectedSummaryListChangeLink5: String = "Change Change number of years policy held"
    override val expectedSummaryListChangeLink6: String = "Change Change tax paid status"
    override val expectedSummaryListChangeLink7: String = "Change Change deficiency relief status"
    override val expectedSummaryListChangeLink8: String = "Change Change amount of relief available"
    override val expectedSummaryListChangeLink9: String = "Change Change tax paid on gain"

    override val expectedButtonText: String = "Save and continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Crynodeb o’r polisi"
    override val expectedHeading: String = "Crynodeb o’r polisi"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedSummaryListItem1: String = "Math o bolisi"
    override val expectedSummaryListItem2: String = "Rhif y polisi"
    override val expectedSummaryListItem3: String = "Swm yr enillion a wnaed"
    override val expectedSummaryListItem4: String = "Digwyddiad polisi"
    override val expectedSummaryListItem5: String = "Nifer y blynyddoedd a ddaliwyd y polisi"
    override val expectedSummaryListItem6: String = "Enillion yn cael eu trin fel treth a dalwyd"
    override val expectedSummaryListItem7: String = "Rhyddhad am ddiffyg"
    override val expectedSummaryListItem8: String = "Swm y rhyddhad sydd ar gael"
    override val expectedSummaryListItem9: String = "Tax paid on gain"
    override val expectedSummaryListChangeLink1: String = "Newid Newid math o bolisi"
    override val expectedSummaryListChangeLink2: String = "Newid Newid rhif y polisi"
    override val expectedSummaryListChangeLink3: String = "Newid Newid swm yr enillion a wnaed"
    override val expectedSummaryListChangeLink4: String = "Newid Newid Digwyddiad polisi"
    override val expectedSummaryListChangeLink5: String = "Newid Newid nifer y blynyddoedd a ddaliwyd y polisi"
    override val expectedSummaryListChangeLink6: String = "Newid Newid statws y dreth a dalwyd"
    override val expectedSummaryListChangeLink7: String = "Newid Newid statws y rhyddhad am ddiffyg"
    override val expectedSummaryListChangeLink8: String = "Newid Newid swm y rhyddhad sydd ar gael"
    override val expectedSummaryListChangeLink9: String = "Newid Newid tax paid on gain"
    override val expectedButtonText: String = "Cadw ac yn eich blaen"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  object AgentExpectedCY extends SpecificExpectedResults {
  }
  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults,SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY)
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy summary page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear,
          Seq(PolicyCyaModel(
            sessionId = sessionId,
            policyType = Some("Life Insurance"),
            policyNumber = Some("abc123"),
            amountOfGain = Some(BigDecimal(100)),
            policyEvent = Some("Policy Matured..."),
            previousGain = Some(true),
            yearsPolicyHeld = Some(5),
            yearsPolicyHeldPrevious = Some(6),
            treatedAsTaxPaid = Some(true),
            taxPaidAmount = Some(BigDecimal(123.45)),
            entitledToDeficiencyRelief = Some(true),
            deficiencyReliefAmount = Some(BigDecimal(123.45))
          )), gateway=true, sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem1, Selectors.summaryListItem(1))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem2, Selectors.summaryListItem(2))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem3, Selectors.summaryListItem(3))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem4, Selectors.summaryListItem(4))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem5, Selectors.summaryListItem(7))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem6, Selectors.summaryListItem(8))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem7, Selectors.summaryListItem(9))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem8, Selectors.summaryListItem(10))

        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink1, Selectors.summaryListChangeLink(1), policyTypePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink2, Selectors.summaryListChangeLink(2), policyNamePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink3, Selectors.summaryListChangeLink(3), gainsAmountPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink4, Selectors.summaryListChangeLink(4), policyEventPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink5, Selectors.summaryListChangeLink(7), policyHeldPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink6, Selectors.summaryListChangeLink(8), paidTaxStatusPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink7, Selectors.summaryListChangeLink(9), deficiencyReliefStatusUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink8, Selectors.summaryListChangeLink(10), amountReliefAvailableUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy summary page with false values" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq(PolicyCyaModel(
          sessionId = sessionId, policyType = Some("Life Annuity"), policyNumber = Some("abc123"), amountOfGain = Some(BigDecimal(100)), policyEvent = Some("Full or part surrender"), previousGain = Some(false), yearsPolicyHeld = Some(5), yearsPolicyHeldPrevious = Some(6), treatedAsTaxPaid = Some(false), taxPaidAmount = Some(BigDecimal(123.45)), entitledToDeficiencyRelief = Some(false)
        )), gateway=true, sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem1, Selectors.summaryListItem(1))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem2, Selectors.summaryListItem(2))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem3, Selectors.summaryListItem(3))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem4, Selectors.summaryListItem(4))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem5, Selectors.summaryListItem(6))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem6, Selectors.summaryListItem(7))

        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink1, Selectors.summaryListChangeLink(1), policyTypePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink2, Selectors.summaryListChangeLink(2), policyNamePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink3, Selectors.summaryListChangeLink(3), gainsAmountPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink4, Selectors.summaryListChangeLink(4), policyEventPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink5, Selectors.summaryListChangeLink(6), policyHeldPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink6, Selectors.summaryListChangeLink(7), paidTaxStatusPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy summary page with capital redemption and sale or assignment" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq(PolicyCyaModel(
          sessionId = sessionId, policyType = Some("Capital Redemption"), policyNumber = Some("abc123"), amountOfGain = Some(BigDecimal(100)), policyEvent = Some("Sale or assignment of a policy"), previousGain = Some(false), yearsPolicyHeld = Some(5), yearsPolicyHeldPrevious = Some(6), treatedAsTaxPaid = Some(true), taxPaidAmount = Some(BigDecimal(123.45)), entitledToDeficiencyRelief = Some(false)
        )), gateway=true, sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem1, Selectors.summaryListItem(1))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem2, Selectors.summaryListItem(2))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem3, Selectors.summaryListItem(3))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem4, Selectors.summaryListItem(4))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem5, Selectors.summaryListItem(6))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem6, Selectors.summaryListItem(7))

        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink1, Selectors.summaryListChangeLink(1), policyTypePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink2, Selectors.summaryListChangeLink(2), policyNamePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink3, Selectors.summaryListChangeLink(3), gainsAmountPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink4, Selectors.summaryListChangeLink(4), policyEventPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink5, Selectors.summaryListChangeLink(6), policyHeldPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink6, Selectors.summaryListChangeLink(7), paidTaxStatusPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy summary page with fp and ppb" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq(PolicyCyaModel(
          sessionId = sessionId, policyType = Some("Foreign Policy"), policyNumber = Some("abc123"), amountOfGain = Some(BigDecimal(100)), policyEvent = Some("Personal Portfolio Bond"), previousGain = Some(false), yearsPolicyHeld = Some(5), yearsPolicyHeldPrevious = Some(6), treatedAsTaxPaid = Some(true), taxPaidAmount = Some(BigDecimal(123.45)), entitledToDeficiencyRelief = Some(false)
        )), gateway=true, sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem1, Selectors.summaryListItem(1))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem2, Selectors.summaryListItem(2))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem3, Selectors.summaryListItem(3))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem4, Selectors.summaryListItem(4))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem5, Selectors.summaryListItem(6))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem6, Selectors.summaryListItem(7))

        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink1, Selectors.summaryListChangeLink(1), policyTypePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink2, Selectors.summaryListChangeLink(2), policyNamePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink3, Selectors.summaryListChangeLink(3), gainsAmountPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink4, Selectors.summaryListChangeLink(4), policyEventPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink5, Selectors.summaryListChangeLink(6), policyHeldPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink6, Selectors.summaryListChangeLink(7), paidTaxStatusPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policy summary page with voided isa and policy matured" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq(PolicyCyaModel(
          sessionId = sessionId, policyType = Some("Voided ISA"), policyNumber = Some("abc123"), amountOfGain = Some(BigDecimal(100)), policyEvent = Some("Policy matured or a death"), previousGain = Some(false), yearsPolicyHeld = Some(5), yearsPolicyHeldPrevious = Some(6), treatedAsTaxPaid = Some(true), taxPaidAmount = Some(BigDecimal(123.45)), entitledToDeficiencyRelief = Some(false)
        )), gateway=true, sessionId).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem1, Selectors.summaryListItem(1))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem2, Selectors.summaryListItem(2))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem3, Selectors.summaryListItem(3))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem4, Selectors.summaryListItem(4))
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem5, Selectors.summaryListItem(6))

        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink1, Selectors.summaryListChangeLink(1), policyTypePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink2, Selectors.summaryListChangeLink(2), policyNamePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink3, Selectors.summaryListChangeLink(3), gainsAmountPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink4, Selectors.summaryListChangeLink(4), policyEventPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink5, Selectors.summaryListChangeLink(6), policyHeldPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

}