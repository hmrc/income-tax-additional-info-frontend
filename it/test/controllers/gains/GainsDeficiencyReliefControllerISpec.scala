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

import forms.RadioButtonAmountForm
import models.AllGainsSessionModel
import models.gains.LifeInsuranceModel
import models.gains.prior.GainsPriorDataModel
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
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, contentAsString, route, running, writeableOf_AnyContentAsEmpty, writeableOf_AnyContentAsFormUrlEncoded}
import play.api.{Environment, Mode}
import repositories.GainsUserDataRepository
import support.IntegrationTest
import uk.gov.hmrc.http.HttpVerbs.POST

import scala.concurrent.Future

class GainsDeficiencyReliefControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/deficiency-relief-status/$sessionId"
  }

  ".show" should {
    "render the paid tax status page" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(Some(GainsPriorDataModel("", Some(Seq(LifeInsuranceModel(customerReference = Some("abc123"), gainAmount = BigDecimal(123.45)))))), nino, taxYear)

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
        userDataStub(Some(GainsPriorDataModel("", Some(Seq(LifeInsuranceModel(customerReference = Some("abc123"), gainAmount = BigDecimal(123.45)))))), nino, taxYear)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) mustEqual OK
      }
    }

    "render the paid tax status page for an agent" in {
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

    "render the deficiency relief page with pre-filled data 1" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = Some(true), deficiencyReliefAmount = Some(BigDecimal(123.45)))))
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Is your client entitled to Deficiency relief?")
        content should include("Yes")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = Some(true), deficiencyReliefAmount = Some(BigDecimal(123.45)))), gateway = Some(true)))
        )

      running(applicationWithBackendMongo) {
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(yearsPolicyHeldPrevious = None)))
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Is your client entitled to Deficiency relief?")
        content should include("Yes")
      }
    }

    "render the deficiency relief page with pre-filled data 2" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = Some(true), deficiencyReliefAmount = Some(BigDecimal(123.45)))))
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Is your client entitled to Deficiency relief?")
        content should include("Yes")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = Some(true), deficiencyReliefAmount = Some(BigDecimal(234.56)))), gateway = Some(true))
          )
        )

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Is your client entitled to Deficiency relief?")
        content should include("Yes")
      }
    }

    "render the deficiency relief page without prefilled data" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = None, deficiencyReliefAmount = None)))
        authoriseAgentOrIndividual(isAgent = true)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Is your client entitled to Deficiency relief?")
        content should include("Yes")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = None, deficiencyReliefAmount = None)), gateway = Some(true))
          )
        )

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = true)
        getSessionDataStub(Some(updatedGainsUserDataModel))

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value
        val content = contentAsString(result)

        status(result) shouldBe OK
        content should include("Is your client entitled to Deficiency relief?")
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

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
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

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
      }
    }
  }

  ".submit" should {
    "show page with error text if no radio is selected" in {
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

    "show page with error text if form is invalid" in {
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

    "redirect to summary page if successful" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "false"))
        .build()

      running(application) {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        userDataStub(gainsPriorDataModel, nino, taxYear)
        authoriseAgentOrIndividual(isAgent = false)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(RadioButtonAmountForm.yesNo -> "true", RadioButtonAmountForm.amount -> "100")

        val result = route(application, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId")
      }

      val applicationWithBackendMongo = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Map(backendSessionEnabled -> "true"))
        .build()

      running(applicationWithBackendMongo) {
        authoriseAgentOrIndividual(isAgent = false)
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        getSessionDataStub()
        updateSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(RadioButtonAmountForm.yesNo -> "true", RadioButtonAmountForm.amount -> "100")

        val result = route(applicationWithBackendMongo, request).value

        status(result) shouldBe SEE_OTHER
        headerStatus(result).headers.get("Location") shouldBe Some(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId")
      }
    }
  }
}
