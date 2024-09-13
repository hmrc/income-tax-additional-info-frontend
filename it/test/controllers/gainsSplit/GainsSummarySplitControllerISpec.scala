/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.gainsSplit

import models.gains.{CapitalRedemptionModel, LifeAnnuityModel, VoidedIsaModel}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.DefaultBodyWritables
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Environment, Mode}
import support.helpers.ViewHelper
import test.support.IntegrationTest

class GainsSummarySplitControllerISpec extends IntegrationTest with ViewHelper with DefaultBodyWritables {

  private val LIFE_INSURANCE: String = "Life Insurance"
  private val LIFE_ANNUITY: String = "Life Annuity"
  private val CAPITAL_REDEMPTION: String = "Capital Redemption"
  private val VOIDED_ISA: String = "Voided ISA"

  private val referenceNumber: String = "abc123"
  private val event: String = "event"
  private val monetaryValue: BigDecimal = 123.45

  private val lifeAnnuityModel = LifeAnnuityModel(Some(referenceNumber), Some(event), monetaryValue, Some(false), Some(2), Some(1), None)
  private val capitalRedemptionModel = CapitalRedemptionModel(Some(referenceNumber), Some(event), monetaryValue, Some(false), Some(2), Some(1), None)
  private val voidedIsaModel = VoidedIsaModel(Some(referenceNumber), Some(event), monetaryValue, None, Some(2), Some(1))

  private def url(policyType: String): String = controllers.gainsBase.routes.GainsSummaryBaseController.show(taxYear, Some(policyType)).url
  private val headers: Seq[(String, String)] = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

  "GainsSummarySplitController.show" should {
    "render the page with life insurance" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains LIFE_INSURANCE mustBe true
      }
    }

    "render the page with life annuity" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        userDataStub(Some(gainsPriorDataModel.get.copy(lifeAnnuity = Some(Seq(lifeAnnuityModel)))), nino, taxYear)

        val request = FakeRequest(GET, url(LIFE_ANNUITY)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains LIFE_ANNUITY mustBe true
      }
    }

    "render the page with capital redemption" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        userDataStub(Some(gainsPriorDataModel.get.copy(capitalRedemption = Some(Seq(capitalRedemptionModel)))), nino, taxYear)

        val request = FakeRequest(GET, url(CAPITAL_REDEMPTION)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains CAPITAL_REDEMPTION mustBe true
      }
    }

    "render the page with voided isa" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        userDataStub(Some(gainsPriorDataModel.get.copy(voidedIsa = Some(Seq(voidedIsaModel)))), nino, taxYear)

        val request = FakeRequest(GET, url(VOIDED_ISA)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains VOIDED_ISA mustBe true
      }
    }

    "render the page for an agent" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      val headers = Seq(HeaderNames.COOKIE -> playSessionCookies(taxYear))

      running(application) {

        authoriseAgentOrIndividual(isAgent = true)
        clearSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) contains completePolicyCyaModel.policyNumber.get
      }
    }

    "redirect to task list when prior is 'None'" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        emptyUserDataStub()

        val request = FakeRequest(GET, url(LIFE_INSURANCE)).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) shouldBe Some(s"http://localhost:11111/update-and-submit-income-tax-return/$taxYear/tasklist")
      }
    }

    "redirect to task list if no valid parameter is passed" in {
      val application = GuiceApplicationBuilder()
        .in(Environment.simple(mode = Mode.Dev))
        .configure(config ++ Seq("feature-switch.split-gains" -> true))
        .build()

      running(application) {

        authoriseIndividual()
        clearSession()
        userDataStub(gainsPriorDataModel, nino, taxYear)

        val request = FakeRequest(GET, url("invalid")).withHeaders(headers: _*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) shouldBe Some(s"http://localhost:11111/update-and-submit-income-tax-return/$taxYear/tasklist")
      }
    }
  }
}
