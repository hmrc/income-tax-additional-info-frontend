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

import java.util.UUID

case class VoidedIsaModel(
                           customerReference: Option[String] = None,
                           event: Option[String] = None,
                           gainAmount: BigDecimal,
                           taxPaidAmount: Option[BigDecimal] = None,
                           yearsHeld: Option[Int] = None,
                           yearsHeldSinceLastGain: Option[Int] = None
                         ) {
  def toPolicyCya: PolicyCyaModel = {
    PolicyCyaModel(
      sessionId = UUID.randomUUID().toString,
      policyType = Some("Voided ISA"),
      policyNumber = this.customerReference,
      amountOfGain = Some(this.gainAmount),
      policyEvent = this.event,
      previousGain = Some(this.yearsHeldSinceLastGain.isDefined),
      yearsPolicyHeld = this.yearsHeld,
      yearsPolicyHeldPrevious = this.yearsHeldSinceLastGain,
      treatedAsTaxPaid = Some(false),
      taxPaidAmount = this.taxPaidAmount,
      entitledToDeficiencyRelief = Some(false),
      deficiencyReliefAmount = None
    )
  }
}

object VoidedIsaModel {
  implicit val formats: OFormat[VoidedIsaModel] = Json.format[VoidedIsaModel]
}