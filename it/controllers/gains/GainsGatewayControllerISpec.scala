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

import models.gains.prior.IncomeSourceObject
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
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the gains gateway page for an agent" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the gains gateway page with no prior" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render gains gateway when prior data is empty" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel)), nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "redirect to policies summary page when cya and prior data is present" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel)), nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

    "redirect to policy summary page when gateway value is false" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

    "render the gains gateway page with only cya" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
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
        urlPost(s"${url(taxYear)}/$sessionId", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("value" -> "true"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-type/$sessionId"
    }

    "redirect to policy type page if successful with no data" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        urlPost(s"${url(taxYear)}/$sessionId", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("value" -> "true"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-type/$sessionId"
    }


    "redirect to policy summary page if user chooses 'No'" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        urlPost(s"${url(taxYear)}/$sessionId", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("value" -> "false"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
    }

    "redirect to policy summary page if user chooses 'No' with no cya or prior data" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        urlPost(s"${url(taxYear)}/$sessionId", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("value" -> "false"))
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
    }

    "show page with error text if form is empty" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(s"${url(taxYear)}/$sessionId", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map[String, String]())
      }

      result.status shouldBe BAD_REQUEST
    }
  }

    ".change" should {
      "render the gateway page" in {
        lazy val result: WSResponse = {
          authoriseAgentOrIndividual(isAgent = false)
          emptyUserDataStub()
          urlGet(s"${url(taxYear)}-change", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
        }

        result.status shouldBe OK
      }
    }
}
