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

import forms.gains.{InputFieldForm, InputYearForm}
import models.gains.LifeInsuranceModel
import models.gains.prior.GainsPriorDataModel
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import support.IntegrationTest


class PolicyNameControllerISpec extends IntegrationTest {

  clearSession()
  populateSessionData()
  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-name/$sessionId"
  }

  ".show" should {

    "render the customer reference page" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(Some(GainsPriorDataModel("", Some(Seq(LifeInsuranceModel(customerReference = Some("abc123"), gainAmount = BigDecimal(123.45)))))), nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the paid tax status page for an agent" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }
      result.status shouldBe OK
    }

    "render the policy number page with pre-filled data" in {
      clearSession()
      populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyNumber= Some("abc123"))))
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
      result.body.contains("abc123")
    }

    "return an internal server error" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear) + "bad-session", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe 500
    }

    "redirect to income tax submission overview page if no session data is found" in {
      clearSession()
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }
  }

  ".submit" should {
    "redirect to gains amount page if successful" in {
      clearSession()
      populateSessionData()
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(InputFieldForm.value -> "mixedAlphaNumOnly1"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-amount/$sessionId"
    }

    "redirect to summary when model is full if successful" in {
      clearSession()
      populateWithSessionDataModel(Seq(completePolicyCyaModel))
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(InputFieldForm.value -> "mixedAlphaNumOnly1"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/${sessionId}"
    }

    "show page with error text if form is invalid" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe BAD_REQUEST
    }

    "show page with error text if input is the wrong format" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String](
          "value" -> "!@£"
        ))
      }

      result.status shouldBe BAD_REQUEST
    }

    "return an internal server error when session is invalid" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear) + "bad-session", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(InputFieldForm.value -> "mixedAlphaNumOnly1"))
      }

      result.status shouldBe 500
    }

    "Redirect to policy summary page when no session data exists" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(InputFieldForm.value -> "mixedAlphaNumOnly1"))
      }

      result.status shouldBe SEE_OTHER
    }
  }
}
