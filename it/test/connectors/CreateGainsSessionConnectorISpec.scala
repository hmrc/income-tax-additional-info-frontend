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

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.AppConfig
import connectors.errors.{ApiError, SingleErrorBody}
import connectors.httpParsers.CreateGainsSessionHttpParser.CreateGainsSessionResponse
import connectors.httpParsers.UpdateGainsSessionHttpParser.UpdateGainsSessionResponse
import connectors.session.{CreateGainsSessionConnector, UpdateGainsSessionConnector}
import models.AllGainsSessionModel
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.NO_CONTENT
import test.support.IntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CreateGainsSessionConnectorISpec extends IntegrationTest {

  lazy val connector: CreateGainsSessionConnector = app.injector.instanceOf[CreateGainsSessionConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def appConfig(host: String): AppConfig = new AppConfig(app.injector.instanceOf[ServicesConfig]) {
    override lazy val additionalInformationServiceBaseUrl: String = s"http://$host:$wiremockPort/income-tax-additional-information"
  }

  val sessionUrl: String = s"/income-tax-additional-information/income-tax/income/insurance-policies/$taxYear/session"

  val validGainsSessionModel: AllGainsSessionModel = AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true))

  "GainsSubmissionConnectorSpec" should {

    "include internal headers" when {
      val headersSentToGainsSubmission = Seq(new HttpHeader(HeaderNames.xSessionId, "sessionIdValue"), new HttpHeader("mtditid", mtditid))

      val internalHost = "localhost"

      "the host is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue"))).withExtraHeaders("mtditid" -> mtditid)
        val connector = new UpdateGainsSessionConnector(httpClient, appConfig(internalHost))

        stubPut(sessionUrl, NO_CONTENT, "{}", headersSentToGainsSubmission)

        val result: UpdateGainsSessionResponse = Await.result(connector.updateGainsSession(validGainsSessionModel, taxYear)(hc), Duration.Inf)

        result shouldBe Right(NO_CONTENT)
      }
    }

    "Return a success result" when {
      "Gains submission returns a 204" in {
        stubPost(sessionUrl, NO_CONTENT, "{}")
        val result: CreateGainsSessionResponse = Await.result(connector.createSessionData(validGainsSessionModel, taxYear)(hc), Duration.Inf)
        result shouldBe Right(NO_CONTENT)
      }

      "Gains submission returns a 400" in {
        stubPost(sessionUrl, BAD_REQUEST, "{}")
        val result: CreateGainsSessionResponse = Await.result(connector.createSessionData(validGainsSessionModel, taxYear)(hc), Duration.Inf)
        result shouldBe Left(ApiError(BAD_REQUEST, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
      }

      "Gains submission returns an error parsing from API 500 response" in {
        stubPost(sessionUrl, INTERNAL_SERVER_ERROR, "{}")
        val result: CreateGainsSessionResponse = Await.result(connector.createSessionData(validGainsSessionModel, taxYear)(hc), Duration.Inf)
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
      }

      "Gains submission returns an unexpected status error 500 response" in {
        val responseBody = Json.obj(
          "code" -> "INTERNAL_SERVER_ERROR",
          "reason" -> "Unexpected status returned from API"
        )

        stubPost(sessionUrl, CREATED, responseBody.toString())
        val result: CreateGainsSessionResponse = Await.result(connector.createSessionData(validGainsSessionModel, taxYear)(hc), Duration.Inf)
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "Unexpected status returned from API")))
      }

      "Gains submission returns a 500 when service is unavailable" in {
        val responseBody = Json.obj(
          "code" -> "SERVICE_UNAVAILABLE",
          "reason" -> "the service is currently unavailable"
        )

        stubPost(sessionUrl, SERVICE_UNAVAILABLE, responseBody.toString())
        val result: CreateGainsSessionResponse = Await.result(connector.createSessionData(validGainsSessionModel, taxYear)(hc), Duration.Inf)
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("SERVICE_UNAVAILABLE", "the service is currently unavailable")))
      }

    }
  }
}
