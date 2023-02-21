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
import views.html.pages.gains.GainsSummaryPageView

class GainsPolicySummaryPageViewSpec extends ViewUnitTest {

  private val page: GainsSummaryPageView = inject[GainsSummaryPageView]

  object Selectors {
    val title = "#main-content > div > div > h1"
    val summaryListKey1 = "#main-content > div > div > dl:nth-child(2) > div:nth-child(1) > dt"
    val summaryListValue1 = "#main-content > div > div > dl:nth-child(2) > div:nth-child(1) > dd.govuk-summary-list__value"
    val summaryListAction1 = "#main-content > div > div > dl:nth-child(2) > div:nth-child(1) > dd.govuk-summary-list__actions > a"
    val summaryListKey2 = "#main-content > div > div > dl:nth-child(2)> div:nth-child(2) > dt"
    val subTitle = "#main-content > div > div > h2"
    val summaryListKey3 = "#main-content > div > div > dl:nth-child(4) > div:nth-child(1) > dt"
    val summaryListKey4 = "#main-content > div > div > dl:nth-child(4) > div:nth-child(2) > dt"
    val button = "#main-content > div > div > a"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedSummaryListKey1: String
    val expectedSummaryListValue1: String
    val expectedSummaryListAction1: String
    val expectedSummaryListKey2: String
    val expectedSummaryListAction2: String
    val expectedSubTitle: String
    val expectedSummaryListKey3: String
    val expectedSummaryListValue2: String
    val expectedSummaryListAction3: String
    val expectedSummaryListKey4: String
    val expectedSummaryListAction4: String
    val expectedButtonText: String
  }

  private val yes = "Yes"
  private val change = "Change"
  private val yesCy = "Iawn"
  private val changeCy = "Newid"

  object CommonExpectedEN extends CommonExpectedResults {

    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedSummaryListKey1: String = "Gain on a UK policy or contract"
    override val expectedSummaryListValue1: String = yes
    override val expectedSummaryListAction1: String = change
    override val expectedSummaryListKey2: String = "LIFE123abc"
    override val expectedSummaryListAction2: String = change
    override val expectedSubTitle: String = "Cancelled policies"
    override val expectedSummaryListKey3: String = "Gain on a cancelled UK policy or contract"
    override val expectedSummaryListValue2: String = yes
    override val expectedSummaryListAction3: String = change
    override val expectedSummaryListKey4: String = "CONTRACT123XYZ"
    override val expectedSummaryListAction4: String = change
    override val expectedButtonText: String = "Return to overview"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) =>  s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedSummaryListKey1: String = "Ennill ar bolisi neu gytundeb y DU"
    override val expectedSummaryListValue1: String = yesCy
    override val expectedSummaryListAction1: String = changeCy
    override val expectedSummaryListKey2: String = "LIFE123abc"
    override val expectedSummaryListAction2: String = changeCy
    override val expectedSubTitle: String = "Polisïau wedi’u canslo"
    override val expectedSummaryListKey3: String = "Ennill ar bolisi neu gytundeb y DU sydd wedi’i ganslo"
    override val expectedSummaryListValue2: String = yesCy
    override val expectedSummaryListAction3: String = changeCy
    override val expectedSummaryListKey4: String = "CONTRACT123XYZ"
    override val expectedSummaryListAction4: String = changeCy
    override val expectedButtonText: String = "Yn ôl i’r trosolwg"
  }

  object ExpectedIndividualEN extends SpecificExpectedResults {
    override val expectedTitle = "Your policies"
  }

  object ExpectedIndividualCY extends SpecificExpectedResults {
    override val expectedTitle = "Eich polisïau"
  }

  object ExpectedAgentEN extends SpecificExpectedResults {
    override val expectedTitle = "Your client's policies"
  }

  object ExpectedAgentCY extends SpecificExpectedResults {
    override val expectedTitle: String = "Polisïau eich cleient"
  }

  override protected val userScenarios: Seq[UserScenario[CommonExpectedResults, SpecificExpectedResults]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, CommonExpectedEN, Some(ExpectedIndividualEN)),
    UserScenario(isWelsh = false, isAgent = true, CommonExpectedEN, Some(ExpectedAgentEN)),
    UserScenario(isWelsh = true, isAgent = false, CommonExpectedCY, Some(ExpectedIndividualCY)),
    UserScenario(isWelsh = true, isAgent = true, CommonExpectedCY, Some(ExpectedAgentCY))
  )

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render the gains summary page" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSubTitle, Selectors.subTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey1, Selectors.summaryListKey1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey2, Selectors.summaryListKey2)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey3, Selectors.summaryListKey3)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey4, Selectors.summaryListKey4)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListValue1, Selectors.summaryListValue1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListAction1, Selectors.summaryListAction1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.button)
      }
    }
  }
}
