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

package models.gains

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.crypto.EncryptedValue

case class PolicyCyaModel(
                           sessionId: String,
                           policyType: Option[String] = None,
                           policyNumber: Option[String] = None,
                           amountOfGain: Option[BigDecimal] = None,
                           policyEvent: Option[String] = None,
                           previousGain: Option[Boolean] = None,
                           yearsPolicyHeld: Option[Int] = None,
                           yearsPolicyHeldPrevious: Option[Int] = None,
                           treatedAsTaxPaid: Option[Boolean] = None,
                           taxPaidAmount: Option[BigDecimal] = None,
                           entitledToDeficiencyRelief: Option[Boolean] = None,
                           deficiencyReliefAmount: Option[BigDecimal] = None
                         ) {

  private def isFinishedGeneralPolicies: Boolean =
    policyNumber.isDefined &&
      amountOfGain.isDefined &&
      policyEvent.isDefined &&
      (previousGain.contains(true) && yearsPolicyHeldPrevious.isDefined | previousGain.contains(false)) &&
      yearsPolicyHeld.isDefined &&
      treatedAsTaxPaid.isDefined &&
      (entitledToDeficiencyRelief.contains(true) && deficiencyReliefAmount.isDefined | entitledToDeficiencyRelief.contains(false))

  private def isFinishedVoidedIsa: Boolean =
    policyNumber.isDefined &&
      amountOfGain.isDefined &&
      policyEvent.isDefined &&
      (previousGain.contains(true) && yearsPolicyHeldPrevious.isDefined | previousGain.contains(false)) &&
      yearsPolicyHeld.isDefined &&
      taxPaidAmount.isDefined

  def isFinished: Boolean = {
    policyType match {
      case Some("Voided ISA") => isFinishedVoidedIsa
      case _ => isFinishedGeneralPolicies
    }
  }
}

object PolicyCyaModel {

  implicit val format: OFormat[PolicyCyaModel] = Json.format[PolicyCyaModel]

}

case class EncryptedPolicyCyaModel(
                                    sessionId: EncryptedValue,
                                    policyType: Option[EncryptedValue] = None,
                                    policyNumber: Option[EncryptedValue] = None,
                                    amountOfGain: Option[EncryptedValue] = None,
                                    policyEvent: Option[EncryptedValue] = None,
                                    previousGain: Option[EncryptedValue] = None,
                                    yearsPolicyHeld: Option[EncryptedValue] = None,
                                    yearsPolicyHeldPrevious: Option[EncryptedValue] = None,
                                    treatedAsTaxPaid: Option[EncryptedValue] = None,
                                    taxPaidAmount: Option[EncryptedValue] = None,
                                    entitledToDeficiencyRelief: Option[EncryptedValue] = None,
                                    deficiencyReliefAmount: Option[EncryptedValue] = None
                                  )

object EncryptedPolicyCyaModel {

  implicit val format: OFormat[PolicyCyaModel] = Json.format[PolicyCyaModel]

}