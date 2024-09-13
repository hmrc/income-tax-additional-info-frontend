/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.gainsBase

import forms.gains.InputFieldForm
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Environment, Mode}
import test.support.IntegrationTest

class PolicyNameBaseControllerISpec extends IntegrationTest {

  val url: String = controllers.gainsBase.routes.PolicyNameBaseController.show(taxYear, sessionId, Some("Life Insurance")).url
  val headers: Seq[(String, String)] = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

  "PolicyNameBaseController.show" should {
    "direct to the original policy name controller when 'split-gains' is false" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> false))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        populateSessionData()

        val request = FakeRequest(GET, url).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "direct to the new policy name controller when 'split-gains' is true" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()

        val request = FakeRequest(GET, url).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }
  }

  "PolicyNameBaseController.submit" should {
    "direct to the original policy name controller when 'split-gains' is false" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> false))
        .build()

      running(application) {

        authoriseIndividual()

        clearSession()
        populateSessionData()

        val request = FakeRequest(POST, url).withHeaders(headers: _*).withFormUrlEncodedBody(InputFieldForm.value -> "mixedAlphaNumOnly1")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "direct to the new policy name controller when 'split-gains' is true" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()

        clearSession()
        populateSessionData()

        val request = FakeRequest(POST, url).withHeaders(headers: _*).withFormUrlEncodedBody(InputFieldForm.value -> "mixedAlphaNumOnly1")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
