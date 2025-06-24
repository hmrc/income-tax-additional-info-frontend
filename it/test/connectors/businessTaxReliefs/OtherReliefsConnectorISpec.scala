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

package connectors.businessTaxReliefs

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.TestConnectorConfig
import connectors.errors.OtherReliefsSubmissionException
import models.Done
import models.businessTaxReliefs.OtherReliefs
import org.scalatest.{EitherValues, OptionValues}
import play.api.http.Status._
import play.api.libs.json.Json
import support.ConnectorIntegrationTest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientV2Support

import scala.concurrent.ExecutionContext.Implicits.global

class OtherReliefsConnectorISpec
  extends ConnectorIntegrationTest
  with HttpClientV2Support
  with EitherValues
  with OptionValues {

  private val taxYear = 2099
  private val nino = "nino-value"
  private val mtdItId = "mtdItId-value"
  private val data = OtherReliefs(
    Some(BigDecimal(1)),
    Some(BigDecimal(2)),
    Some(BigDecimal(3))
  )

  private val url = s"/income-tax-additional-information/income-tax/reliefs/other/$nino/$taxYear"

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val connector = new OtherReliefsConnector(
      config = TestConnectorConfig(
        vcSessionServiceBaseUrl = "",
        additionalInformationServiceBaseUrl =  wireMockUrl
      ),
      httpClient = httpClientV2
    )
  }

  "submit" when {

    "given a valid payload and response" should {
      "return Done for a 204 response" in new Test {
        stubFor(
          put(urlMatching(url))
            .withRequestBody(equalToJson(Json.toJson(data).toString))
            .willReturn(
              aResponse()
                .withStatus(NO_CONTENT)
            )
        )

        val result = await(connector.submit(nino, mtdItId, taxYear, data)(hc))

        result shouldBe Done
      }
    }

    "there is an error with the payload" should {
      "a failed future for a 400 response" in new Test {
        stubFor(
          put(urlMatching(url))
            .withRequestBody(equalToJson(Json.toJson(data).toString))
            .willReturn(
              aResponse().withStatus(BAD_REQUEST)
            )
        )

        val result = await(connector.submit(nino, mtdItId, taxYear, data)(hc).failed)

        result shouldBe OtherReliefsSubmissionException(BAD_REQUEST)
      }

      "a failed future for a 404 response" in new Test {
        stubFor(
          put(urlMatching(url))
            .withRequestBody(equalToJson(Json.toJson(data).toString))
            .willReturn(
              aResponse().withStatus(NOT_FOUND)
            )
        )

        val result = await(connector.submit(nino, mtdItId, taxYear, data)(hc).failed)

        result shouldBe OtherReliefsSubmissionException(NOT_FOUND)
      }

      "a failed future for a 422 response" in new Test {
        stubFor(
          put(urlMatching(url))
            .withRequestBody(equalToJson(Json.toJson(data).toString))
            .willReturn(
              aResponse().withStatus(UNPROCESSABLE_ENTITY)
            )
        )

        val result = await(connector.submit(nino, mtdItId, taxYear, data)(hc).failed)

        result shouldBe OtherReliefsSubmissionException(UNPROCESSABLE_ENTITY)
      }
    }

    "when there an error with the downstream service" should {
      "a failed future for a 500 response" in new Test {
        stubFor(
          put(urlMatching(url))
            .withRequestBody(equalToJson(Json.toJson(data).toString))
            .willReturn(
              aResponse().withStatus(INTERNAL_SERVER_ERROR)
            )
        )

        val result = await(connector.submit(nino, mtdItId, taxYear, data)(hc).failed)

        result shouldBe OtherReliefsSubmissionException(INTERNAL_SERVER_ERROR)
      }

      "a failed future for a 503 response" in new Test {
        stubFor(
          put(urlMatching(url))
            .withRequestBody(equalToJson(Json.toJson(data).toString))
            .willReturn(
              aResponse().withStatus(SERVICE_UNAVAILABLE)
            )
        )

        val result = await(connector.submit(nino, mtdItId, taxYear, data)(hc).failed)

        result shouldBe OtherReliefsSubmissionException(SERVICE_UNAVAILABLE)
      }
    }
  }
}
