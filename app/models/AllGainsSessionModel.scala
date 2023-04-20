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

import models.gains.{EncryptedPolicyCyaModel, PolicyCyaModel}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.crypto.EncryptedValue

case class AllGainsSessionModel(allGains: Seq[PolicyCyaModel])

object AllGainsSessionModel {

  implicit val formatOpt: OFormat[PolicyCyaModel] = Json.format[PolicyCyaModel]
  implicit val format: OFormat[AllGainsSessionModel] = Json.format[AllGainsSessionModel]

}
case class EncryptedAllGainsSessionModel(allGains: Seq[EncryptedPolicyCyaModel])

object EncryptedAllGainsSessionModel {

  implicit val formatEnc: OFormat[EncryptedValue] = Json.format[EncryptedValue]
  implicit val formatSeq: OFormat[EncryptedPolicyCyaModel] = Json.format[EncryptedPolicyCyaModel]
  implicit val format: OFormat[EncryptedAllGainsSessionModel] = Json.format[EncryptedAllGainsSessionModel]

}