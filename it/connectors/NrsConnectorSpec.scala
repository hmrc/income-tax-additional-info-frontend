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
import models.gains.prior.GainsPriorDataModel
import models.gains._
import play.api.libs.json.{JsString, Writes}
import play.mvc.Http.Status._
import support.IntegrationTest
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class NrsConnectorSpec extends IntegrationTest {

  lazy val connector: NrsConnector = app.injector.instanceOf[NrsConnector]

  def appConfig(host: String): AppConfig = new AppConfig(app.injector.instanceOf[ServicesConfig]) {
    override lazy val nrsProxyBaseUrl: String = s"http://$host:$wiremockPort/income-tax-nrs-proxy"
  }

  implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))
  implicit val writesObject: Writes[DecodedGainsSubmissionPayload] = (o: DecodedGainsSubmissionPayload) => JsString(o.toString)

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

  val gainsCyaModel: GainsSubmissionModel = GainsSubmissionModel(
    Seq(validLifeInsuranceModel),
    Some(Seq(validCapitalRedemptionModel)),
    Some(Seq(validLifeAnnuityModel)),
    Some(Seq(validVoidedIsaModel)),
    Some(Seq(validForeignModel))
  )

  val priorData: GainsPriorDataModel = GainsPriorDataModel(
    "2020-01-04T05:01:01Z",
    Seq(validLifeInsuranceModel),
    Some(Seq(validCapitalRedemptionModel)),
    Some(Seq(validLifeAnnuityModel)),
    Some(Seq(validVoidedIsaModel)),
    Some(Seq(validForeignModel))
  )

  val decodedModel: DecodedGainsSubmissionPayload = DecodedGainsSubmissionPayload(Some(gainsCyaModel), Some(priorData))
  val expectedHeaders = Seq(new HttpHeader("mtditid", mtditid))

  val url: String = s"/income-tax-nrs-proxy/$nino/itsa-personal-income-submission"

  ".NrsConnector" should {

    "return an Ok response when successful" in {

      stubPost(url, OK, "{}")
      val result = await(connector.postNrsConnector(nino, decodedModel))

      result shouldBe Right()
    }

    "return an InternalServerError" in {

      val expectedResult = ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("INTERNAL_SERVER_ERROR", "Internal Server Error"))

      stubPost(url, INTERNAL_SERVER_ERROR, expectedResult.toJson.toString())
      val result = await(connector.postNrsConnector(nino, decodedModel))

      result shouldBe Left(expectedResult)
    }

    "return a NotFound error" in {

      val expectedResult = ApiError(NOT_FOUND, SingleErrorBody("NOT_FOUND", "NRS returning not found error"))

      stubPost(url, NOT_FOUND, expectedResult.toJson.toString())
      val result = await(connector.postNrsConnector(nino, decodedModel))

      result shouldBe Left(expectedResult)
    }

    "return a ParsingError when an unexpected error has occurred" in {

      val expectedResult = ApiError(CONFLICT, SingleErrorBody("PARSING_ERROR", "Error parsing response from API"))

      stubPost(url, CONFLICT, expectedResult.toJson.toString())
      val result = await(connector.postNrsConnector(nino, decodedModel))

      result shouldBe Left(expectedResult)
    }

  }
}