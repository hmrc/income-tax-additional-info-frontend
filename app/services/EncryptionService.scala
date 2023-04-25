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

import models.{AllGainsSessionModel, EncryptedAllGainsSessionModel}
import models.gains.{EncryptedPolicyCyaModel, PolicyCyaModel}
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
      gains = gainsUserDataModel.gains.map(value => encryptAllGainsSessionModel(value)),
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
      gains = gainsUserDataModel.gains.map(decryptAllGainsSessionModel),
      lastUpdated = gainsUserDataModel.lastUpdated
    )
  }

  private def encryptAllGainsSessionModel(allGainsSessionModel: AllGainsSessionModel)
                                           (implicit associatedText: String): EncryptedAllGainsSessionModel = {
    EncryptedAllGainsSessionModel(
      allGains = allGainsSessionModel.allGains.map(encryptPolicyCyaModel), allGainsSessionModel.gateway
    )
  }

  private def decryptAllGainsSessionModel(allGainsSessionModel: EncryptedAllGainsSessionModel)
                                           (implicit associatedText: String): AllGainsSessionModel = {
    AllGainsSessionModel(
      allGains = allGainsSessionModel.allGains.map(decryptPolicyCyaModel), allGainsSessionModel.gateway
    )
  }

  private def encryptPolicyCyaModel(gains: PolicyCyaModel)
                                  (implicit associatedText: String): EncryptedPolicyCyaModel = {
    EncryptedPolicyCyaModel(
      gains.sessionId.encrypted,
      gains.policyType.encrypted,
      gains.policyNumber.map(_.encrypted),
      gains.amountOfGain.map(_.encrypted),
      gains.policyEvent.map(_.encrypted),
      gains.previousGain.map(_.encrypted),
      gains.yearsPolicyHeld.map(_.toString.encrypted),
      gains.yearsPolicyHeldPrevious.map(_.toString.encrypted),
      gains.treatedAsTaxPaid.map(_.encrypted),
      gains.taxPaidAmount.map(_.encrypted),
      gains.entitledToDeficiencyRelief.map(_.encrypted),
      gains.deficiencyReliefAmount.map(_.encrypted)
    )
  }

  private def decryptPolicyCyaModel(gains: EncryptedPolicyCyaModel)
                                  (implicit associatedText: String): PolicyCyaModel = {
    PolicyCyaModel(
      gains.sessionId.decrypted[String],
      gains.policyType.decrypted[String],
      gains.policyNumber.map(_.decrypted[String]),
      gains.amountOfGain.map(_.decrypted[BigDecimal]),
      gains.policyEvent.map(_.decrypted[String]),
      gains.previousGain.map(_.decrypted[Boolean]),
      gains.yearsPolicyHeld.map(_.decrypted[String].toInt),
      gains.yearsPolicyHeldPrevious.map(_.decrypted[String].toInt),
      gains.treatedAsTaxPaid.map(_.decrypted[Boolean]),
      gains.taxPaidAmount.map(_.decrypted[BigDecimal]),
      gains.entitledToDeficiencyRelief.map(_.decrypted[Boolean]),
      gains.deficiencyReliefAmount.map(_.decrypted[BigDecimal])
    )

  }
}
