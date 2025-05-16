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

package connectors.session

import connectors.TestConnectorConfig
import connectors.errors.{ApiError, SingleErrorBody}
import models.session.UserSessionData
import org.scalatest.{EitherValues, OptionValues}
import play.api.http.Status._
import play.api.libs.json.Json
import test.support.ConnectorIntegrationTest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientV2Support

import scala.concurrent.ExecutionContext.Implicits.global

class UserSessionDataConnectorISpec
  extends ConnectorIntegrationTest
  with HttpClientV2Support
  with EitherValues
  with OptionValues {

  private val getUrl = "/income-tax-session-data"
  private val sessionDataResponse = UserSessionData("test_sessionId", "test_mtditid", "test_nino", "test_utr")

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val connector = new UserSessionDataConnectorImpl(
      config = TestConnectorConfig(
        vcSessionServiceBaseUrl = wireMockUrl
      ),
      httpClient = httpClientV2
    )
  }

  "getSessionData" when {
    "return session data" when {
      "downstream response is successful and includes valid JSON" in new Test {
        stubGet(getUrl, OK, Json.toJson(sessionDataResponse).toString())

        val result = await(connector.getSessionData(hc))
        result.value.value shouldBe sessionDataResponse
      }
    }

    "return Right(None)" when {
      "downstream returns NOT_FOUND" in new Test {
        stubGet(getUrl, NOT_FOUND, "")

        val result = await(connector.getSessionData(hc))
        result.value shouldBe None
      }

      "downstream returns NO_CONTENT" in new Test {
        stubGet(getUrl, NO_CONTENT, "")

        val result = await(connector.getSessionData(hc))
        result.value shouldBe None
      }
    }

    "return Left(error)" when {

      "downstream returns OK but with an invalid response payload" in new Test {
        stubGet(getUrl, OK, "{}")

        val result = await(connector.getSessionData(hc))
        result.left.value.status shouldBe INTERNAL_SERVER_ERROR
        result.left.value.body.isInstanceOf[SingleErrorBody] shouldBe true
        result.left.value.body.asInstanceOf[SingleErrorBody].reason should include("Error parsing response from API")
      }

      "downstream fails with Internal Error" in new Test {
        val serviceUnavailableError = SingleErrorBody("INTERNAL_SERVER_ERROR", "Failed to retrieve session with id")
        stubGet(getUrl, INTERNAL_SERVER_ERROR, Json.toJson(serviceUnavailableError).toString())

        val result = await(connector.getSessionData(hc))
        result.left.value shouldBe ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "Failed to retrieve session with id"))
      }

      "downstream returns SERVICE_UNAVAILABLE" in new Test {
        val serviceUnavailableError = SingleErrorBody("SERVICE_UNAVAILABLE", "Internal Server error")
        stubGet(getUrl, SERVICE_UNAVAILABLE, Json.toJson(serviceUnavailableError).toString())

        val result = await(connector.getSessionData(hc))
        result.left.value shouldBe ApiError(SERVICE_UNAVAILABLE, SingleErrorBody("SERVICE_UNAVAILABLE", "Internal Server error"))
      }

      "downstream fails with unknown error" in new Test {
        val someRandomError = SingleErrorBody("TOO_MANY_REQUESTS", s"some random error")
        stubGet(getUrl, TOO_MANY_REQUESTS, Json.toJson(someRandomError).toString())

        val result = await(connector.getSessionData(hc))
        result.left.value shouldBe ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("TOO_MANY_REQUESTS", s"some random error"))
      }

      "downstream fails when there is unexpected response format" in new Test {
        stubGet(getUrl, TOO_MANY_REQUESTS, Json.toJson("unexpected response").toString())

        val result = await(connector.getSessionData(hc))
        result.left.value shouldBe ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API"))
      }
    }
  }
}
