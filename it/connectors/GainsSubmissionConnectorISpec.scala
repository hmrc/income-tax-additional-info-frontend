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
import connectors.httpParsers.GainsSubmissionHttpParser._
import models.gains._
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.NO_CONTENT
import support.IntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GainsSubmissionConnectorISpec extends IntegrationTest {

  lazy val connector: GainsSubmissionConnector = app.injector.instanceOf[GainsSubmissionConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(host: String): AppConfig = new AppConfig(app.injector.instanceOf[ServicesConfig]) {
    override lazy val additionalInformationServiceBaseUrl: String = s"http://$host:$wiremockPort/income-tax-additional-information"
  }


  val validLifeInsuranceModel: LifeInsuranceModel = LifeInsuranceModel(
    customerReference = Some("RefNo13254687"),
    event = Some("Life"),
    gainAmount = 123.45,
    taxPaid = Some(true),
    yearsHeld = Some(4),
    yearsHeldSinceLastGain = Some(3),
    deficiencyRelief = Some(123.45)
  )

  val validCapitalRedemptionModel: CapitalRedemptionModel = CapitalRedemptionModel(
    customerReference = Some("RefNo13254687"),
    event = Some("Capital"),
    gainAmount = 123.45,
    taxPaid = Some(true),
    yearsHeld = Some(3),
    yearsHeldSinceLastGain = Some(2),
    deficiencyRelief = Some(0)
  )

  val validLifeAnnuityModel: LifeAnnuityModel = LifeAnnuityModel(
    customerReference = Some("RefNo13254687"),
    event = Some("Life"),
    gainAmount = 0,
    taxPaid = Some(true),
    yearsHeld = Some(2),
    yearsHeldSinceLastGain = Some(22),
    deficiencyRelief = Some(123.45)
  )

  val validVoidedIsaModel: VoidedIsaModel = VoidedIsaModel(
    customerReference = Some("RefNo13254687"),
    event = Some("isa"),
    gainAmount = 123.45,
    taxPaidAmount = Some(123.45),
    yearsHeld = Some(5),
    yearsHeldSinceLastGain = Some(6)
  )

  val validForeignModel: ForeignModel = ForeignModel(
    customerReference = Some("RefNo13254687"),
    gainAmount = 123.45,
    taxPaidAmount = Some(123.45),
    yearsHeld = Some(3)
  )

  val validGainsSubmissionModel: GainsSubmissionModel = GainsSubmissionModel(
    Some(Seq(validLifeInsuranceModel)),
    Some(Seq(validCapitalRedemptionModel)),
    Some(Seq(validLifeAnnuityModel)),
    Some(Seq(validVoidedIsaModel)),
    Some(Seq(validForeignModel))
  )

  val expectedHeaders: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", mtditid))

  "GainsSubmissionConnectorSpec" should {

    "include internal headers" when {
      val headersSentToGainsSubmission= Seq(new HttpHeader(HeaderNames.xSessionId, "sessionIdValue"), new HttpHeader("mtditid", mtditid))

      val internalHost = "localhost"

      "the host is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue"))).withExtraHeaders("mtditid"->mtditid)
        val connector = new GainsSubmissionConnector(httpClient, appConfig(internalHost))

        stubPut(s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", NO_CONTENT, "{}",
          headersSentToGainsSubmission)

        val result: GainsSubmissionResponse = Await.result(connector.submitGains(validGainsSubmissionModel, nino, taxYear)(hc), Duration.Inf)

        result shouldBe Right(NO_CONTENT)
      }
    }
    "Return a success result" when {
      "Gains submission returns a 204" in {
        stubPut(s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", NO_CONTENT, "{}", expectedHeaders)
        val result: GainsSubmissionResponse = Await.result(connector.submitGains(validGainsSubmissionModel, nino, taxYear), Duration.Inf)
        result shouldBe Right(NO_CONTENT)
      }

      "Gains submission returns a 400" in {
        stubPut(s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", BAD_REQUEST, "{}", expectedHeaders)
        val result = Await.result(connector.submitGains(validGainsSubmissionModel, nino, taxYear), Duration.Inf)
        result shouldBe Left(ApiError(BAD_REQUEST, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
      }

      "Gains submission returns an error parsing from API 500 response" in {
        stubPut(s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", INTERNAL_SERVER_ERROR, "{}", expectedHeaders)
        val result = Await.result(connector.submitGains(validGainsSubmissionModel, nino, taxYear), Duration.Inf)
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("PARSING_ERROR", "Error while parsing response from API")))
      }

      "Gains submission returns an unexpected status error 500 response" in {

        val responseBody = Json.obj(
          "code" -> "INTERNAL_SERVER_ERROR",
          "reason" -> "Unexpected status returned from API"
        )

        stubPut(s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", CREATED, responseBody.toString(), expectedHeaders)
        val result = await(connector.submitGains(validGainsSubmissionModel, nino, taxYear))
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "Unexpected status returned from API")))
      }

      "Gains submission returns a 500 when service is unavailable" in {

        val responseBody = Json.obj(
          "code" -> "SERVICE_UNAVAILABLE",
          "reason" -> "the service is currently unavailable"
        )

        stubPut(s"/income-tax-additional-information/income-tax/insurance-policies/income/$nino/$taxYear", SERVICE_UNAVAILABLE, responseBody.toString(), expectedHeaders)
        val result = Await.result(connector.submitGains(validGainsSubmissionModel, nino, taxYear), Duration.Inf)

        /** TODO Fix me : handle Error method in parser is not returning expected error response as per spec.In this case response is
         *  service unavailable but supposed to return internal server error response */
        result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("SERVICE_UNAVAILABLE", "the service is currently unavailable")))
      }

    }
  }
}