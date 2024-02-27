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

import models.gains._
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.crypto.EncryptedValue

case class AllGainsSessionModel(allGains: Seq[PolicyCyaModel], gateway: Option[Boolean] = None) {
  def toSubmissionModel: GainsSubmissionModel = {

    def convertEmptyToOption[A](items: Seq[A]): Option[Seq[A]] = if (items.isEmpty) None else Some(items)

    val life: Option[Seq[LifeInsuranceModel]] = convertEmptyToOption(allGains.filter(elem => elem.policyType.contains("Life Insurance")).map(cya =>
      LifeInsuranceModel(
        customerReference = cya.policyNumber,
        event = cya.policyEvent,
        gainAmount = cya.amountOfGain.get,
        taxPaid = cya.treatedAsTaxPaid,
        yearsHeld = cya.yearsPolicyHeld,
        yearsHeldSinceLastGain = cya.yearsPolicyHeldPrevious,
        deficiencyRelief = cya.deficiencyReliefAmount)
    ))
    val capital: Option[Seq[CapitalRedemptionModel]] = convertEmptyToOption(allGains.filter(elem => elem.policyType.contains("Capital Redemption")).map(cya =>
      CapitalRedemptionModel(
        customerReference = cya.policyNumber,
        event = cya.policyEvent,
        gainAmount = cya.amountOfGain.get,
        taxPaid = cya.treatedAsTaxPaid,
        yearsHeld = cya.yearsPolicyHeld,
        yearsHeldSinceLastGain = cya.yearsPolicyHeldPrevious,
        deficiencyRelief = cya.deficiencyReliefAmount)
    ))
    val lifeAnnuity: Option[Seq[LifeAnnuityModel]] = convertEmptyToOption(allGains.filter(elem => elem.policyType.contains("Life Annuity")).map(cya =>
      LifeAnnuityModel(
        customerReference = cya.policyNumber,
        event = cya.policyEvent,
        gainAmount = cya.amountOfGain.get,
        taxPaid = cya.treatedAsTaxPaid,
        yearsHeld = cya.yearsPolicyHeld,
        yearsHeldSinceLastGain = cya.yearsPolicyHeldPrevious,
        deficiencyRelief = cya.deficiencyReliefAmount)
    ))

    val voidedIsa: Option[Seq[VoidedIsaModel]] = convertEmptyToOption(allGains.filter(elem => elem.policyType.contains("Voided ISA")).map(cya =>
      VoidedIsaModel(
        customerReference = cya.policyNumber,
        event = cya.policyEvent,
        gainAmount = cya.amountOfGain.get,
        taxPaidAmount = cya.taxPaidAmount,
        yearsHeld = cya.yearsPolicyHeld,
        yearsHeldSinceLastGain = cya.yearsPolicyHeldPrevious
      ))
    )

    val foreign: Option[Seq[ForeignModel]] = convertEmptyToOption(allGains.filter(elem => elem.policyType.contains("Foreign Policy")).map(cya =>
      ForeignModel(
        customerReference = cya.policyNumber,
        gainAmount = cya.amountOfGain.get,
        /*As per additional information manual form requirement, taxPaidAmount field is only relevant to voided ISA and
          doesn't require for foreign policy and hence this question is not part of foreign policy journey*/
        taxPaidAmount = None,
        yearsHeld = cya.yearsPolicyHeld)
    ))

    GainsSubmissionModel(
      lifeInsurance = life,
      capitalRedemption = capital,
      lifeAnnuity = lifeAnnuity,
      voidedIsa = voidedIsa,
      foreign = foreign
    )
  }
}

object AllGainsSessionModel {

  implicit val formatOpt: OFormat[PolicyCyaModel] = Json.format[PolicyCyaModel]
  implicit val format: OFormat[AllGainsSessionModel] = Json.format[AllGainsSessionModel]

}

case class EncryptedAllGainsSessionModel(allGains: Seq[EncryptedPolicyCyaModel], gateway: Option[Boolean])

object EncryptedAllGainsSessionModel {

  implicit val formatEnc: OFormat[EncryptedValue] = Json.format[EncryptedValue]
  implicit val formatSeq: OFormat[EncryptedPolicyCyaModel] = Json.format[EncryptedPolicyCyaModel]
  implicit val format: OFormat[EncryptedAllGainsSessionModel] = Json.format[EncryptedAllGainsSessionModel]

}