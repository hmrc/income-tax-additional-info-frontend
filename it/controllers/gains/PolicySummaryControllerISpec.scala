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

import play.api.http.HeaderNames
import play.api.http.Status.{NO_CONTENT, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import support.IntegrationTest

class PolicySummaryControllerISpec extends IntegrationTest {

  clearSession()
  populateSessionData()

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
  }
  private val postUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary"
  private val submissionUrl: String = s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/exclude-journey/$taxYear"
  private val nrsUrl: String = s"/income-tax-nrs-proxy/$nino/itsa-personal-income-submission"

  ".show" should {
    "render the policy summary page" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy summary page for an agent" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render an empty summary page" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
      result.body.contains("Gain on a UK policy or contract")
    }

    "render the page with prior and cya data" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the overview page when no prior data and session data" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }
  }

  ".submit" should {
    "redirect to the gains summary page" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }

    "redirect to overview after submission" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        stubPost(submissionUrl, NO_CONTENT, "{}")
        stubPost(nrsUrl, OK, "{}")
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
    }

    "return an internal server error" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("journey" -> "gains"))
      }

      result.status shouldBe 500
    }
  }

}
