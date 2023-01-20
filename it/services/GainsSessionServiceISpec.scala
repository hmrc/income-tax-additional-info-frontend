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
import repositories.GainsUserDataRepository
import support.IntegrationTest


class GainsSessionServiceISpec extends IntegrationTest {

  val gainsUserDataRepository: GainsUserDataRepository = app.injector.instanceOf[GainsUserDataRepository]
  val getGainsDataConnector: GetGainsConnector = app.injector.instanceOf[GetGainsConnector]

  val gainsSessionServiceInvalidEncryption: GainsSessionService = appWithInvalidEncryptionKey.injector.instanceOf[GainsSessionService]
  val gainsSessionService: GainsSessionService = new GainsSessionService(gainsUserDataRepository, getGainsDataConnector)

  "create" should {
    "return false when failing to decrypt the model" in {
      val result = await(gainsSessionServiceInvalidEncryption.createSessionData(completeGainsCyaModel, taxYear)(false)(true))
      result shouldBe false
    }
    "return true when succesful and false when adding a duplicate" in {
      await(gainsUserDataRepository.collection.drop().toFuture())
      await(gainsUserDataRepository.ensureIndexes)
      val initialResult = await(gainsSessionService.createSessionData(completeGainsCyaModel, taxYear)(false)(true))
      val duplicateResult = await(gainsSessionService.createSessionData(completeGainsCyaModel, taxYear)(false)(true))
      initialResult shouldBe true
      duplicateResult shouldBe false
    }
  }

  "update" should {
    "return false when failing to decrypt the model" in {
      val result = await(gainsSessionServiceInvalidEncryption.updateSessionData(completeGainsCyaModel, taxYear)(false)(true))
      result shouldBe false
    }
  }
}
