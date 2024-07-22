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
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse
import test.support.IntegrationTest

import java.util.UUID

class GainsAmountControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-amount/$sessionId"
  }

  ".show" should {
    "render the gains amount page" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the gains amount page for an agent" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the gains amount page with pre-filled data" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(amountOfGain = Some(123.45))), gateway = Some(true)))
        )

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the error page when session is not matching" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(sessionId = s"sessionId-${UUID.randomUUID().toString}")), gateway = Some(true)))
        )

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return an internal server error with invalid session " in {
      lazy val result: WSResponse = {
        getSessionDataStub(status = INTERNAL_SERVER_ERROR)
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
    "redirect to policy event page if successful" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = None)), gateway = Some(true))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(AmountForm.amount -> "100"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-event/$sessionId"
    }

    "show page with error text if form is invalid" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(AmountForm.amount -> "abc"))
      }

      result.status shouldBe BAD_REQUEST
    }

    "show page with error text if form is empty" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(AmountForm.amount -> ""))
      }

      result.status shouldBe BAD_REQUEST
    }

    "show page with error text if form value is too high" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(AmountForm.amount -> "10000000000000"))
      }

      result.status shouldBe BAD_REQUEST
    }

    "redirect to summary when model is full if successful" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        updateSession()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(AmountForm.amount -> "100"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
    }

    "redirect to server error page when session not exists" in {
      lazy val result: WSResponse = {
        getSessionDataStub(status = NO_CONTENT)
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map(AmountForm.amount -> "100"))
      }

      result.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
