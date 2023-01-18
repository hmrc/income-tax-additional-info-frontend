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
import utils.EncryptedValue

case class GainsCyaModel(
                          gatewayQuestion: Option[Boolean] = None,
                          customerReference: Option[String] = None,
                          whatCausedThisGain: Option[String] = None,
                          previousGain: Option[Boolean] = None,
                          yearsSinceLastGain: Option[String] = None,
                          howMuchGain: Option[BigDecimal] = None,
                          policyYearsHeld: Option[String] = None,
                          paidTaxOnGain: Option[Boolean] = None,
                          taxPaid: Option[BigDecimal] = None,
                          entitledToDeficiencyRelief: Option[Boolean] = None,
                          amountAvailableForRelief: Option[BigDecimal] = None
                        )

object GainsCyaModel {

  implicit val format: OFormat[GainsCyaModel] = Json.format[GainsCyaModel]
}

case class EncryptedGainsCyaModel(
                                   gatewayQuestion: Option[EncryptedValue] = None,
                                   customerReference: Option[EncryptedValue] = None,
                                   whatCausedThisGain: Option[EncryptedValue] = None,
                                   previousGain: Option[EncryptedValue] = None,
                                   yearsSinceLastGain: Option[EncryptedValue] = None,
                                   howMuchGain: Option[EncryptedValue] = None,
                                   policyYearsHeld: Option[EncryptedValue] = None,
                                   paidTaxOnGain: Option[EncryptedValue] = None,
                                   taxPaid: Option[EncryptedValue] = None,
                                   entitledToDeficiencyRelief: Option[EncryptedValue] = None,
                                   amountAvailableForRelief: Option[EncryptedValue] = None
                                 )

object EncryptedGainsCyaModel {
  implicit val format: OFormat[EncryptedGainsCyaModel] = Json.format[EncryptedGainsCyaModel]
}