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

import com.github.tomakehurst.wiremock.http.HttpHeader
import models.AllGainsSessionModel
import play.api.http.HeaderNames
import play.api.http.Status.{NO_CONTENT, OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import test.support.IntegrationTest

class PoliciesRemoveControllerISpec extends IntegrationTest {

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policies-remove-confirmation/$sessionId"
  }

  ".show" should {
    "render the policies remove page" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policies remove page for an agent" in {
      lazy val result: WSResponse = {
        getSessionDataStub()
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }
      result.status shouldBe OK
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
    "redirect to gains summary page if successful" in {
      val updatedGainsUserDataModel =
        gainsUserDataModel.copy(
          gains = Some(
            AllGainsSessionModel(Seq(completePolicyCyaModel.copy(sessionId = sessionId), completePolicyCyaModel.copy(sessionId = "anotherSession")), gateway = Some(true))
          )
        )

      lazy val result: WSResponse = {
        clearSession()
        getSessionDataStub(userData = Some(updatedGainsUserDataModel))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        stubPut(
          s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", NO_CONTENT,"", headersSentToIF)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }

    "redirect to gains summary page when cya is empty" in {
      lazy val result: WSResponse = {
        clearSession()
        getSessionDataStub(status = NO_CONTENT)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        authoriseAgentOrIndividual(isAgent = false)
        stubDeleteWithoutResponseBody(s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", NO_CONTENT, headersSentToIF)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }

    "redirect to gains summary page in case of no cya data and no prior data" in {
      lazy val result: WSResponse = {
        getSessionDataStub(status = NO_CONTENT)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }
  }

}
