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

import models.requests.AuthorisationRequest
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import views.html.pages.gains.PoliciesAddPageView

class PoliciesAddPageViewSpec extends ViewUnitTest {

  private val page: PoliciesAddPageView = inject[PoliciesAddPageView]

  private val customerReferencePageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-name"
  private val policiesRemovePageUrl: String = s""
  private val policySummaryPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary"


  object Selectors {
    val summaryListChangeLink = "#policy-change"
    val summaryListRemoveLink = "#policy-remove"
    val addPolicyLink = "#main-content > div > div > p > a"
    val continueButton = "#continue"
    val getHelpLink = "#help"
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedCaption: Int => String
    val expectedSummaryListChangeLink: String
    val expectedSummaryListRemoveLink: String
    val expectedAddPolicyLink: String
    val expectedButtonText: String
    val expectedHelpLinkText: String
  }

  trait SpecificExpectedResults {
  }
  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Policies"
    override val expectedHeading: String = "Policies"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedSummaryListChangeLink: String = "Change Change the policy details"
    override val expectedSummaryListRemoveLink: String = "Remove Remove the policy"
    override val expectedAddPolicyLink: String = "Add another gain from a different policy"
    override val expectedButtonText: String = "Continue"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Polisïau"
    override val expectedHeading: String = "Polisïau"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedSummaryListChangeLink: String = "Newid Newid manylion y polisi"
    override val expectedSummaryListRemoveLink: String = "Tynnu Tynnu’r polisi"
    override val expectedAddPolicyLink: String = "Ychwanegu ennill arall o bolisi gwahanol"
    override val expectedButtonText: String = "Yn eich blaen"
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
      "render policies add page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)

        linkCheck(userScenario.commonExpectedResults.expectedSummaryListChangeLink, Selectors.summaryListChangeLink, policySummaryPageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedSummaryListRemoveLink, Selectors.summaryListRemoveLink, policiesRemovePageUrl)
        linkCheck(userScenario.commonExpectedResults.expectedAddPolicyLink, Selectors.addPolicyLink, customerReferencePageUrl)

        buttonCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.continueButton)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }





}
