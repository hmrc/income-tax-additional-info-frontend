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

import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import support.IntegrationTest
import support.builders.requests.AuthorisationRequestBuilder


class GainsSessionServiceISpec extends IntegrationTest {

  val gainsSessionServiceInvalidEncryption: GainsSessionService = appWithInvalidEncryptionKey.injector.instanceOf[GainsSessionService]
  gainsSessionService.createSessionData(AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, "")), gateway = Some(true)), taxYear)(false)(true)(AuthorisationRequestBuilder.anAuthorisationRequest, ec, headerCarrier)

  "create" should {
    "return false when failing to decrypt the model" in {
      val result =
        await(gainsSessionServiceInvalidEncryption.createSessionData(
          AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear
        )(false)(true)(AuthorisationRequestBuilder.anAuthorisationRequest, ec, headerCarrier))
      result shouldBe false
    }
    "return true when succesful and false when adding a duplicate" in {
      await(gainsUserDataRepository.collection.drop().toFuture())
      await(gainsUserDataRepository.ensureIndexes())
      val initialResult =
        await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(AuthorisationRequestBuilder.anAuthorisationRequest, ec, headerCarrier))
      val duplicateResult =
        await(gainsSessionService.createSessionData(AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(AuthorisationRequestBuilder.anAuthorisationRequest, ec, headerCarrier))
      initialResult shouldBe true
      duplicateResult shouldBe false
    }
  }

  "update" should {
    "return false when failing to decrypt the model" in {
      val result = await(gainsSessionServiceInvalidEncryption.updateSessionData(AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true)), taxYear)(false)(true)(AuthorisationRequestBuilder.anAuthorisationRequest, ec, headerCarrier))
      result shouldBe false
    }
  }
}
