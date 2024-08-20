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
import connectors.session.DeleteGainsSessionConnector
import play.api.http.Status._
import test.support.IntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class DeleteGainsSessionConnectorISpec extends IntegrationTest {

  lazy val connector: DeleteGainsSessionConnector = app.injector.instanceOf[DeleteGainsSessionConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val sessionUrl: String = s"/income-tax-additional-information/income-tax/income/insurance-policies/$taxYear/session"

  def appConfig(host: String): AppConfig = new AppConfig(app.injector.instanceOf[ServicesConfig]) {
    override lazy val additionalInformationServiceBaseUrl: String = s"http://$host:$wiremockPort/income-tax-additional-information"
  }

  "DeleteGainsConnector " should {

    "include internal headers" when {
      val headersSentToIF = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host for IF is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new DeleteGainsSessionConnector(httpClient, appConfig(internalHost))

        stubDeleteWithoutResponseBody(sessionUrl, NO_CONTENT, headersSentToIF)

        val result = await(connector.deleteGainsData(taxYear)(hc))

        result shouldBe Right(true)
      }

      "the host for IF is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new DeleteGainsSessionConnector(httpClient, appConfig(externalHost))

        stubDeleteWithoutResponseBody(sessionUrl, NO_CONTENT, Seq[HttpHeader]())

        val result = await(connector.deleteGainsData(taxYear)(hc))

        result shouldBe Right(true)
      }
    }

    "handle error" when {

      val errorBodyModel = SingleErrorBody("IF_CODE", "IF_REASON")

      Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, NOT_FOUND, BAD_REQUEST).foreach { status =>

        s"If returns $status" in {
          val ifError = ApiError(status, errorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubDeleteWithResponseBody(sessionUrl, status, ifError.toJson.toString())

          val result = await(connector.deleteGainsData(taxYear)(hc))

          result shouldBe Left(ifError)
        }
      }

      "IF returns an unexpected error code - 502 BadGateway" in {
        val ifError = ApiError(BAD_GATEWAY, errorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubDeleteWithResponseBody(sessionUrl, BAD_GATEWAY, ifError.toJson.toString())

        val result = await(connector.deleteGainsData(taxYear)(hc))

        result shouldBe Left(ifError)
      }

    }

  }
}
