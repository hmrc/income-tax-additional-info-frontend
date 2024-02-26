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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import support.IntegrationTest

class PolicySummaryControllerISpec extends IntegrationTest {

  clearSession()
  populateSessionData()

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
  }
  private val postUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
  private val putUrl: String =  s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear"
  private val submissionUrl: String = s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/exclude-journey/$taxYear"

  ".show" should {
    "render the policy summary page" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy summary page for an agent" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)
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

    "redirect to policy name page with incomplete cya data with policy type as Life insurance" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId, policyType = Some("Life Insurance"), previousGain=Some(true), entitledToDeficiencyRelief = Some(true))))
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

    "redirect to policy name page with incomplete cya data with previous gain and entitledToDeficiencyRelief as false" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId, policyType = Some("Life Insurance"), previousGain = Some(false), entitledToDeficiencyRelief = Some(false))))
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

    "redirect to policy name page with incomplete cya data with policy type as Voided ISA" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(PolicyCyaModel(sessionId, policyType = Some("Voided ISA"), previousGain = Some(true), entitledToDeficiencyRelief = Some(true))))
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
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

    "render the overview page when user clicks back and session id not matches" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionDataWithRandomSession()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }
  }

  ".submit" should {
    "redirect to the gains summary page when it has prior data and same policy reference number" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(policyNumber = Some("abc123"))))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        stubPut(putUrl, NO_CONTENT, "{}")
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }

    "redirect to the gains summary page when it has prior data with no active session" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq())
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        stubPut(putUrl, NO_CONTENT, "{}")
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }

    "redirect to the gains summary page when no prior data" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        stubPut(putUrl, NO_CONTENT, "{}")
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }

    "redirect to error page when there is a problem posting data" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        stubPut(putUrl, INTERNAL_SERVER_ERROR, "{}")
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to overview after submission" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionDataWithFalseGateway()
        authoriseAgentOrIndividual(isAgent = false)
        emptyUserDataStub()
        stubPost(submissionUrl, NO_CONTENT, "{}")
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
