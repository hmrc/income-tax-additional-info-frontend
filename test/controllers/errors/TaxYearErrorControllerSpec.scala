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

package controllers.errors

import models.authorisation.SessionValues._
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentType, status}
import support.ControllerUnitTest
import support.builders.UserBuilder.aUser
import support.mocks.{MockActionsProvider, MockAuthorisedAction}
import views.html.templates.TaxYearErrorTemplate

class TaxYearErrorControllerSpec extends ControllerUnitTest
  with MockAuthorisedAction
  with MockActionsProvider {

  private val underTest = new TaxYearErrorController(mockAuthorisedAction, inject[TaxYearErrorTemplate])

  ".show" should {
    "return successful response for individual" in {
      val fakeRequest = FakeRequest("GET", "/error/wrong-tax-year")
        .withHeaders(newHeaders = "X-Session-ID" -> aUser.sessionId)
        .withSession(VALID_TAX_YEARS -> validTaxYearList.mkString(","))

      mockAuthAsIndividual(Some(aUser.nino))

      val result = underTest.show().apply(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }

    "return an OK response when .show() is called and user is authenticated as an agent" in {
      mockAuthAsAgent()

      val fakeRequest = FakeRequest("GET", "/error/wrong-tax-year")
        .withHeaders("X-Session-ID" -> aUser.sessionId)
        .withSession(
          CLIENT_MTDITID -> "1234567890",
          CLIENT_NINO -> "AA123456A",
          VALID_TAX_YEARS -> validTaxYearList.mkString(",")
        )

      val result = underTest.show()(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }

    "return a SEE_OTHER response when .show() is called and user isn't authenticated" in {
      mockFailToAuthenticate()

      val fakeRequest = FakeRequest("GET", "/error/wrong-tax-year")
        .withHeaders(newHeaders = "X-Session-ID" -> aUser.sessionId)
        .withSession(VALID_TAX_YEARS -> validTaxYearList.mkString(","))

      val result = underTest.show()(fakeRequest)

      status(result) shouldBe SEE_OTHER
      await(result).header.headers.getOrElse("Location", "/") shouldBe "/update-and-submit-income-tax-return/additional-information/error/not-authorised-to-use-service"
    }
  }
}
