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
import views.html.pages.gains.PoliciesRemovePageView

import java.util.UUID

class PoliciesRemovePageViewSpec extends ViewUnitTest {

  private val page: PoliciesRemovePageView = inject[PoliciesRemovePageView]
  private val sessionId: String = UUID.randomUUID().toString

  private val policiesAddPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"

  object Selectors {
    val summaryListItem1 = "#main-content > div > div > dl > div:nth-child(1) > dt"
    val summaryListItem2 = "#main-content > div > div > dl > div:nth-child(2) > dt"
    val summaryListItem3 = "#main-content > div > div > dl > div:nth-child(3) > dt"
    val summaryListItem4 = "#main-content > div > div > dl > div:nth-child(4) > dt"
    val summaryListItem5 = "#main-content > div > div > dl > div:nth-child(5) > dt"
    val summaryListItem6 = "#main-content > div > div > dl > div:nth-child(6) > dt"
    val summaryListItem7 = "#main-content > div > div > dl > div:nth-child(7) > dt"
    val summaryListItem8 = "#main-content > div > div > dl > div:nth-child(8) > dt"
    val removeButton = "#remove"
    val dontRemoveLink = "#cancel-remove"
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
    val expectedButtonText: String
    val expectedDontRemoveLinkText: String
    val expectedHelpLinkText: String
  }

  trait SpecificExpectedResults {
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Are you sure you want to remove this policy?"
    override val expectedHeading: String = "Are you sure you want to remove this policy?"
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
    override val expectedButtonText: String = "Remove"
    override val expectedDontRemoveLinkText: String = "Don't remove"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "A ydych yn siŵr eich bod am dynnu’r polisi hwn?"
    override val expectedHeading: String = "A ydych yn siŵr eich bod am dynnu’r polisi hwn?"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedSummaryListItem1: String = "Math o bolisi"
    override val expectedSummaryListItem2: String = "Rhif y polisi"
    override val expectedSummaryListItem3: String = "Swm yr enillion a wnaed"
    override val expectedSummaryListItem4: String = "Digwyddiad polisi"
    override val expectedSummaryListItem5: String = "Nifer y blynyddoedd a ddaliwyd y polisi"
    override val expectedSummaryListItem6: String = "Enillion yn cael eu trin fel treth a dalwyd"
    override val expectedSummaryListItem7: String = "Rhyddhad am ddiffyg"
    override val expectedSummaryListItem8: String = "Swm y rhyddhad sydd ar gael"
    override val expectedSummaryListItem9: String = "Treth a dalwyd ar yr enillion"
    override val expectedButtonText: String = "Tynnu"
    override val expectedDontRemoveLinkText: String = "Paid â thynnu"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY)
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policies remove page with deficiency relief" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, sessionId, PolicyCyaModel(sessionId, "Capital Redemption", policyEvent = Some("Sale or assignment of a policy"), entitledToDeficiencyRelief = Some(true), deficiencyReliefAmount = Some(BigDecimal(123.45)), treatedAsTaxPaid = Some(true))).body)

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

        linkCheck(userScenario.commonExpectedResults.expectedDontRemoveLinkText, Selectors.dontRemoveLink, policiesAddPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.removeButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policies remove page without deficiency relief" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, sessionId, PolicyCyaModel(sessionId, "Life Annuity", policyEvent = Some("Policy matured or a death"), entitledToDeficiencyRelief = Some(false), treatedAsTaxPaid = Some(false))).body)

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

        linkCheck(userScenario.commonExpectedResults.expectedDontRemoveLinkText, Selectors.dontRemoveLink, policiesAddPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.removeButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policies remove page with life insurance and full or part surrender" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, sessionId, PolicyCyaModel(sessionId, "Life Insurance", policyEvent = Some("Full or part surrender"), entitledToDeficiencyRelief = Some(false), treatedAsTaxPaid = Some(false))).body)

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

        linkCheck(userScenario.commonExpectedResults.expectedDontRemoveLinkText, Selectors.dontRemoveLink, policiesAddPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.removeButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policies remove page with foreign policy and ppb" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, sessionId, PolicyCyaModel(sessionId, "Foreign Policy", policyEvent = Some("Personal Portfolio Bond"), entitledToDeficiencyRelief = Some(false), treatedAsTaxPaid = Some(false))).body)

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

        linkCheck(userScenario.commonExpectedResults.expectedDontRemoveLinkText, Selectors.dontRemoveLink, policiesAddPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.removeButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policies remove page with voided isa" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, sessionId, PolicyCyaModel(sessionId, "Voided ISA", policyEvent = Some("Some other event"), entitledToDeficiencyRelief = Some(false), treatedAsTaxPaid = Some(false))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem1, Selectors.summaryListItem1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem2, Selectors.summaryListItem2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem3, Selectors.summaryListItem3)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem4, Selectors.summaryListItem4)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem5, Selectors.summaryListItem5)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListItem9, Selectors.summaryListItem6)


        linkCheck(userScenario.commonExpectedResults.expectedDontRemoveLinkText, Selectors.dontRemoveLink, policiesAddPageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.removeButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }

}
