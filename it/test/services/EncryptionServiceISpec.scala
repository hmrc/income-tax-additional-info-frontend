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
import models.mongo.{EncryptedGainsUserDataModel, GainsUserDataModel}
import support.IntegrationTest
import utils.AesGcmAdCrypto

import java.time.Instant

class EncryptionServiceISpec extends IntegrationTest {

  val service: EncryptionService = app.injector.instanceOf[EncryptionService]
  val encryption: AesGcmAdCrypto = app.injector.instanceOf[AesGcmAdCrypto]

  "encryptGainsUserData" should {

    val data: GainsUserDataModel = GainsUserDataModel("sessionId", "mtditid", "AA123456A", 1999, Some(AllGainsSessionModel(Seq(completePolicyCyaModel), gateway = Some(true))), Instant.now)

    "encrypt all the user data apart from the look up ids and timestamp" in {
      val result = service.encryptGainsUserData(data)
      result shouldBe EncryptedGainsUserDataModel(
        sessionId = data.sessionId,
        mtdItId = data.mtdItId,
        nino = data.nino,
        taxYear = data.taxYear,
        gains = result.gains,
        lastUpdated = data.lastUpdated
      )
    }

    "encrypt the data and decrypt it back to the initial model" in {
      val encryptResult = service.encryptGainsUserData(data)
      val decryptResult = service.decryptGainsUserData(encryptResult)

      decryptResult shouldBe data
    }
  }
}
