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

class PolicyEventControllerISpec extends IntegrationTest {

  clearSession()

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-event/$sessionId"
  }

  ".show" should {

    "render the policy event page" in {
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

    "render the policy event page for an agent" in {
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

    "render the policy event page with pre-filled data 1" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Full or part surrender"))), gateway = Some(true))))

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Full or part surrender"))))
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Full or part surrender"))))
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

    "render the policy event page with pre-filled data 2" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Policy matured or a death"))), gateway = Some(true))))

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Policy matured or a death"))))
        authoriseAgentOrIndividual(isAgent = true)


        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {

        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Policy matured or a death"))))
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

    "render the policy event page with pre-filled data 3" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Sale or assignment of a policy"))), gateway = Some(true))))

      running(application) {

        authoriseAgentOrIndividual(isAgent = true)
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Sale or assignment of a policy"))))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {

        authoriseAgentOrIndividual(isAgent = true)

        getSessionDataStub(Some(updatedGainsUserDataModel))

        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Sale or assignment of a policy"))))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

    "render the policy event page with pre-filled data 4" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Personal Portfolio Bond"))), gateway = Some(true))))

      running(application) {
        authoriseAgentOrIndividual(isAgent = true)
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Personal Portfolio Bond"))))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Personal Portfolio Bond"))))
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

    "render the policy event page with pre-filled data 5" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Some other"))), gateway = Some(true))))

      running(application) {
        authoriseAgentOrIndividual(isAgent = true)
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Some other"))))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Some other"))))
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

    "render the policy event page with empty policy event value" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        authoriseAgentOrIndividual(isAgent = true)
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyEvent = None)))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(application, request).value

        status(result) shouldBe OK
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val a = gainsUserDataModel.gains.get.copy(allGains = Seq(completePolicyCyaModel.copy(policyType = None)))
      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(userData = Some(gainsUserDataModel.copy(gains = Some(a))))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe OK
      }
    }

    "return an internal server error with bad session value" in {

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


    ".submit" should {
      "redirect to gains status page if successful" in {

        val updatedGainsUserDataModel =
          gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(amountOfGain = None)), gateway = Some(true))))

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
            .withFormUrlEncodedBody("policy-event" -> "Full or part surrender")

          val result = route(application, request).value

          status(result) shouldBe SEE_OTHER
        }

        val applicationWithBackendMongo = GuiceApplicationBuilder()
          .in(Environment.simple(mode = Mode.Dev))
          .configure(config ++ Map(backendSessionEnabled -> "true"))
          .build()

        running(applicationWithBackendMongo) {
          authoriseAgentOrIndividual(isAgent = false)
          getSessionDataStub(userData = Some(updatedGainsUserDataModel))
          updateSession(responseBody = Json.toJson(updatedGainsUserDataModel).toString())
          userDataStub(gainsPriorDataModel, nino, taxYear)

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody("policy-event" -> "Full or part surrender")

          val result = route(applicationWithBackendMongo, request).value

          status(result) shouldBe SEE_OTHER
        }
      }

      "redirect to gains status page if successful with other selected" in {

        val updatedGainsUserDataModel =
          gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(amountOfGain = None)), gateway = Some(true))))

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
            .withFormUrlEncodedBody("policy-event" -> "Other", "other-text" -> "Some other")

          val result = route(application, request).value

          status(result) shouldBe SEE_OTHER
        }

        val applicationWithBackendMongo = GuiceApplicationBuilder()
          .in(Environment.simple(mode = Mode.Dev))
          .configure(config ++ Map(backendSessionEnabled -> "true"))
          .build()

        running(applicationWithBackendMongo) {
          authoriseAgentOrIndividual(isAgent = false)
          getSessionDataStub(userData = Some(updatedGainsUserDataModel))
          updateSession(responseBody = Json.toJson(updatedGainsUserDataModel).toString())
          userDataStub(gainsPriorDataModel, nino, taxYear)

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody("policy-event" -> "Other", "other-text" -> "Some other")

          val result = route(applicationWithBackendMongo, request).value

          status(result) shouldBe SEE_OTHER
        }
      }

      "show page with error text if no selection is made" in {
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

      "show page with error text if the wrong format is entered" in {
        val application = GuiceApplicationBuilder()
          .in(Environment.simple(mode = Mode.Dev))
          .configure(config ++ Map(backendSessionEnabled -> "false"))
          .build()

        running(application) {
          clearSession()
          authoriseAgentOrIndividual(isAgent = false)

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody("policy-event" -> "123 456")

          val result = route(application, request).value

          status(result) shouldBe BAD_REQUEST
        }

        val applicationWithBackendMongo = GuiceApplicationBuilder()
          .in(Environment.simple(mode = Mode.Dev))
          .configure(config ++ Map(backendSessionEnabled -> "true"))
          .build()

        running(applicationWithBackendMongo) {
          authoriseAgentOrIndividual(isAgent = false)

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody("policy-event" -> "123 456")

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
            .withFormUrlEncodedBody("policy-event" -> "Full or part surrender")

          val result = route(application, request).value

          status(result) shouldBe SEE_OTHER
        }

        val applicationWithBackendMongo = GuiceApplicationBuilder()
          .in(Environment.simple(mode = Mode.Dev))
          .configure(config ++ Map(backendSessionEnabled -> "true"))
          .build()

        running(applicationWithBackendMongo) {
          authoriseAgentOrIndividual(isAgent = false)
          getSessionDataStub(userData = Some(gainsUserDataModel))
          updateSession(responseBody = Json.toJson(Some(gainsUserDataModel)).toString())
          userDataStub(gainsPriorDataModel, nino, taxYear)

          val request = FakeRequest(POST, url(taxYear))
            .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
            .withFormUrlEncodedBody("policy-event" -> "Full or part surrender")

          val result = route(applicationWithBackendMongo, request).value

          status(result) shouldBe SEE_OTHER
        }
      }
    }

    "return an internal server error from db error" in {

      val mockRepo = mock[GainsUserDataRepository]

      when(mockRepo.find(any())(any())).thenReturn(Future.successful(Left(DataNotFound)))

      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .overrides(bind[GainsUserDataRepository].to(mockRepo))
        .build()

      running(application) {
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody("policy-event" -> "Full or part surrender")

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

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody("policy-event" -> "Full or part surrender")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
