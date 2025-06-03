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

package controllers.gains

import forms.RadioButtonYearForm.yesNo
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
import play.api.libs.ws.WSResponse
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, writeableOf_AnyContentAsEmpty, writeableOf_AnyContentAsFormUrlEncoded}
import play.api.{Environment, Mode}
import repositories.GainsUserDataRepository
import support.IntegrationTest
import uk.gov.hmrc.http.HttpVerbs.POST

import scala.concurrent.Future

class GainsStatusControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-status/$sessionId"
  }

  ".show" should {
    "render the gains status page for individual user" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
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
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub()

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) mustEqual OK
      }
    }

    "render the gains status page for an agent" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
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
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub()

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) mustEqual OK
      }
    }

    "render the gains status with pre-filled data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(yearsPolicyHeldPrevious = Some(1))))
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Have you had a gain from this policy in an earlier tax year?")
        content should include("Yes")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(amountOfGain = Some(123.45))), gateway = Some(true)))
        )

      running(applicationWithBackendMongo) {
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Have you had a gain from this policy in an earlier tax year?")
        content should include("Yes")
      }

    }

    "render the gains status without pre-filled data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Have you had a gain from this policy in an earlier tax year?")
        content should include("Yes")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(previousGain = None)), gateway = Some(true))))

      running(applicationWithBackendMongo) {
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Have you had a gain from this policy in an earlier tax year?")
        content should include("Yes")
      }
    }

    "return an internal server error" in {
      val mockRepo = mock[GainsUserDataRepository]

      when(mockRepo.find(any())(any())).thenReturn(Future.successful(Left(DataNotFound)))

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
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
        .configure(config ++ Map(backendSessionEnabled -> "true"))
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
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(GET, url(taxYear) + "bad-session").withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(status = NO_CONTENT)

        val request = FakeRequest(GET, url(taxYear) + "bad-session").withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
      }
    }
  }

  ".submit" should {
    "redirect to policy held previous page if successful" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(yesNo -> "true")

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-held-previous/$sessionId")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(amountOfGain = None)), gateway = Some(true))))

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession(responseBody = Json.toJson(updatedGainsUserDataModel).toString())
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(yesNo -> "true")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-held-previous/$sessionId")
      }
    }

    "show page with error text if form is empty" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
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
        .configure(config ++ Map(backendSessionEnabled -> "true"))
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

    "redirect to summary when model is full if successful" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(yesNo -> "false")

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(previousGain = Some(false))), gateway = Some(true))))

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession(responseBody = Json.toJson(Some(gainsUserDataModel)).toString())
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(yesNo -> "false")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId")
      }
    }

    "redirect to policy held page if successful" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(yesNo -> "false")

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-held/$sessionId")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyNumber = None)), gateway = Some(true))))

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession(responseBody = Json.toJson(updatedGainsUserDataModel).toString())
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(yesNo -> "false")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-held/$sessionId")
      }


      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(yesNo -> "false"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-held/${sessionId}"
    }
  }
}
