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
import play.api.http.Status._
import play.api.libs.ws.WSResponse
import support.IntegrationTest

class GainsSummaryControllerISpec extends IntegrationTest {
  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
  }

  ".show" should {
    "render the summary page" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(gainsPriorDataModel, nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the summary page for an agent" in {
      lazy val result: WSResponse = {
        clearSession()
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the empty summary page when no gains are found" in {
      lazy val result: WSResponse = {
        clearSession()
        populateEmptySessionData()
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "redirect to overview page when there is no prior or cya data" in {
      lazy val result: WSResponse = {
        clearSession()
        stubGetWithHeadersCheck(
          s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", NOT_FOUND,
          "", "X-Session-ID" -> sessionId, "mtditid" -> mtditid)
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe SEE_OTHER
    }

  }
}
