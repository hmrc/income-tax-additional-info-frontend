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

import config.AppConfig
import models.gains.{EncryptedGainsCyaModel, GainsCyaModel}
import models.mongo._
import utils.SecureGCMCipher

import javax.inject.Inject

class EncryptionService @Inject()(encryptionService: SecureGCMCipher, appConfig: AppConfig) {

  // Gains
  def encryptGainsUserData(gainsUserDataModel: GainsUserDataModel): EncryptedGainsUserDataModel = {
    implicit val textAndKey: TextAndKey = TextAndKey(gainsUserDataModel.mtdItId, appConfig.encryptionKey)

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
    implicit val textAndKey: TextAndKey = TextAndKey(gainsUserDataModel.mtdItId, appConfig.encryptionKey)

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
                                  (implicit textAndKey: TextAndKey): EncryptedGainsCyaModel = {
    EncryptedGainsCyaModel(
      gains.gatewayQuestion.map(x => encryptionService.encrypt[Boolean](x)),
      gains.customerReference.map(x => encryptionService.encrypt[String](x)),
      gains.whatCausedThisGain.map(x => encryptionService.encrypt[String](x)),
      gains.previousGain.map(x => encryptionService.encrypt[Boolean](x)),
      gains.yearsSinceLastGain.map(x => encryptionService.encrypt[String](x)),
      gains.howMuchGain.map(x => encryptionService.encrypt[BigDecimal](x)),
      gains.policyYearsHeld.map(x => encryptionService.encrypt[String](x)),
      gains.paidTaxOnGain.map(x => encryptionService.encrypt[Boolean](x)),
      gains.taxPaid.map(x => encryptionService.encrypt[BigDecimal](x)),
      gains.entitledToDeficiencyRelief.map(x => encryptionService.encrypt[Boolean](x)),
      gains.amountAvailableForRelief.map(x => encryptionService.encrypt[BigDecimal](x))
    )
  }

  private def decryptGainsCyaModel(gains: EncryptedGainsCyaModel)
                                  (implicit textAndKey: TextAndKey): GainsCyaModel = {
    GainsCyaModel(
      gains.gatewayQuestion.map(x => encryptionService.decrypt[Boolean](x.value, x.nonce)),
      gains.customerReference.map(x => encryptionService.decrypt[String](x.value, x.nonce)),
      gains.whatCausedThisGain.map(x => encryptionService.decrypt[String](x.value, x.nonce)),
      gains.previousGain.map(x => encryptionService.decrypt[Boolean](x.value, x.nonce)),
      gains.yearsSinceLastGain.map(x => encryptionService.decrypt[String](x.value, x.nonce)),
      gains.howMuchGain.map(x => encryptionService.decrypt[BigDecimal](x.value, x.nonce)),
      gains.policyYearsHeld.map(x => encryptionService.decrypt[String](x.value, x.nonce)),
      gains.paidTaxOnGain.map(x => encryptionService.decrypt[Boolean](x.value, x.nonce)),
      gains.taxPaid.map(x => encryptionService.decrypt[BigDecimal](x.value, x.nonce)),
      gains.entitledToDeficiencyRelief.map(x => encryptionService.decrypt[Boolean](x.value, x.nonce)),
      gains.amountAvailableForRelief.map(x => encryptionService.decrypt[BigDecimal](x.value, x.nonce))
    )
  }
}
