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

import connectors.errors.{ApiError, SingleErrorBody}
import connectors.session.{CreateGainsSessionConnector, DeleteGainsSessionConnector, GetGainsSessionConnector, UpdateGainsSessionConnector}
import models.AllGainsSessionModel
import models.mongo.DataNotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT}
import support.builders.requests.AuthorisationRequestBuilder.anAuthorisationRequest
import test.support.IntegrationTest

import scala.concurrent.Future


class NewGainsSessionServiceISpec extends IntegrationTest {

  val mockCreateSessionConnector: CreateGainsSessionConnector = mock[CreateGainsSessionConnector]
  val mockUpdateSessionConnector: UpdateGainsSessionConnector = mock[UpdateGainsSessionConnector]
  val mockDeleteSessionConnector: DeleteGainsSessionConnector = mock[DeleteGainsSessionConnector]
  val mockGetSessionConnector: GetGainsSessionConnector = mock[GetGainsSessionConnector]
  val newGainsSessionService: NewGainsSessionService = new NewGainsSessionService(
    getGainsDataConnector, mockCreateSessionConnector, mockUpdateSessionConnector, mockDeleteSessionConnector, mockGetSessionConnector)(correlationId)

  ".createSessionData" should {
    "return true when session is created" in {
      when(mockCreateSessionConnector.createSessionData(any(), any())(any())).thenReturn(Future.successful(Right(NO_CONTENT)))

      val result = await(newGainsSessionService.createSessionData(
        AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier)
      )

      result shouldBe true
    }

    "return false when failing to create session" in {
      val singleErrorBody = SingleErrorBody("PARSING_ERROR", "Encryption / Decryption exception occurred. Exception: Failed to Decrypt")

      when(mockCreateSessionConnector.createSessionData(any(), any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, singleErrorBody))))

      val result = await(newGainsSessionService.createSessionData(
        AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier)
      )

      result shouldBe false
    }
  }

  ".getSessionData" should {
    "return a Gains User Data Model when session is retrieved" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Right(Some(gainsUserDataModel))))

      val result = await(newGainsSessionService.getSessionData(taxYear)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe Right(Some(gainsUserDataModel))
    }

    "return Nothing when session is not present" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Right(None)))

      val result = await(newGainsSessionService.getSessionData(taxYear)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe Right(None)
    }

    "return Error when Gains User Data can not be returned due to mongo exception" in {
      val singleErrorBody = SingleErrorBody("PARSING_ERROR", "User data could not be found due to mongo exception")

      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, singleErrorBody))))

      val result = await(newGainsSessionService.getSessionData(taxYear)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe Left(DataNotFound)
    }
  }

  ".updateSessionData" should {
    "return true when session is updated" in {
      when(mockUpdateSessionConnector.updateGainsSession(any(), any())(any())).thenReturn(Future.successful(Right(NO_CONTENT)))

      val result = await(newGainsSessionService.updateSessionData(
        AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier)
      )

      result shouldBe true
    }

    "return false when failing to update session" in {
      val singleErrorBody = SingleErrorBody("PARSING_ERROR", "Encryption / Decryption exception occurred. Exception: Failed to Decrypt")

      when(mockUpdateSessionConnector.updateGainsSession(any(), any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, singleErrorBody))))

      val result = await(newGainsSessionService.updateSessionData(
        AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier)
      )

      result shouldBe false
    }
  }

  ".deleteSessionData" should {
    "return true when session is deleted" in {
      when(mockDeleteSessionConnector.deleteGainsData(any())(any())).thenReturn(Future.successful(Right(true)))

      val result = await(newGainsSessionService.deleteSessionData(taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe true
    }

    "return false when failing to delete session" in {
      val singleErrorBody = SingleErrorBody("PARSING_ERROR", "User data was not deleted due to mongo exception")

      when(mockDeleteSessionConnector.deleteGainsData(any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, singleErrorBody))))

      val result = await(newGainsSessionService.deleteSessionData(taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe false
    }
  }

}
