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

import models.gains.LifeInsuranceModel
import models.gains.prior.IncomeSourceObject
import play.api.http.HeaderNames
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.ws.WSResponse
import support.IntegrationTest

class PolicySummaryControllerISpec extends IntegrationTest {

  clearSession()
  populateSessionData()

  private def url(taxYear: Int): String = {
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/$sessionId"
  }

  ".show" should {
    "render the policy summary page" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel)), nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the policy summary page for an agent" in {
      lazy val result: WSResponse = {
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel)), nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render an empty summary page" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = true)
        emptyUserDataStub()
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
      result.body.contains("Gain on a UK policy or contract")
    }

    "render the page with prior data" in {
      lazy val result: WSResponse = {
        clearSession()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel.copy(lifeInsurance = Seq(LifeInsuranceModel(gainAmount = BigDecimal(123.45)))))), nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }

    "render the page with prior and cya data" in {
      lazy val result: WSResponse = {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = true)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel.copy(lifeInsurance = Seq(LifeInsuranceModel(gainAmount = BigDecimal(123.45)))))), nino, taxYear)
        urlGet(url(taxYear), headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)))
      }

      result.status shouldBe OK
    }
  }

  ".submit" should {
    "redirect to the gains summary page" in {
      lazy val result: WSResponse = {
        populateSessionData()
        authoriseAgentOrIndividual(isAgent = false)
        userDataStub(IncomeSourceObject(Some(gainsPriorDataModel)), nino, taxYear)
        urlPost(
          s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/policy-summary/", headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear)), body = ""
        )
      }

      result.status shouldBe SEE_OTHER
      result.headers("Location").head shouldBe s"/update-and-submit-income-tax-return/additional-information/$taxYear/gains/summary"
    }
  }

}
