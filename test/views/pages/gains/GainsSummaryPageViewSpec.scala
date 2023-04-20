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
import views.html.pages.gains.GainsSummaryPageView

import java.util.UUID

class GainsSummaryPageViewSpec extends ViewUnitTest {

  private val page: GainsSummaryPageView = inject[GainsSummaryPageView]
  private val sessionId: String = UUID.randomUUID().toString

  object Selectors {
    val title = "#main-content > div > div > h1"
    val summaryListKey1 = "#main-content > div > div > dl:nth-child(2) > div:nth-child(1) > dt"
    val summaryListValue1 = "#main-content > div > div > dl:nth-child(2) > div:nth-child(1) > dd.govuk-summary-list__value"
    val summaryListAction1 = "#main-content > div > div > dl:nth-child(2) > div:nth-child(1) > dd.govuk-summary-list__actions > a"
    val button = "#main-content > div > div > a"
  }

  trait SpecificExpectedResults {
    val expectedTitle: String
  }

  trait CommonExpectedResults {
    val expectedCaption: Int => String
    val expectedSummaryListKey1: String
    val expectedSummaryListValue1: String
    val expectedSummaryListValue2: String
    val expectedSummaryListValue3: String
    val expectedSummaryListValue4: String
    val expectedSummaryListValue5: String
    val expectedSummaryListAction1: String
    val expectedButtonText: String
  }

  object CommonExpectedEN extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) => s"Gains from life insurance policies and contracts for 6 April ${taxYear - 1} to 5 April $taxYear"
    override val expectedSummaryListKey1: String = "CONTRACT123XYZ"
    override val expectedSummaryListValue1: String = "Life Insurance"
    override val expectedSummaryListValue2: String = "Life Annuity"
    override val expectedSummaryListValue3: String = "Capital Redemption"
    override val expectedSummaryListValue4: String = "Voided ISA - a policy cancelled by your ISA manager"
    override val expectedSummaryListValue5: String = "A foreign policy"
    override val expectedSummaryListAction1: String = "Change Change the policy details Remove Remove this policy"
    override val expectedButtonText: String = "Return to overview"
  }

  object CommonExpectedCY extends CommonExpectedResults {
    override val expectedCaption: Int => String = (taxYear: Int) =>  s"Enillion o bolisïau yswiriant bywyd a chontractau ar gyfer 6 Ebrill ${taxYear - 1} i 5 Ebrill $taxYear"
    override val expectedSummaryListKey1: String = "CONTRACT123XYZ"
    override val expectedSummaryListValue1: String = "Yswiriant Bywyd"
    override val expectedSummaryListValue2: String = "Blwydd-dal Bywyd"
    override val expectedSummaryListValue3: String = "Adbrynu Cyfalaf"
    override val expectedSummaryListValue4: String = "ISA sydd wedi’i ddirymu – polisi a ganslodd rheolwr eich ISA"
    override val expectedSummaryListValue5: String = "Polisi tramor"
    override val expectedSummaryListAction1: String = "Newid Newid y math o bolisi Tynnu Tynnu’r polisi hwn"
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

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq[PolicyCyaModel](PolicyCyaModel(sessionId, "Life Insurance", Some("CONTRACT123XYZ")))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey1, Selectors.summaryListKey1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListValue1, Selectors.summaryListValue1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListAction1, Selectors.summaryListAction1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.button)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render the gains summary page with life annuity" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq[PolicyCyaModel](PolicyCyaModel(sessionId, "Life Annuity", Some("CONTRACT123XYZ")))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey1, Selectors.summaryListKey1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListValue2, Selectors.summaryListValue1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListAction1, Selectors.summaryListAction1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.button)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render the gains summary page with capital redemption" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq[PolicyCyaModel](PolicyCyaModel(sessionId, "Capital Redemption", Some("CONTRACT123XYZ")))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey1, Selectors.summaryListKey1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListValue3, Selectors.summaryListValue1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListAction1, Selectors.summaryListAction1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.button)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render the gains summary page with voided isa" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq[PolicyCyaModel](PolicyCyaModel(sessionId, "Voided ISA", Some("CONTRACT123XYZ")))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey1, Selectors.summaryListKey1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListValue4, Selectors.summaryListValue1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListAction1, Selectors.summaryListAction1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.button)
      }
    }
  }

  userScenarios.foreach { userScenario =>
    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" should {
      "render the gains summary page with foreign policy" which {
        implicit val userPriorDataRequest: AuthorisationRequest[AnyContent] = getAuthRequest(userScenario.isAgent)
        implicit val messages: Messages = getMessages(userScenario.isWelsh)

        implicit val document: Document = Jsoup.parse(page(taxYear, Seq[PolicyCyaModel](PolicyCyaModel(sessionId, "Foreign Policy", Some("CONTRACT123XYZ")))).body)

        welshToggleCheck(userScenario.isWelsh)
        titleCheck(userScenario.specificExpectedResults.get.expectedTitle, userScenario.isWelsh)
        captionCheck(userScenario.commonExpectedResults.expectedCaption(taxYear))
        h1Check(userScenario.specificExpectedResults.get.expectedTitle)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListKey1, Selectors.summaryListKey1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListValue5, Selectors.summaryListValue1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedSummaryListAction1, Selectors.summaryListAction1)
        textOnPageCheck(userScenario.commonExpectedResults.expectedButtonText, Selectors.button)
      }
    }
  }
}
