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

package connectors

import connectors.errors.{ApiError, SingleErrorBody}
import connectors.httpParsers.GetGainsHttpParser.GetGainsResponse
import models.gains.prior.IncomeSourceObject
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import support.{ConnectorIntegrationTest, IntegrationTest}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GetGainsConnectorISpec extends IntegrationTest with ConnectorIntegrationTest {

  val connector: GetGainsConnector = app.injector.instanceOf[GetGainsConnector]

  implicit override val headerCarrier: HeaderCarrier = HeaderCarrier().withExtraHeaders("mtditid" -> mtditid, "X-Session-ID" -> sessionId)

  val url = s"/income-tax-submission-service/income-tax/nino/$nino/sources/session\\?taxYear=$taxYear"

  "GetGainsConnector" should {
    "Return a success result" when {
      "request returns a 204" in {
        stubGetWithHeadersCheck(url, NO_CONTENT,
          "{}", "X-Session-ID" -> sessionId, "mtditid" -> mtditid)

        val result: GetGainsResponse = Await.result(connector.getUserData(taxYear), Duration.Inf)
        result shouldBe Right(IncomeSourceObject(None))
      }

      "request returns a 200" in {

        stubGetWithHeadersCheck(url, OK,
          Json.toJson(IncomeSourceObject(Some(gainsPriorDataModel))).toString(), "X-Session-ID" -> sessionId, "mtditid" -> mtditid)
        val result: GetGainsResponse = Await.result(connector.getUserData(taxYear), Duration.Inf)
        result shouldBe Right(IncomeSourceObject(Some(gainsPriorDataModel)))
      }
    }

    "Return an error result" when {

      "request returns a 200 but invalid json" in {

        stubGetWithHeadersCheck(url, OK,
          Json.toJson("""{"invalid": true}""").toString(), "X-Session-ID" -> sessionId, "mtditid" -> mtditid)

        val result: GetGainsResponse = Await.result(connector.getUserData(taxYear), Duration.Inf)
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody.parsingError))
      }

      "request returns a 500" in {

        stubGetWithHeadersCheck(url, INTERNAL_SERVER_ERROR,
          """{"code": "FAILED", "reason": "failed"}""", "X-Session-ID" -> sessionId, "mtditid" -> mtditid)

        val result: GetGainsResponse = Await.result(connector.getUserData(taxYear), Duration.Inf)
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("FAILED", "failed")))
      }

      "request returns a 503" in {

        stubGetWithHeadersCheck(url, SERVICE_UNAVAILABLE,
          """{"code": "FAILED", "reason": "failed"}""", "X-Session-ID" -> sessionId, "mtditid" -> mtditid)

        val result: GetGainsResponse = Await.result(connector.getUserData(taxYear), Duration.Inf)
        result shouldBe Left(ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("FAILED", "failed")))
      }

      "request returns an unexpected result" in {

        stubGetWithHeadersCheck(url, IM_A_TEAPOT,
          """{"code": "FAILED", "reason": "failed"}""", "X-Session-ID" -> sessionId, "mtditid" -> mtditid)

        val result: GetGainsResponse = Await.result(connector.getUserData(taxYear), Duration.Inf)
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("FAILED", "failed")))
      }
    }
  }

}
