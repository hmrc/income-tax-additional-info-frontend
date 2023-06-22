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

import connectors.GainsSubmissionConnector
import connectors.errors.{ApiError, SingleErrorBody}
import connectors.httpParsers.GainsSubmissionHttpParser._
import models.gains.{CapitalRedemptionModel, ForeignModel, GainsSubmissionModel, LifeAnnuityModel, LifeInsuranceModel, VoidedIsaModel}
import play.api.http.Status._
import support.UnitTest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class GainsSubmissionServiceSpec extends UnitTest {

  val connector: GainsSubmissionConnector = mock[GainsSubmissionConnector]
  val auth: AuthConnector = mock[AuthConnector]
  val service = new GainsSubmissionService(connector)

  ".submitGains" should {

    "return the connector response" when {

      val gainAmount: BigDecimal = 123.45

      val cyaData = GainsSubmissionModel(
        Some(Seq(LifeInsuranceModel(None, None, gainAmount,None, None, None, None))),
        Some(Seq(CapitalRedemptionModel(None, None, gainAmount, None, None, None, None))),
        Some(Seq(LifeAnnuityModel(None, None, gainAmount, None, None, None, None))),
        Some(Seq(VoidedIsaModel(None, None, gainAmount, None, None, None))),
        Some(Seq(ForeignModel(None, gainAmount, None, None))),
      )


      val nino = "AA123456A"
      val mtdItid = "SomeMtdItid"
      val taxYear = 2023

      "Given connector returns a right" in  {
        implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
        (connector.submitGains(_: GainsSubmissionModel, _: String, _: Int)(_: HeaderCarrier))
          .expects(cyaData, nino, taxYear, emptyHeaderCarrier.withExtraHeaders("mtditid"-> mtdItid)).returning(Future.successful(Right((NO_CONTENT))))

        val result = await(service.submitGains(Some(cyaData), nino, mtdItid, taxYear))
        result.isRight shouldBe true

      }
      "Given connector returns a left" in {
        implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

        (connector.submitGains(_: GainsSubmissionModel, _: String, _: Int)(_: HeaderCarrier))
          .expects(cyaData, nino, taxYear, emptyHeaderCarrier.withExtraHeaders("mtditid"-> mtdItid))
          .returning(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, SingleErrorBody("test","test")))))

        val result = await(service.submitGains(Some(cyaData), nino, mtdItid, taxYear))
        result.isLeft shouldBe true

      }

      "Given no model is supplied" in {

        implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
        lazy val result: GainsSubmissionResponse = {
          val blankData = GainsSubmissionModel(None, None, None, None, None)

          (blankData, nino, mtdItid, taxYear, emptyHeaderCarrier.withExtraHeaders("mtditid"-> mtdItid))
          Future.successful(Right(NO_CONTENT))

          await(service.submitGains(None, nino, mtdItid, taxYear))
        }
        result.isRight shouldBe true
        result shouldBe Right((NO_CONTENT))
      }
    }
  }
}