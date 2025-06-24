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

import connectors.GetGainsConnector
import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import models.mongo.DataNotUpdated
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import repositories.GainsUserDataRepository
import support.builders.requests.AuthorisationRequestBuilder
import support.utils.TaxYearUtils
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GainsSessionServiceISpec extends AnyWordSpec with Matchers with FutureAwaits with DefaultAwaitTimeout {

  trait Setup {
    val gainsUserDataRepository: GainsUserDataRepository = mock[GainsUserDataRepository]
    val getGainsDataConnector: GetGainsConnector = mock[GetGainsConnector]

    val gainsSessionService: GainsSessionServiceImpl = new GainsSessionServiceImpl(gainsUserDataRepository, getGainsDataConnector)
  }

  val sessionId = "sessionId-eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  val completePolicyCyaModel: PolicyCyaModel =
    PolicyCyaModel(sessionId, Some("Life Insurance"), Some("123"), Some(0), Some(""), Some(true), Some(0), Some(0), Some(true), Some(123.11), Some(true), Some(123.11))

  val headerCarrier = HeaderCarrier()

  val taxYear = TaxYearUtils.taxYear

  "GainsSessionServiceImpl" should {

    "create" should {

      "return false when failing to decrypt the model" in new Setup {
        when(gainsUserDataRepository.create(any())).thenReturn(Future.successful(Left(DataNotUpdated)))

        val result =
          await(gainsSessionService.createSessionData(
            AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(Future.successful(false))(Future.successful(true))(AuthorisationRequestBuilder.anAuthorisationRequest, headerCarrier))

        result shouldBe false
      }

      "return true when successful and false when adding a duplicate" in new Setup {
        when(gainsUserDataRepository.create(any()))
          .thenReturn(
            Future.successful(Right(true)),
            Future.successful(Left(DataNotUpdated))
          )

        val model = AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true))
        val initialResult =
          await(gainsSessionService.createSessionData(model, taxYear)(Future.successful(false))(Future.successful(true))(
            AuthorisationRequestBuilder.anAuthorisationRequest, headerCarrier)
          )

        val duplicateResult =
          await(gainsSessionService.createSessionData(model, taxYear)(Future.successful(false))(Future.successful(true))(
            AuthorisationRequestBuilder.anAuthorisationRequest, headerCarrier)
          )

        initialResult shouldBe true
        duplicateResult shouldBe false
      }
    }

    "update" should {
      "return false when failing to decrypt the model" in new Setup {
        when(gainsUserDataRepository.update(any()))
          .thenReturn(Future.successful(Left(DataNotUpdated)))

        val result = await(gainsSessionService.updateSessionData(AllGainsSessionModel
        (Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(Future.successful(false))(Future.successful(true))
        (AuthorisationRequestBuilder.anAuthorisationRequest, headerCarrier))

        result shouldBe false
      }
    }
  }
}
