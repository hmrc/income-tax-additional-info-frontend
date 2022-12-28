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

import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.PolicySummaryPageView

class PolicySummaryPageViewSpec extends ViewUnitTest {

  private val page: PolicySummaryPageView = inject[PolicySummaryPageView]

  private val customerReferencePageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-name"
  private val policyEventPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-event"
  private val gainsStatusPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-status"
  private val policyHeldPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-held"
  private val gainsAmountPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-amount"
  private val taxPaidOnGainPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/paid-tax-status"
  private val deficiencyUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/deficiency-relief-status"

  object Selectors {
    val summaryListItem1 = "#main-content > div > div > dl > div:nth-child(1) > dt"
    val summaryListItem2 = "#main-content > div > div > dl > div:nth-child(2) > dt"
    val summaryListItem3 = "#main-content > div > div > dl > div:nth-child(3) > dt"
    val summaryListItem4 = "#main-content > div > div > dl > div:nth-child(4) > dt"
    val summaryListItem5 = "#main-content > div > div > dl > div:nth-child(5) > dt"
    val summaryListItem6 = "#main-content > div > div > dl > div:nth-child(6) > dt"
    val summaryListItem7 = "#main-content > div > div > dl > div:nth-child(7) > dt"
    val summaryListItem8 = "#main-content > div > div > dl > div:nth-child(8) > dt"
    val summaryListItem9 = "#main-content > div > div > dl > div:nth-child(9) > dt"
    val summaryListChangeLink1 = "#customer-reference"
    val summaryListChangeLink2 = "#policy-event"
    val summaryListChangeLink3 = "#gain-status"
    val summaryListChangeLink4 = "#policy-held"
    val summaryListChangeLink5 = "#gains-amount"
    val summaryListChangeLink6 = "#paid-tax-status"
    val summaryListChangeLink8 = "#deficiency-relief"
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
    val expectedSummaryListChangeLink8: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  trait SpecificExpectedResults {
  }
  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Policy summary"
    override val expectedHeading: String = "Policy summary"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedSummaryListItem1: String = "Customer reference"
    override val expectedSummaryListItem2: String = "Policy event"
    override val expectedSummaryListItem3: String = "Previous gain from this policy"
    override val expectedSummaryListItem4: String = "Policy held for"
    override val expectedSummaryListItem5: String = "Amount of gain made"
    override val expectedSummaryListItem6: String = "Tax paid on gain"
    override val expectedSummaryListItem7: String = "Amount of tax paid"
    override val expectedSummaryListItem8: String = "Deficiency relief"
    override val expectedSummaryListItem9: String = "Amount of relief available"
    override val expectedSummaryListChangeLink1: String = "Change Change your customer reference"
    override val expectedSummaryListChangeLink2: String = "Change Change the policy event"
    override val expectedSummaryListChangeLink3: String = "Change Change the previous gain from this policy"
    override val expectedSummaryListChangeLink4: String = "Change Change the number of years you held this policy"
    override val expectedSummaryListChangeLink5: String = "Change Change the amount of gain made"
    override val expectedSummaryListChangeLink6: String = "Change Change the amount of tax paid"
    override val expectedSummaryListChangeLink8: String = "Change Change the deficiency relief"
    override val expectedButtonText: String = "Save and continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Crynodeb o’r polisi"
    override val expectedHeading: String = "Crynodeb o’r polisi"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedSummaryListItem1: String = "Cyfeirnod y cwsmer"
    override val expectedSummaryListItem2: String = "Digwyddiad polisi"
    override val expectedSummaryListItem3: String = "Enillion blaenorol o’r polisi hwn"
    override val expectedSummaryListItem4: String = "Cyfnod y daliwyd y polisi"
    override val expectedSummaryListItem5: String = "Swm yr enillion a wnaed"
    override val expectedSummaryListItem6: String = "Treth a dalwyd ar yr enillion"
    override val expectedSummaryListItem7: String = "Swm y dreth a dalwyd"
    override val expectedSummaryListItem8: String = "Rhyddhad am ddiffyg"
    override val expectedSummaryListItem9: String = "Swm y rhyddhad sydd ar gael"
    override val expectedSummaryListChangeLink1: String = "Newid Newid eich cyfeirnod cwsmer"
    override val expectedSummaryListChangeLink2: String = "Newid Newid y digwyddiad polisi"
    override val expectedSummaryListChangeLink3: String = "Newid Newid yr enillion blaenorol o’r polisi hwn"
    override val expectedSummaryListChangeLink4: String = "Newid Newid nifer y blynyddoedd yr ydych wedi dal y polisi hwn"
    override val expectedSummaryListChangeLink5: String = "Newid Newid swm yr enillion a wnaed"
    override val expectedSummaryListChangeLink6: String = "Newid Newid swm y dreth a dalwyd"
    override val expectedSummaryListChangeLink8: String = "Newid Newid y rhyddhad am ddiffyg"
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

        implicit val document: Document = Jsoup.parse(page(taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem1, Selectors.summaryListItem1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem2, Selectors.summaryListItem2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem3, Selectors.summaryListItem3)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem4, Selectors.summaryListItem4)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem5, Selectors.summaryListItem5)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem6, Selectors.summaryListItem6)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem7, Selectors.summaryListItem7)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem8, Selectors.summaryListItem8)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem9, Selectors.summaryListItem9)

        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink1, Selectors.summaryListChangeLink1, customerReferencePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink2, Selectors.summaryListChangeLink2, policyEventPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink3, Selectors.summaryListChangeLink3, gainsStatusPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink4, Selectors.summaryListChangeLink4, policyHeldPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink5, Selectors.summaryListChangeLink5, gainsAmountPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink6, Selectors.summaryListChangeLink6, taxPaidOnGainPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink8, Selectors.summaryListChangeLink8, deficiencyUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }





}
