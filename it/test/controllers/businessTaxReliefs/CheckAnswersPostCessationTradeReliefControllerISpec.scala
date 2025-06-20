/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.businessTaxReliefs

import fixtures.messages.businessTaxReliefs.PostCessationTradeReliefMessages
import models.{BusinessTaxReliefs, Done, UserAnswersModel}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.businessTaxReliefs.PostCessationTradeReliefPage
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Environment, Mode, inject}
import services.businessTaxReliefs.OtherReliefsService
import support.stubs.UserAnswersStub
import support.IntegrationTest
import utils.ViewUtils.bigDecimalCurrency

import scala.concurrent.Future

class CheckAnswersPostCessationTradeReliefControllerISpec extends IntegrationTest with UserAnswersStub with BeforeAndAfterEach {

  val mockService: OtherReliefsService = mock[OtherReliefsService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(mockService)
  }

  lazy val application: Application = GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .bindings(inject.bind[OtherReliefsService].toInstance(mockService))
    .build()

  private def url(taxYear: Int): String =
    s"/update-and-submit-income-tax-return/additional-information/$taxYear/business-reliefs/post-cessation-trade-relief/check-answers"

  lazy val userAnswers: UserAnswersModel =
    emptyUserAnswers(taxYear, BusinessTaxReliefs).set(PostCessationTradeReliefPage, BigDecimal(22.45))

  ".show" when {

    "user is an Agent" should {

      "render the Check Answers Post Cessation Trade Relief page" in {

        authoriseAgentOrIndividual(isAgent = true)
        stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(PostCessationTradeReliefMessages.EnglishAgent.cya)
        contentAsString(result) must include(bigDecimalCurrency("22.45"))
      }
    }

    "user is an Individual" should {

      "render the Check Answers Post Cessation Trade Relief page" in {

        authoriseAgentOrIndividual(isAgent = false)
        stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)

        val request = FakeRequest(GET, url(taxYear)).withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear))
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include(PostCessationTradeReliefMessages.English.cya)
        contentAsString(result) must include("22.45")
      }
    }
  }

  ".submit" should {

    "user is an Agent" should {

      "redirect to the 'Have you finished' page" in {

        authoriseAgentOrIndividual(isAgent = true)
        stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)
        stubStoreUserAnswers()
        when(mockService.submit(eqTo(taxYear), any())(any())).thenReturn(Future.successful(Done))

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
      }
    }

    "user is an Individual" should {

      "redirect to the 'Have you finished' page" in {

        authoriseAgentOrIndividual(isAgent = false)
        stubGetUserAnswers(taxYear, BusinessTaxReliefs)(userAnswers)
        when(mockService.submit(eqTo(taxYear), any())(any())).thenReturn(Future.successful(Done))

        val request = FakeRequest(POST, url(taxYear))
          .withHeaders(HeaderNames.COOKIE -> playSessionCookies(taxYear), "Csrf-Token" -> "nocheck")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.incomeTaxSubmissionOverviewUrl(taxYear)
      }
    }
  }
}
