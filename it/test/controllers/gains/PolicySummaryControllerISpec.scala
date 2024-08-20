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

import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import models.mongo.DataNotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.HeaderNames
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK, SEE_OTHER}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, route, running, writeableOf_AnyContentAsEmpty, writeableOf_AnyContentAsFormUrlEncoded}
import play.api.{Environment, Mode}
import repositories.GainsUserDataRepository
import test.support.IntegrationTest
import uk.gov.hmrc.http.HttpVerbs.POST

import java.util.UUID
import scala.concurrent.Future

class PolicySummaryControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
  }

  ".show" should {

    "render the policy summary page" in {
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

    "return internal server error when trying to get user data" in {
      val mockRepo = mock[GainsUserDataRepository]

      when(mockRepo.find(any())(any())).thenReturn(Future.successful(Left(DataNotFound)))

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(application) {
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "render the policy summary page for an agent" in {
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

    "render an empty summary page" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Gain on a UK policy or contract")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Gain on a UK policy or contract")
      }
    }

    "render the page with prior and cya data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)

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
        updateSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) mustEqual OK
      }
    }

    "redirect to submission overview page when cya has no gains data" in {
      val mockRepo = mock[GainsUserDataRepository]

      when(mockRepo.find(any())(any())).thenReturn(Future.successful(Right(Some(gainsUserDataModel.copy(gains = None)))))

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(application) {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(applicationWithBackendMongo) {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))
      }
    }

    "redirect to policy name page with incomplete cya data with policy type as Life insurance" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = true)
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId, policyType = Some("Life Insurance"), previousGain=Some(true), entitledToDeficiencyRelief = Some(true))))
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(yearsPolicyHeld = None)), gateway = Some(true))))

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "redirect to policy name page with incomplete cya data with previous gain and entitledToDeficiencyRelief as false" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId, policyType = Some("Life Insurance"), previousGain = Some(false), entitledToDeficiencyRelief = Some(false))))
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(
              Seq(completePolicyCyaModel.copy(previousGain = Some(false), yearsPolicyHeld = None, entitledToDeficiencyRelief = Some(false))), gateway = Some(true))
          )
        )

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "redirect to policy name page with incomplete cya data with policy type as Voided ISA" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId, policyType = Some("Voided ISA"), previousGain = Some(true), entitledToDeficiencyRelief = Some(true))))
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyType = Some("Voided ISA"), yearsPolicyHeld = None)), gateway = Some(true)))
        )

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "render the overview page when no prior data and session data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(status = NO_CONTENT)
        emptyUserDataStub()

        val request = FakeRequest(GET, url(taxYear) + "bad-session").withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "render the overview page when user clicks back and session id not matches" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateSessionDataWithRandomSession()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear) + "bad-session").withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(sessionId = s"sessionId-${UUID.randomUUID().toString}")), gateway = Some(true)))
        )

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(taxYear) + "bad-session").withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

  }

  ".submit" should {

    "redirect to the gains summary page when it has prior data and same policy reference number" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyNumber = Some("abc123"))))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        submitGains()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }
    }

    "redirect to the gains summary page when it has prior data with no active session" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }
    }

    "redirect to the gains summary page when no prior data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        getSessionDataStub()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }
    }

    "redirect to error page when there is a problem posting data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        submitGains(status = INTERNAL_SERVER_ERROR)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
          clearSession()
          populateWithSessionDataModel(Seq(completePolicyCyaModel))
          authoriseAgentOrIndividual(isAgent = false)
          userDataStub(gainsPriorDataModel, nino, taxYear)
          submitGains(status = INTERNAL_SERVER_ERROR)

      val request = FakeRequest(POST, url(taxYear))
        .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
        .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to overview after submission" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        postExcludeJourney()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        postExcludeJourney()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))
      }
    }

    "return internal server error when gains was not excluded downstream" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        postExcludeJourney(status = INTERNAL_SERVER_ERROR)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        postExcludeJourney(status = INTERNAL_SERVER_ERROR)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "return internal server error when cya data is not found" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "return an internal server error" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
          .withFormUrlEncodedBody("journey" -> "gains")

        val result = route(application, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(status = INTERNAL_SERVER_ERROR)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
          .withFormUrlEncodedBody("journey" -> "gains")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

  }

}
