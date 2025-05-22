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

package services

import config.TestFeatureConfig
import connectors.errors._
import connectors.session.UserSessionDataConnector
import models.authorisation.SessionValues
import models.session.UserSessionData
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.IM_A_TEAPOT
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class SessionDetailsServiceSpec extends AnyWordSpec with Matchers with MockFactory with FutureAwaits with DefaultAwaitTimeout {

  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val sessionId = "test-sessionId"

  "getSessionData" when {
    "V&C Session Data service feature is enabled" when {
      "the call to retrieve session data from the downstream V&C service is successful" should {
        "return the session data" in {
          val config = TestFeatureConfig.allDisabled.enableSessionCookieService
          val mockSessionDataConnector = mock[UserSessionDataConnector]
          val testService = new SessionDetailsService(mockSessionDataConnector, config)

          val userSessionData = UserSessionData(sessionId = sessionId, mtditid = "111111", nino = "AA111111A", utr = Some("123456"))
          (mockSessionDataConnector.getSessionData(_: HeaderCarrier))
            .expects(*)
            .returning(Future.successful(Right(Some(userSessionData))))

          implicit val request = FakeRequest()
            .withSession(
              (SessionValues.CLIENT_NINO, "BB111111B"),
              (SessionValues.CLIENT_MTDITID, "87654321")
            )

          val result = await(testService.getSessionData(sessionId))
          result mustEqual userSessionData
        }
      }

      "the call to retrieve session data from the downstream V&C service is unsuccessful" should {
        "the fallback is successful and retrieves client MTDITID and NINO from the Session Cookie" should {
          "return session data" in {
            val config = TestFeatureConfig.allDisabled.enableSessionCookieService
            val mockSessionDataConnector = mock[UserSessionDataConnector]
            val testService = new SessionDetailsService(mockSessionDataConnector, config)

            val error = ApiError(IM_A_TEAPOT, SingleErrorBody("codeValue", "reasonValue"))
            (mockSessionDataConnector.getSessionData(_: HeaderCarrier))
              .expects(*)
              .returning(Future.successful(Left(error)))

            val userSessionData = UserSessionData(sessionId = sessionId, mtditid = "111111", nino = "AA111111A", utr = None)
            implicit val request = FakeRequest()
              .withSession(
                (SessionValues.CLIENT_NINO, userSessionData.nino),
                (SessionValues.CLIENT_MTDITID, userSessionData.mtditid)
              )

            val result = await(testService.getSessionData(sessionId))
            result mustEqual userSessionData
          }
        }

        "the retrieval of fallback data from the session cookie also fails to find the clients details" should {
          "return an error when session values are missing" in {
            val config = TestFeatureConfig.allDisabled.enableSessionCookieService
            val mockSessionDataConnector = mock[UserSessionDataConnector]
            val testService = new SessionDetailsService(mockSessionDataConnector, config)

            val error = ApiError(IM_A_TEAPOT, SingleErrorBody("codeValue", "reasonValue"))
            (mockSessionDataConnector.getSessionData(_: HeaderCarrier))
              .expects(*)
              .returning(Future.successful(Left(error)))

            implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

            val result = intercept[MissingAgentClientDetails](await(testService.getSessionData(sessionId)))
            result.message mustEqual "Session Data service and Session Cookie both returned empty data"
          }
        }
      }
    }

    "V&C Session Data service feature is enabled" when {
      "the call to retrieve session data from the downstream V&C service is successful" should {
        "return session data" in {
          val config = TestFeatureConfig.allDisabled.disableSessionCookieService
          val mockSessionDataConnector = mock[UserSessionDataConnector]
          val testService = new SessionDetailsService(mockSessionDataConnector, config)

          (mockSessionDataConnector.getSessionData(_: HeaderCarrier))
            .expects(*)
            .never()

          val userSessionData = UserSessionData(sessionId = sessionId, mtditid = "111111", nino = "AA111111A", utr = None)
          implicit val request = FakeRequest()
            .withSession(
              (SessionValues.CLIENT_NINO, userSessionData.nino),
              (SessionValues.CLIENT_MTDITID, userSessionData.mtditid)
            )

          val result = await(testService.getSessionData(sessionId))
          result mustEqual userSessionData
        }
      }

      "return an error when session values are missing" in {
        val config = TestFeatureConfig.allDisabled.disableSessionCookieService
        val mockSessionDataConnector = mock[UserSessionDataConnector]
        val testService = new SessionDetailsService(mockSessionDataConnector, config)

        (mockSessionDataConnector.getSessionData(_: HeaderCarrier))
          .expects(*)
          .never()

        implicit val request = FakeRequest()

        val result = intercept[MissingAgentClientDetails](await(testService.getSessionData(sessionId)))
        result.message mustEqual "Session Data service and Session Cookie both returned empty data"
      }
    }
  }
}


