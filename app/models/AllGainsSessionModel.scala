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

package models

import models.gains.{
  CapitalRedemptionModel, EncryptedPolicyCyaModel, ForeignModel, GainsSubmissionModel, LifeAnnuityModel, LifeInsuranceModel, PolicyCyaModel, VoidedIsaModel
}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.crypto.EncryptedValue

case class AllGainsSessionModel(allGains: Seq[PolicyCyaModel], gateway: Boolean) {
  def toSubmissionModel: GainsSubmissionModel = {

    val life: Seq[LifeInsuranceModel] = allGains.filter(elem => elem.policyType == "Life Insurance").map(cya =>
      LifeInsuranceModel(
        customerReference = cya.policyNumber,
        event = cya.policyEvent,
        gainAmount = cya.amountOfGain.get,
        taxPaid = cya.treatedAsTaxPaid,
        yearsHeld = cya.yearsPolicyHeld,
        yearsHeldSinceLastGain = cya.yearsPolicyHeldPrevious,
        deficiencyRelief = cya.deficiencyReliefAmount)
    )
    val capital: Seq[CapitalRedemptionModel] = allGains.filter(elem => elem.policyType == "Capital Redemption").map(cya =>
      CapitalRedemptionModel(
        customerReference = cya.policyNumber,
        event = cya.policyEvent,
        gainAmount = cya.amountOfGain.get,
        taxPaid = cya.treatedAsTaxPaid,
        yearsHeld = cya.yearsPolicyHeld,
        yearsHeldSinceLastGain = cya.yearsPolicyHeldPrevious,
        deficiencyRelief = cya.deficiencyReliefAmount)
    )
    val lifeAnnuity: Seq[LifeAnnuityModel] = allGains.filter(elem => elem.policyType == "Life Annuity").map(cya =>
      LifeAnnuityModel(
        customerReference = cya.policyNumber,
        event = cya.policyEvent,
        gainAmount = cya.amountOfGain.get,
        taxPaid = cya.treatedAsTaxPaid,
        yearsHeld = cya.yearsPolicyHeld,
        yearsHeldSinceLastGain = cya.yearsPolicyHeldPrevious,
        deficiencyRelief = cya.deficiencyReliefAmount)
    )
    val foreign: Seq[ForeignModel] = allGains.filter(elem => elem.policyType == "Foreign Policy").map(cya =>
      ForeignModel(customerReference = cya.policyNumber, gainAmount = cya.amountOfGain.get, taxPaidAmount = cya.taxPaidAmount, yearsHeld = cya.yearsPolicyHeld)
    )
    val voided: Seq[VoidedIsaModel] = allGains.filter(elem => elem.policyType == "Voided ISA").map(cya =>
      VoidedIsaModel(
        customerReference = cya.policyNumber, gainAmount = cya.amountOfGain.get, taxPaidAmount = cya.taxPaidAmount, yearsHeld = cya.yearsPolicyHeld
      )
    )

    GainsSubmissionModel(
      lifeInsurance = life, capitalRedemption = Some(capital), lifeAnnuity = Some(lifeAnnuity), voidedIsa = Some(voided), foreign = Some(foreign)
    )
  }
}

object AllGainsSessionModel {

  implicit val formatOpt: OFormat[PolicyCyaModel] = Json.format[PolicyCyaModel]
  implicit val format: OFormat[AllGainsSessionModel] = Json.format[AllGainsSessionModel]

}

case class EncryptedAllGainsSessionModel(allGains: Seq[EncryptedPolicyCyaModel], gateway: Boolean)

object EncryptedAllGainsSessionModel {

  implicit val formatEnc: OFormat[EncryptedValue] = Json.format[EncryptedValue]
  implicit val formatSeq: OFormat[EncryptedPolicyCyaModel] = Json.format[EncryptedPolicyCyaModel]
  implicit val format: OFormat[EncryptedAllGainsSessionModel] = Json.format[EncryptedAllGainsSessionModel]

}