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
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.HeaderNames
import play.api.http.Status.{NO_CONTENT, OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, route, running, writeableOf_AnyContentAsEmpty, writeableOf_AnyContentAsFormUrlEncoded}
import play.api.{Environment, Mode}
import test.support.IntegrationTest
import uk.gov.hmrc.http.HttpVerbs.POST

class PoliciesRemoveControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policies-remove-confirmation/$sessionId"
  }

  ".show" should {
    "render the policies remove page" in {
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

    "render the policies remove page for an agent" in {
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

    "redirect to income tax submission overview page if no session data is found" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
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
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
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
    "redirect to gains summary page if successful" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(Seq(completePolicyCyaModel.copy(sessionId = sessionId), completePolicyCyaModel.copy(sessionId = "anotherSession")), gateway = Some(true))
          )
        )

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession(responseBody = Json.toJson(updatedGainsUserDataModel).toString())
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }
    }

    "redirect to gains summary page when cya is empty" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
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
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
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

    "redirect to gains summary page in case of no cya data and no prior data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map("newGainsServiceEnabled" -> "false"))
        .build()

      running(application) {
        clearSession()
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
        .configure(config ++ Map("newGainsServiceEnabled" -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(status = NO_CONTENT)
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody()

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary")
      }
    }
  }

}
