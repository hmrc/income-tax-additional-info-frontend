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

package controllers

import models.authorisation.SessionValues
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentType, status}
import support.ControllerUnitTest
import views.html.templates.TimeoutPage

class SessionExpiredControllerSpec extends ControllerUnitTest {

  private val underTest = new SessionExpiredController(inject[TimeoutPage])

  ".keepAlive" should {
    "return NoContent" in {
      val result = underTest.keepAlive().apply(fakeRequest)

      status(result) shouldBe NO_CONTENT
    }
  }

  ".timeout" should {
    "return OK when taxYear in session" in {
      val fakeRequest = FakeRequest("GET", "/error/wrong-tax-year")
        .withSession(SessionValues.TAX_YEAR -> taxYearEOY.toString)

      val result = underTest.timeout().apply(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }

    "return OK when no taxYear in session" in {
      val result = underTest.timeout().apply(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }
}
