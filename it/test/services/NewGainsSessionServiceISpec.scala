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
import connectors.errors.{ApiError, SingleErrorBody}
import connectors.session.{CreateGainsSessionConnector, DeleteGainsSessionConnector, GetGainsSessionConnector, UpdateGainsSessionConnector}
import models.AllGainsSessionModel
import models.gains.prior.GainsPriorDataModel
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
  val mockGetGainsConnector: GetGainsConnector = mock[GetGainsConnector]
  val newGainsSessionServiceImpl: NewGainsSessionServiceImpl = new NewGainsSessionServiceImpl(
    mockGetGainsConnector, mockCreateSessionConnector, mockUpdateSessionConnector, mockDeleteSessionConnector, mockGetSessionConnector)(correlationId)

  val notFoundMongoError: SingleErrorBody = SingleErrorBody("PARSING_ERROR", "User data could not be found due to mongo exception")
  val createUpdateMongoError: SingleErrorBody = SingleErrorBody("PARSING_ERROR", "User data was not updated due to mongo exception")
  val deleteMongoError: SingleErrorBody = SingleErrorBody("PARSING_ERROR", "User data was not deleted due to mongo exception")
  val mongoDecryptionError: SingleErrorBody = SingleErrorBody("PARSING_ERROR", "Encryption / Decryption exception occurred. Exception: Failed to Decrypt")

  ".getPriorData" should {

    "return a Gains Prior Data Model when called" in {
      when(mockGetGainsConnector.getUserData(any())(any(), any())).thenReturn(Future.successful(Right(gainsPriorDataModel)))

      val result = await(newGainsSessionServiceImpl.getPriorData(taxYear)(anAuthorisationRequest, headerCarrier))

      result shouldBe Right(gainsPriorDataModel)
    }

    "return Nothing when Gains Prior Data Model does not exist" in {
      when(mockGetGainsConnector.getUserData(any())(any(), any())).thenReturn(Future.successful(Right(None)))

      val result = await(newGainsSessionServiceImpl.getPriorData(taxYear)(anAuthorisationRequest, headerCarrier))

      result shouldBe Right(None)
    }

    "return Error when Gains Prior Data can not be returned due to mongo exception" in {
      when(mockGetGainsConnector.getUserData(any())(any(), any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, notFoundMongoError))))

      val result = await(newGainsSessionServiceImpl.getPriorData(taxYear)(anAuthorisationRequest, headerCarrier))

      result shouldBe Left(ApiError(INTERNAL_SERVER_ERROR, notFoundMongoError))
    }
  }

  ".createSessionData" should {

    "return true when session is created" in {
      when(mockCreateSessionConnector.createSessionData(any(), any())(any())).thenReturn(Future.successful(Right(NO_CONTENT)))

      val result = await(newGainsSessionServiceImpl.createSessionData(
        AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier)
      )

      result shouldBe true
    }

    "return false when failing to create session" in {
      when(mockCreateSessionConnector.createSessionData(any(), any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, createUpdateMongoError))))

      val result = await(newGainsSessionServiceImpl.createSessionData(
        AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier)
      )

      result shouldBe false
    }
  }

  ".getSessionData" should {

    "return a Gains User Data Model when session is retrieved" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Right(Some(gainsUserDataModel))))

      val result = await(newGainsSessionServiceImpl.getSessionData(taxYear)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe Right(Some(gainsUserDataModel))
    }

    "return Nothing when session is not present" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Right(None)))

      val result = await(newGainsSessionServiceImpl.getSessionData(taxYear)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe Right(None)
    }

    "return Error when Gains User Data can not be returned due to mongo exception" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, notFoundMongoError))))

      val result = await(newGainsSessionServiceImpl.getSessionData(taxYear)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe Left(DataNotFound)
    }
  }

  ".updateSessionData" should {

    "return true when session is updated" in {
      when(mockUpdateSessionConnector.updateGainsSession(any(), any())(any())).thenReturn(Future.successful(Right(NO_CONTENT)))

      val result = await(newGainsSessionServiceImpl.updateSessionData(
        AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier)
      )

      result shouldBe true
    }

    "return false when failing to update session" in {
      when(mockUpdateSessionConnector.updateGainsSession(any(), any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, createUpdateMongoError))))

      val result = await(newGainsSessionServiceImpl.updateSessionData(
        AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier)
      )

      result shouldBe false
    }
  }

  ".deleteSessionData" should {

    "return true when session is deleted" in {
      when(mockDeleteSessionConnector.deleteGainsData(any())(any())).thenReturn(Future.successful(Right(true)))

      val result = await(newGainsSessionServiceImpl.deleteSessionData(taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe true
    }

    "return false when failing to delete session" in {
      when(mockDeleteSessionConnector.deleteGainsData(any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, deleteMongoError))))

      val result = await(newGainsSessionServiceImpl.deleteSessionData(taxYear)(false)(true)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe false
    }
  }

  ".getAndHandle" should {

    val block = (cya: Option[AllGainsSessionModel], prior: Option[GainsPriorDataModel]) => true

    "returns true and runs block when both Session and Prior Data exists" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Right(Some(gainsUserDataModel))))
      when(mockGetGainsConnector.getUserData(any())(any(), any())).thenReturn(Future.successful(Right(gainsPriorDataModel)))

      val result = await(newGainsSessionServiceImpl.getAndHandle(taxYear)(false)(block)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe true
    }

    "returns true and runs block when Prior Data exists but no Session Data is available" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Right(None)))
      when(mockGetGainsConnector.getUserData(any())(any(), any())).thenReturn(Future.successful(Right(gainsPriorDataModel)))

      val result = await(newGainsSessionServiceImpl.getAndHandle(taxYear)(false)(block)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe true
    }

    "returns false and when Session Data is not retrieved due to error and Prior Data exists" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, notFoundMongoError))))
      when(mockGetGainsConnector.getUserData(any())(any(), any())).thenReturn(Future.successful(Right(gainsPriorDataModel)))

      val result = await(newGainsSessionServiceImpl.getAndHandle(taxYear)(false)(block)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe false
    }

    "returns false and when Prior Data is not retrieved due to error and Session Data exists" in {
      when(mockGetSessionConnector.getSessionData(any())(any())).thenReturn(Future.successful(Right(Some(gainsUserDataModel))))
      when(mockGetGainsConnector.getUserData(any())(any(), any())).thenReturn(Future.successful(Left(ApiError(INTERNAL_SERVER_ERROR, notFoundMongoError))))

      val result = await(newGainsSessionServiceImpl.getAndHandle(taxYear)(false)(block)(anAuthorisationRequest, ec, headerCarrier))

      result shouldBe false
    }
  }

}
