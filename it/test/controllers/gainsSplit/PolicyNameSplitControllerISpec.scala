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

package controllers.gainsSplit

import forms.gains.InputFieldForm
import models.gains.PolicyCyaModel
import models.mongo.DataNotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.HeaderNames
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.DefaultBodyWritables
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Environment, Mode}
import services.GainsSessionServiceProvider
import support.helpers.ViewHelper
import support.IntegrationTest

import scala.concurrent.Future

class PolicyNameSplitControllerISpec extends IntegrationTest with ViewHelper with DefaultBodyWritables {

  private val LIFE_INSURANCE: String = "Life Insurance"
  private val LIFE_ANNUITY: String = "Life Annuity"
  private val CAPITAL_REDEMPTION: String = "Capital Redemption"
  private val VOIDED_ISA: String = "Voided ISA"

  private def url(policyType: String): String = controllers.gainsBase.routes.PolicyNameBaseController.show(taxYear, sessionId, Some(policyType)).url
  private val headers: Seq[(String, String)] = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

  "PolicyNameSplitController.show" should {
    "render the page with life insurance" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()
        populateSessionData()

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains completePolicyCyaModel.policyNumber.get
      }
    }

    "render the page with life annuity" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()
        populateSessionData()

        val request = FakeRequest(GET, url(LIFE_ANNUITY)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains completePolicyCyaModel.policyNumber.get
      }
    }

    "render the page with capital redemption" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()
        populateSessionData()

        val request = FakeRequest(GET, url(CAPITAL_REDEMPTION)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains completePolicyCyaModel.policyNumber.get
      }
    }

    "render the page with voided isa" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()
        populateSessionData()

        val request = FakeRequest(GET, url(VOIDED_ISA)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains completePolicyCyaModel.policyNumber.get
      }
    }

    "render the page for an agent" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseAgentOrIndividual(isAgent = true)
        clearSession()
        emptyUserDataStub()
        populateSessionData()

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains completePolicyCyaModel.policyNumber.get
      }
    }

    "render the page when no session is defined" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "render the page when session is defined without stock dividend amount" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()
        populateSessionData()

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "return an INTERNAL_SERVER_ERROR when db returns an error" in {
      val mockService = mock[GainsSessionServiceProvider]

      when(mockService.getSessionData(any())(any(), any(), any())).thenReturn(
        Future.successful(Left(DataNotFound))
      )

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .overrides(bind[GainsSessionServiceProvider].toInstance(mockService))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "render the page with a new session when no sessionId matches" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel("badSession", Some(LIFE_INSURANCE))))
        emptyUserDataStub()

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "render the page with an empty model when an invalid param is passed" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()
        populateSessionData()

        val request = FakeRequest(GET, url("invalid")).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }
  }

  "PolicyNameSplitController.submit" should {
    "direct to the cya page when model is full" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()

        clearSession()
        populateSessionData()

        val request = FakeRequest(POST, url(LIFE_INSURANCE)).withHeaders(headers: _*).withFormUrlEncodedBody(InputFieldForm.value -> "mixedAlphaNumOnly1")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId")
      }
    }

    "direct to the next page of the journey" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()

        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId, Some(LIFE_INSURANCE))))

        val request = FakeRequest(POST, url(LIFE_INSURANCE)).withHeaders(headers: _*).withFormUrlEncodedBody(InputFieldForm.value -> "mixedAlphaNumOnly1")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-amount/$sessionId")
      }
    }

    "return BAD_REQUEST with invalid body" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()

        clearSession()
        populateSessionData()

        val request = FakeRequest(POST, url(LIFE_INSURANCE)).withHeaders(headers: _*).withFormUrlEncodedBody("invalid" -> "123")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "return an INTERNAL_SERVER_ERROR when sessionId does not match" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()

        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel("badSession", Some(LIFE_INSURANCE))))

        val request = FakeRequest(POST, url(LIFE_INSURANCE)).withHeaders(headers: _*).withFormUrlEncodedBody(InputFieldForm.value -> "mixedAlphaNumOnly1")

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "return an INTERNAL_SERVER_ERROR when no session exists in db" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()

        clearSession()

        val request = FakeRequest(POST, url(LIFE_INSURANCE)).withHeaders(headers: _*).withFormUrlEncodedBody(InputFieldForm.value -> "mixedAlphaNumOnly1")

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "return an INTERNAL_SERVER_ERROR when db returns an error" in {
      val mockService = mock[GainsSessionServiceProvider]

      when(mockService.getSessionData(any())(any(), any(), any())).thenReturn(
        Future.successful(Left(DataNotFound))
      )

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .overrides(bind[GainsSessionServiceProvider].toInstance(mockService))
        .build()

      running(application) {

        authoriseIndividual()

        clearSession()
        populateSessionData()

        val request = FakeRequest(POST, url(LIFE_INSURANCE)).withHeaders(headers: _*).withFormUrlEncodedBody(InputFieldForm.value -> "mixedAlphaNumOnly1")

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}
