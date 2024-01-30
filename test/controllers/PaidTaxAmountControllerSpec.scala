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

package controllers

import actions.AuthorisedAction
import com.google.inject.matcher.Matchers.any
import config.{AppConfig, ErrorHandler}
import controllers.gains.PaidTaxAmountController
import models.User
import models.mongo.DataNotFound
import models.requests.AuthorisationRequest
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessagesControllerComponents
import services.GainsSessionService
import support.UnitTest
import support.mocks.MockAuthorisationService
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.{ExecutionContext, Future}

class PaidTaxAmountControllerSpec extends UnitTest with MockAuthorisationService{

  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val service: GainsSessionService = mock[GainsSessionService]
  val authAction:AuthorisedAction = mock[AuthorisedAction]
  //val view: PaidTaxAmountPageView = inject[PaidTaxAmountPageView]
  val errorHandler:ErrorHandler = mock[ErrorHandler]
  val mcc:MessagesControllerComponents =  stubMessagesControllerComponents()

 /* implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  lazy val cc: ControllerComponents = stubControllerComponents()

  lazy val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withHeaders(
    HeaderNames.AUTHORIZATION -> "Bearer Token"
  )*/

  private val underTest = new PaidTaxAmountController(authAction, null, service, errorHandler)(mock[AppConfig], mcc, executionContext)

  val auth: AuthConnector = mock[AuthConnector]

  "controller" should {

    "return the service response" when {

      val nino = "AA123456A"
      val mtdItid = "SomeMtdItid"
      val taxYear = 2023

      "Given service returns a left" in {

        implicit val request: AuthorisationRequest[_] = AuthorisationRequest(User(mtdItid, None, nino,"", ""), FakeRequest())
        (authAction.parser).expects(any())
        mockAuth(Some("AA123456A"))
        (service.getSessionData(_: Int)(_: AuthorisationRequest[_] , _:ExecutionContext))
          .expects(taxYear, request, executionContext)
          .returning(Future.successful(Left(DataNotFound)))

        val result = underTest.submit(taxYear, "")
       // val result = await(service.getSessionData(taxYear))
        result.leftSideValue shouldBe DataNotFound

      }
    }
  }
}