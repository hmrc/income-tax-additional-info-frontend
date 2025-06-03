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

package controllers.businessTaxReliefs

import fixtures.messages.businessTaxReliefs.PostCessationTradeReliefMessages
import forms.businessTaxReliefs.PostCessationTradeReliefForm
import models.{BusinessTaxReliefs, UserAnswersModel}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import pages.businessTaxReliefs.PostCessationTradeReliefPage
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Environment, Mode}
import support.stubs.UserAnswersStub
import support.IntegrationTest

class PostCessationTradeReliefControllerISpec extends IntegrationTest with UserAnswersStub {

  lazy val application: Application = GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build()

  private def url(taxYear: Int): String =
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/business-reliefs/post-cessation-trade-relief/relief-claimed"

  lazy val userAnswers: UserAnswersModel = emptyUserAnswers(taxYear, BusinessTaxReliefs)

  ".show" when {

    "user is an Agent" should {

      "render the Post-Cessation Trade Relief amount page (no previously saved value)" in {

        authoriseAgentOrIndividual(isAgent = true)
        stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(PostCessationTradeReliefMessages.EnglishAgent.headingAndTitle)
      }
    }

    "user is an Individual" should {

      "render the Post-Cessation Trade Relief amount page (previously saved value pre-popped)" in {

        authoriseAgentOrIndividual(isAgent = false)
        stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers.set(PostCessationTradeReliefPage, BigDecimal(141.44)))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(PostCessationTradeReliefMessages.English.headingAndTitle)
        contentAsString(result) must include("141.44")
      }
    }
  }

  ".submit" should {

    "user is an Agent" should {

      "valid data is submitted" should {

        "redirect to the Check Your Answers page" in {

          authoriseAgentOrIndividual(isAgent = true)
          stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)
          stubStoreUserAnswers()

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody(PostCessationTradeReliefForm.key -> "1000.00")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(routes.CheckAnswersPostCessationTradeReliefController.show(taxYear).url)
        }
      }

      "invalid data is submitted" should {

        "render the page as a bad request" in {

          authoriseAgentOrIndividual(isAgent = true)
          stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody(PostCessationTradeReliefForm.key -> "")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include(PostCessationTradeReliefMessages.EnglishAgent.amountEmpty)
        }
      }
    }

    "user is an Individual" should {

      "valid data is submitted" should {

        "redirect to the Check Your Answers page" in {

          authoriseAgentOrIndividual(isAgent = false)
          stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)
          stubStoreUserAnswers()

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody(PostCessationTradeReliefForm.key -> "1000.00")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result) mustBe Some(routes.CheckAnswersPostCessationTradeReliefController.show(taxYear).url)
        }
      }

      "invalid data is submitted" should {

        "render the page as a bad request" in {

          authoriseAgentOrIndividual(isAgent = true)
          stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody(PostCessationTradeReliefForm.key -> "")

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) must include(PostCessationTradeReliefMessages.EnglishAgent.amountEmpty)
        }
      }
    }
  }
}
