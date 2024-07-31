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
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.ws.WSResponse
import test.support.IntegrationTest

class PolicyEventControllerISpec extends IntegrationTest {

  clearSession()
  populateSessionData()

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-event/$sessionId"
  }

  ".show" should {
    "render the policy event page" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy event page for an agent" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy event page with pre-filled data 1" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Full or part surrender"))), gateway = Some(true))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy event page with pre-filled data 2" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Policy matured or a death"))), gateway = Some(true))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy event page with pre-filled data 3" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Policy matured or a death"))), gateway = Some(true))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy event page with pre-filled data 4" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Personal Portfolio Bond"))), gateway = Some(true))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy event page with pre-filled data 5" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = Some("Some other"))), gateway = Some(true))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy event page without pre-filled data" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(policyEvent = None)), gateway = Some(true))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "return an internal server error with bad session value" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear) + "bad-session", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to income tax submission overview page if no session data is found" in {
      lazy val result: WSResponse = {
        if(appConfig.newGainsServiceEnabled) getSessionDataStub(status = NO_CONTENT) else clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }
  }

  ".submit" should {
    "redirect to gains status page if successful" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(amountOfGain = None)), gateway = Some(true))))

      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("policy-event" -> "Full or part surrender"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-status/$sessionId"
    }

    "redirect to gains status page if successful with other selected" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(completePolicyCyaModel.copy(amountOfGain = None)), gateway = Some(true))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("policy-event" -> "Other", "other-text" -> "Some other"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-status/$sessionId"
    }

    "show page with error text if no selection is made" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe BAD_REQUEST
    }

    "show page with error text if the wrong format is entered" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("policy-event" -> "123 456"))
      }

      result.status shouldBe BAD_REQUEST
    }

    "redirect to summary when model is full if successful" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        updateSession()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("policy-event" -> "Full or part surrender"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
    }
  }
}
