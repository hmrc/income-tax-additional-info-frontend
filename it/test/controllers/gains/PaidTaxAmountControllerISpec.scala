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

package test.controllers.gains

import forms.AmountForm
import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import models.mongo.DataNotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, route, running, writeableOf_AnyContentAsEmpty, writeableOf_AnyContentAsFormUrlEncoded}
import play.api.{Environment, Mode}
import repositories.GainsUserDataRepository
import test.support.IntegrationTest
import uk.gov.hmrc.http.HttpVerbs.POST

import scala.concurrent.Future

class PaidTaxAmountControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/paid-tax-amount/$sessionId"
  }

  ".show" should {

    "render the paid tax amount page" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) mustEqual OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub()

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) mustEqual OK
      }
    }

    "render the paid tax amount page for an agent" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) mustEqual OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub()

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) mustEqual OK
      }
    }

    "render the gains amount page with pre-filled data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(taxPaidAmount = Some(123.45))))
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(taxPaidAmount = Some(123.45))), gateway = Some(true))))

      running(applicationWithBackendMongo) {
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

    "return an internal server error" in {
      val mockRepo = mock[GainsUserDataRepository]

      when(mockRepo.find(any())(any())).thenReturn(Future.successful(Left(DataNotFound)))

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(application) {
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(GET, url(taxYear) + "bad-session").withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub()

        val request = FakeRequest(GET, url(taxYear) + "bad-session").withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to income tax submission overview page if no session data is found" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(status = NO_CONTENT)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "redirect to paid tax amount page when cya has no gains data" in {
      val mockRepo = mock[GainsUserDataRepository]

      when(mockRepo.find(any())(any())).thenReturn(Future.successful(Right(Some(gainsUserDataModel.copy(gains = None)))))

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(application) {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(applicationWithBackendMongo) {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

    "redirect to paid tax amount page when tax paid amount is not set" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = true)
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(taxPaidAmount = None)))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = true)
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(taxPaidAmount = None)))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

  }

  ".submit" should {

    "redirect to policy summary if successful" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(AmountForm.amount -> "100")

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = None)), gateway = Some(true))))

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession(responseBody = Json.toJson(updatedGainsUserDataModel).toString())
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(AmountForm.amount -> "100")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId")
      }
    }

    "return internal server error when trying to get user data" in {
      val mockRepo = mock[GainsUserDataRepository]

      when(mockRepo.find(any())(any())).thenReturn(Future.successful(Left(DataNotFound)))

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(application) {
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(AmountForm.amount -> "100")

        val result = route(application, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(AmountForm.amount -> "100")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "show page with error text if form is invalid" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(AmountForm.amount -> "abc")

        val result = route(application, request).value

        status(result) shouldBe BAD_REQUEST
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(AmountForm.amount -> "abc")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe BAD_REQUEST
      }
    }

    "show page with error text if form is empty" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe BAD_REQUEST
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe BAD_REQUEST
      }
    }

    "show page with error text if form value is too high" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(AmountForm.amount -> "10000000000000")

        val result = route(application, request).value

        status(result) shouldBe BAD_REQUEST
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(AmountForm.amount -> "10000000000000")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe BAD_REQUEST
      }
    }
  }

}
