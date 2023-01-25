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

import models.gains.{EncryptedGainsCyaModel, GainsCyaModel}
import models.mongo._
import utils.AesGcmAdCrypto
import utils.CypherSyntax.{DecryptableOps, EncryptableOps}

import javax.inject.Inject

class EncryptionService @Inject()(implicit val encryptionService: AesGcmAdCrypto) {

  // Gains
  def encryptGainsUserData(gainsUserDataModel: GainsUserDataModel): EncryptedGainsUserDataModel = {
    implicit val associatedText: String = gainsUserDataModel.mtdItId

    EncryptedGainsUserDataModel(
      sessionId = gainsUserDataModel.sessionId,
      mtdItId = gainsUserDataModel.mtdItId,
      nino = gainsUserDataModel.nino,
      taxYear = gainsUserDataModel.taxYear,
      gains = gainsUserDataModel.gains.map(encryptGainsCyaModel),
      lastUpdated = gainsUserDataModel.lastUpdated
    )
  }

  def decryptGainsUserData(gainsUserDataModel: EncryptedGainsUserDataModel): GainsUserDataModel = {
    implicit val associatedText: String = gainsUserDataModel.mtdItId

    GainsUserDataModel(
      sessionId = gainsUserDataModel.sessionId,
      mtdItId = gainsUserDataModel.mtdItId,
      nino = gainsUserDataModel.nino,
      taxYear = gainsUserDataModel.taxYear,
      gains = gainsUserDataModel.gains.map(decryptGainsCyaModel),
      lastUpdated = gainsUserDataModel.lastUpdated
    )
  }

  private def encryptGainsCyaModel(gains: GainsCyaModel)
                                  (implicit associatedText: String): EncryptedGainsCyaModel = {
    EncryptedGainsCyaModel(
      gains.gatewayQuestion.map(_.encrypted),
      gains.customerReference.map(_.encrypted),
      gains.whatCausedThisGain.map(_.encrypted),
      gains.previousGain.map(_.encrypted),
      gains.yearsSinceLastGain.map(_.encrypted),
      gains.howMuchGain.map(_.encrypted),
      gains.policyYearsHeld.map(_.encrypted),
      gains.paidTaxOnGain.map(_.encrypted),
      gains.taxPaid.map(_.encrypted),
      gains.entitledToDeficiencyRelief.map(_.encrypted),
      gains.amountAvailableForRelief.map(_.encrypted)
    )
  }

  private def decryptGainsCyaModel(gains: EncryptedGainsCyaModel)
                                  (implicit associatedText: String): GainsCyaModel = {
    GainsCyaModel(
      gains.gatewayQuestion.map(_.decrypted[Boolean]),
      gains.customerReference.map(_.decrypted[String]),
      gains.whatCausedThisGain.map(_.decrypted[String]),
      gains.previousGain.map(_.decrypted[Boolean]),
      gains.yearsSinceLastGain.map(_.decrypted[String]),
      gains.howMuchGain.map(_.decrypted[BigDecimal]),
      gains.policyYearsHeld.map(_.decrypted[String]),
      gains.paidTaxOnGain.map(_.decrypted[Boolean]),
      gains.taxPaid.map(_.decrypted[BigDecimal]),
      gains.entitledToDeficiencyRelief.map(_.decrypted[Boolean]),
      gains.amountAvailableForRelief.map(_.decrypted[BigDecimal])
    )
  }
}
