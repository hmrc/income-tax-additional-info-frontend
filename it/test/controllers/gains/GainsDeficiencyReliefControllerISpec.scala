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

import forms.RadioButtonAmountForm
import models.AllGainsSessionModel
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import test.support.IntegrationTest

class GainsDeficiencyReliefControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/deficiency-relief-status/$sessionId"
  }

  ".show" should {
    "render the paid tax status page" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }
      result.status shouldBe OK
    }

    "render the paid tax status page for an agent" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }
      result.status shouldBe OK
    }

    "render the deficiency relief page with pre-filled data 1" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = Some(true), deficiencyReliefAmount = Some(BigDecimal(123.45)))), gateway = Some(true)))
        )

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
      result.body.contains("Yes")
    }

    "render the deficiency relief page with pre-filled data 2" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(
              Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = Some(true), deficiencyReliefAmount = None)), gateway = Some(true))
          )
        )

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
      result.body.contains("Yes")
    }

    "render the deficiency relief page without prefilled data" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(
              Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = None, deficiencyReliefAmount = None)), gateway = Some(true))
          )
        )

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
      result.body.contains("Yes")
    }

    "return an internal server error" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear) + "bad-session", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to income tax submission overview page if no session data is found" in {
      lazy val result: WSResponse = {
        getSessionDataStub(status = NO_CONTENT)
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }
  }

  ".submit" should {
    "show page with error text if no radio is selected" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe BAD_REQUEST
    }

    "show page with error text if form is invalid" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe BAD_REQUEST
    }

    "redirect to summary page if successful" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        updateSession()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(RadioButtonAmountForm.yesNo -> "true", RadioButtonAmountForm.amount -> "100"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
    }
  }
}
