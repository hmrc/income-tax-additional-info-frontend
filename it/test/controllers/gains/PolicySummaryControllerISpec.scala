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
import play.api.http.HeaderNames
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import test.support.IntegrationTest

import java.util.UUID

class PolicySummaryControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
  }
  private val postUrl: String = s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
  private val putUrl: String =  s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear"
  private val submissionUrl: String = s"/income-tax-submission-service/income-tax/nino/AA123456A/sources/exclude-journey/$taxYear"

  ".show" should {
    "render the policy summary page" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        updateSession()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy summary page for an agent" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        updateSession()
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render an empty summary page" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(), gateway = Some(false))))

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
      result.body.contains("Gain on a UK policy or contract")
    }

    "render the page with prior and cya data" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        updateSession()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    val updatedGainsUserDataModel =
      gainsUserDataModel.copy(
        gains = Some(
          AllGainsSessionModel(
            Seq(completePolicyCyaModel.copy(entitledToDeficiencyRelief = Some(true), deficiencyReliefAmount = None)), gateway = Some(true))
        )
      )


    "redirect to policy name page with incomplete cya data with policy type as Life insurance" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(
              Seq(completePolicyCyaModel.copy(yearsPolicyHeld = None)), gateway = Some(true))
          )
        )

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

    "redirect to policy name page with incomplete cya data with previous gain and entitledToDeficiencyRelief as false" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(
              Seq(completePolicyCyaModel.copy(previousGain = Some(false), yearsPolicyHeld = None, entitledToDeficiencyRelief = Some(false))), gateway = Some(true))
          )
        )

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

    "redirect to policy name page with incomplete cya data with policy type as Voided ISA" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(
              Seq(completePolicyCyaModel.copy(policyType = Some("Voided ISA"), yearsPolicyHeld = None)), gateway = Some(true))
          )
        )


      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

    "render the overview page when no prior data and session data" in {
      lazy val result: WSResponse = {
        getSessionDataStub(status = NO_CONTENT)
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

    "render the overview page when user clicks back and session id not matches" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(
              Seq(completePolicyCyaModel.copy(sessionId = s"sessionId-${UUID.randomUUID().toString}")), gateway = Some(true)))
        )

      lazy val result: WSResponse = {
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
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
        getSessionDataStub()
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
        getSessionDataStub()
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
        getSessionDataStub()
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
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        stubPut(putUrl, INTERNAL_SERVER_ERROR, "{}")
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe INTERNAL_SERVER_ERROR
    }

    "redirect to overview after submission" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(gains = Some(AllGainsSessionModel(Seq(), gateway = Some(false))))


      lazy val result: WSResponse = {
        clearSession()
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        updateSession()
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
        getSessionDataStub(status = INTERNAL_SERVER_ERROR)
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlPost(postUrl, headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = Map("journey" -> "gains"))
      }

      result.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

}
