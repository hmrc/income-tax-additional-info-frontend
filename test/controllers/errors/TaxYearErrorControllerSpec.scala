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

package controllers.errors

import models.authorisation.SessionValues.VALID_TAX_YEARS
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentType, status}
import support.ControllerUnitTest
import support.builders.models.UserBuilder.aUser
import support.mocks.MockActionsProvider
import views.html.templates.TaxYearErrorTemplate

class TaxYearErrorControllerSpec extends ControllerUnitTest
  with MockActionsProvider {

  private val underTest = new TaxYearErrorController(mockActionsProvider, inject[TaxYearErrorTemplate])

  ".show" should {
    "return successful response" in {
      val fakeRequest = FakeRequest("GET", "/error/wrong-tax-year")
        .withHeaders(newHeaders = "X-Session-ID" -> aUser.sessionId)
        .withSession(VALID_TAX_YEARS -> validTaxYearList.mkString(","))

      mockAuthorisedAction()

      val result = underTest.show().apply(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }
}
