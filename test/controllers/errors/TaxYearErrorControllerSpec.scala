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

import actions.{AuthorisedAction, FakeIdentifyAction}
import models.authorisation.SessionValues._
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentType, status}
import support.ControllerUnitTest


class TaxYearErrorControllerSpec extends ControllerUnitTest {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[AuthorisedAction].to[FakeIdentifyAction])
      .build()

  private lazy val underTest = inject[TaxYearErrorController]

  ".show" should {
    "return successful response" in {
      val fakeRequest = FakeRequest("GET", "/error/wrong-tax-year")
        .withSession(VALID_TAX_YEARS -> validTaxYearList.mkString(","))

      val result = underTest.show().apply(fakeRequest)

      status(result) shouldBe OK
      contentType(result) shouldBe Some("text/html")
    }
  }
}
