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
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Environment, Mode}
import test.support.IntegrationTest

class PostCessationTradeReliefControllerISpec extends IntegrationTest {

  lazy val application: Application = GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build()

  private def url(taxYear: Int): String =
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/business-reliefs/post-cessation-trade-relief/relief-claimed"

  ".show" when {

    "user is an Agent" should {

      "render the Post-Cessation Trade Relief amount page" in {

        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(PostCessationTradeReliefMessages.EnglishAgent.headingAndTitle)
      }
    }

    "user is an Individual" should {

      "render the Post-Cessation Trade Relief amount page" in {

        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(PostCessationTradeReliefMessages.English.headingAndTitle)
      }
    }
  }

  ".submit" should {

    "user is an Agent" should {

      "valid data is submitted" should {

        "redirect to the Check Your Answers page" in {

          authoriseAgentOrIndividual(isAgent = true)

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody(PostCessationTradeReliefForm.key -> "1000.00")

          val result = route(application, request).value

          //TODO: In future story, this will be updated to redirect to the CYA page
          status(result) mustEqual NOT_IMPLEMENTED
        }
      }

      "invalid data is submitted" should {

        "render the page as a bad request" in {

          authoriseAgentOrIndividual(isAgent = true)

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

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody(PostCessationTradeReliefForm.key -> "1000.00")

          val result = route(application, request).value

          //TODO: In future story, this will be updated to redirect to the CYA page
          status(result) mustEqual NOT_IMPLEMENTED
        }
      }

      "invalid data is submitted" should {

        "render the page as a bad request" in {

          authoriseAgentOrIndividual(isAgent = true)

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
