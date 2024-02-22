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

import models.gains.PolicyCyaModel
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import support.IntegrationTest

class GainsGatewayControllerISpec extends IntegrationTest {

  clearSession()
  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/gains-gateway"
  }

  ".show" should {
    "render the gains gateway page" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        clearSession()
        populateSessionData()
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the gains gateway page for an agent" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = true)
        clearSession()
        populateSessionData()
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the gains gateway page when there is no cya data" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        clearSession()
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the gains gateway page when gateway value is none" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        clearSession()
        populateSessionDataWithEmptyGateway()
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

  }

  ".submit" should {
    "redirect to policy type page if successful" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("value" -> "true"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head contains(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-type/") shouldBe(true)
    }

    "redirect to policy type page when session not has any existing data" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId)))
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("value" -> "true"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head contains (s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-type/") shouldBe (true)
    }

    "redirect to policy summary page if user chooses 'No'" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("value" -> "false"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head contains(s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/") shouldBe(true)
    }

    "show page with error text when session is empty" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe BAD_REQUEST
    }

    "show page with error text if form is empty" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe BAD_REQUEST
    }
  }
}
