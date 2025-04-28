/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels.businessTaxReliefs

import fixtures.messages.businessTaxReliefs.BusinessReliefsQualifyingLoanMessages
import fixtures.messages.i18n
import models.BusinessTaxReliefs
import models.requests.JourneyDataRequest
import pages.businessTaxReliefs.QualifyingLoanReliefPage
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import support.ViewUnitTest
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import utils.ViewUtils.bigDecimalCurrency

class CheckAnswersQualifyingLoanSummarySpec extends ViewUnitTest {

  override protected val userScenarios: Seq[UserScenario[BusinessReliefsQualifyingLoanMessages.Messages with i18n, _]] = Seq(
    UserScenario(isWelsh = false, isAgent = false, BusinessReliefsQualifyingLoanMessages.English),
    UserScenario(isWelsh = false, isAgent = true, BusinessReliefsQualifyingLoanMessages.EnglishAgent),
    UserScenario(isWelsh = true, isAgent = false, BusinessReliefsQualifyingLoanMessages.Welsh),
    UserScenario(isWelsh = true, isAgent = true, BusinessReliefsQualifyingLoanMessages.WelshAgent)
  )

  userScenarios.foreach { userScenario =>

    s"language is ${welshTest(userScenario.isWelsh)} and request is from an ${agentTest(userScenario.isAgent)}" when {

      "a user answer exists for the page" should {

        "return SummaryList with correct content" in {

          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val authRequest = getAuthRequest(userScenario.isAgent)
          implicit val request: JourneyDataRequest[AnyContent] = JourneyDataRequest(
            authRequest.user,
            authRequest,
            emptyUserAnswers(taxYear, BusinessTaxReliefs).set(QualifyingLoanReliefPage, BigDecimal("1000.01"))
          )

          CheckAnswersQualifyingLoanSummary.summary(taxYear) shouldBe
            SummaryList(Seq(
              SummaryListRow(
                key = Key(
                  content = Text(userScenario.commonExpectedResults.label),
                  classes = "govuk-!-width-two-thirds"
                ),
                value = Value(
                  content = Text(bigDecimalCurrency("1000.01")),
                  classes = "govuk-!-width-one-third"
                ),
                actions =
                  Some(Actions(items = Seq(
                    ActionItem(
                      href = controllers.businessTaxReliefs.routes.BusinessReliefsQualifyingLoanController.show(taxYear).url,
                      content = Text(userScenario.commonExpectedResults.change),
                      visuallyHiddenText = Some(userScenario.commonExpectedResults.label)
                    )
                  )))
              )
            ))
        }
      }

      "a user answer DOES NOT exist for the page" should {

        "return empty SummaryList" in {

          implicit val messages: Messages = getMessages(userScenario.isWelsh)

          val authRequest = getAuthRequest(userScenario.isAgent)
          implicit val request: JourneyDataRequest[AnyContent] = JourneyDataRequest(
            authRequest.user,
            authRequest,
            emptyUserAnswers(taxYear, BusinessTaxReliefs)
          )

          CheckAnswersQualifyingLoanSummary.summary(taxYear) shouldBe SummaryList(Seq())
        }
      }
    }
  }
}
