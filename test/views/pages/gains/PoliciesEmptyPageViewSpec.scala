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
import views.html.pages.gains.PoliciesEmptyPageView

class PoliciesEmptyPageViewSpec extends ViewUnitTest {

  private val page: PoliciesEmptyPageView = inject[PoliciesEmptyPageView]

  private val policiesEmptyPageUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policies-empty"
  private val overviewUrl: String = appConfig.incomeTaxSubmissionOverviewUrl(taxYear);

  object Selectors {
    val paragraph1 = "#main-content > div > div > p:nth-child(2)"
    val paragraph2 = "#main-content > div > div > p:nth-child(4)"
    val button1 = "#continue"
    val button2 = "#return-to-overview-button"
    val getHelpLink = "#help"
  }

  trait CommonExpectedResults {
    val expectedTitle: String
    val expectedHeading: String
    val expectedCaption: Int => String
    val expectedParagraph1: String
    val expectedParagraph2: String
    val expectedButtonText1: String
    val expectedButtonText2: String
    val expectedHelpLinkText: String
  }

  trait SpecificExpectedResults {
  }
  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedTitle: String = "Policies"
    override val expectedHeading: String = "Policies"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedParagraph1: String = "You need to add one or more life insurance policy or contract."
    override val expectedParagraph2: String = "If you don't have a policy to add you can return to the overview page and come back later."
    override val expectedButtonText1: String = "Add a policy"
    override val expectedButtonText2: String = "Return to overview"
    override val expectedHelpLinkText: String = "Get help with this page"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedTitle: String = "Polisïau"
    override val expectedHeading: String = "Polisïau"
    override val expectedCaption: Int => String = (taxYear: Int) => s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedParagraph1: String = "Mae angen i chi ychwanegu un neu fwy o bolisïau yswiriant bywyd neu gontract."
    override val expectedParagraph2: String = "Os nad oes gennych bolisi i’w ychwanegu, gallwch fynd yn ôl i dudalen y trosolwg a dychwelyd yn nes ymlaen."
    override val expectedButtonText1: String = "Ychwanegu polisi"
    override val expectedButtonText2: String = "Yn ôl i’r trosolwg"
    override val expectedHelpLinkText: String = "Help gyda’r dudalen hon"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults,SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY)
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render policies empty page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.commonExpectedResults.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.commonExpectedResults.expectedHeading)
        textOnPageCheck(userScenario.commonExpectedResults.expectedParagraph1, Selectors.paragraph1)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText1, Selectors.button1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedParagraph2, Selectors.paragraph2)
        buttonCheck(userScenario.commonExpectedResults.expectedButtonText2, Selectors.button2)
        linkCheck(userScenario.commonExpectedResults.expectedHelpLinkText, Selectors.getHelpLink, appConfig.contactUrl(userScenario.isAgent))
      }
    }
  }
}