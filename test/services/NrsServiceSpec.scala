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

package services

import connectors.NrsConnector
import connectors.httpParsers.NrsSubmissionHttpParser.NrsSubmissionResponse
import models.gains._
import models.gains.prior.GainsPriorDataModel
import play.api.libs.json.{JsString, Writes}
import support.UnitTest
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import java.util.UUID
import scala.concurrent.Future

class NrsServiceSpec extends UnitTest {

  val connector: NrsConnector = mock[NrsConnector]
  val service: NrsService = new NrsService(connector)
  val sessionId: String = UUID.randomUUID().toString

  implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId)))
  implicit val writesObject: Writes[DecodedGainsSubmissionPayload] = (o: DecodedGainsSubmissionPayload) => JsString(o.toString)

  val nino: String = "AA123456A"
  val mtditid: String = "968501689"

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
    Some(Seq(validLifeInsuranceModel)),
    Some(Seq(validCapitalRedemptionModel)),
    Some(Seq(validLifeAnnuityModel)),
    Some(Seq(validVoidedIsaModel)),
    Some(Seq(validForeignModel))
  )

  val priorData: GainsPriorDataModel = GainsPriorDataModel(
    "2020-01-04T05:01:01Z",
    Some(Seq(validLifeInsuranceModel)),
    Some(Seq(validCapitalRedemptionModel)),
    Some(Seq(validLifeAnnuityModel)),
    Some(Seq(validVoidedIsaModel)),
    Some(Seq(validForeignModel))
  )

  val decodedModel: DecodedGainsSubmissionPayload = DecodedGainsSubmissionPayload(Some(gainsCyaModel), Some(priorData))

  ".postNrsConnector" when {

    "there is user-agent, true client ip and port" should {

      "return the connector response" in {

        val expectedResult: NrsSubmissionResponse = Right(())

        val headerCarrierWithTrueClientDetails = headerCarrierWithSession.copy(trueClientIp = Some("127.0.0.1"), trueClientPort = Some("80"))

        (connector.postNrsConnector(_: String, _: DecodedGainsSubmissionPayload)(_: HeaderCarrier, _: Writes[DecodedGainsSubmissionPayload]))
          .expects(nino, decodedModel, headerCarrierWithTrueClientDetails.withExtraHeaders("mtditid" -> mtditid, "clientIP" -> "127.0.0.1", "clientPort" -> "80"), writesObject)
          .returning(Future.successful(expectedResult))

        val result = await(service.submit(nino, decodedModel, mtditid)(headerCarrierWithTrueClientDetails, writesObject))

        result shouldBe expectedResult
      }
    }

    "there isn't user-agent, true client ip and port" should {

      "return the connector response" in {

        val expectedResult: NrsSubmissionResponse = Right(())

        (connector.postNrsConnector(_: String, _: DecodedGainsSubmissionPayload)(_: HeaderCarrier, _: Writes[DecodedGainsSubmissionPayload]))
          .expects(nino, decodedModel, headerCarrierWithSession.withExtraHeaders("mtditid" -> mtditid), writesObject)
          .returning(Future.successful(expectedResult))
        val result = await(service.submit(nino, decodedModel, mtditid))
        result shouldBe expectedResult
      }
    }

  }

}
