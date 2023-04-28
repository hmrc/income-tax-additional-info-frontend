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
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import support.IntegrationTest

class PoliciesRemoveControllerISpec extends IntegrationTest {

  clearSession()
  populateSessionData()

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policies-remove-confirmation/$sessionId"
  }

  ".show" should {
    "render the policies remove page" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policies remove page for an agent" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = true)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }
      result.status shouldBe OK
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
    "redirect to gains summary page if successful" in {
      lazy val result: WSResponse = {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel)), nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }

    "redirect to gains summary page and update submission that data was removed" in {
      lazy val result: WSResponse = {
        clearSession()
        populateWithSessionDataModel(Seq(completePolicyCyaModel.copy(sessionId = "session"), completePolicyCyaModel.copy(sessionId = "anotherSession")))
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel)), nino, taxYear)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }

    "redirect to gains summary page in case of no cya data" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = false)
        urlPost(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = "")
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }
  }

}
