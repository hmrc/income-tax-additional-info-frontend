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

package controllers

import models.authorisation.SessionValues
import play.api.http.Status.SEE_OTHER
import play.api.test.Helpers.{REFERER, redirectLocation, status}
import support.ControllerUnitTest

class LanguageSwitchControllerSpec extends ControllerUnitTest {

  private val underTest = new LanguageSwitchController()

  ".switchToLanguage" should {
    "return a redirect result when taxYear provided and no referer" in {
      val request = fakeRequest.withSession(SessionValues.TAX_YEAR -> taxYear.toString)
      val result = underTest.switchToLanguage("en").apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(s"/income-tax-submission-base-url/$taxYear/start")
    }

    "return a redirect result when no taxYear provided and no referer" in {
      val result = underTest.switchToLanguage("en").apply(fakeRequest)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(s"/income-tax-submission-base-url/$taxYearEOY/start")
    }

    "return a redirect result when there is referer and taxYear" in {
      val request = fakeRequest.withHeaders(REFERER -> "/some-referer")
      val result = underTest.switchToLanguage("en").apply(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(s"/some-referer")
    }
  }
}
