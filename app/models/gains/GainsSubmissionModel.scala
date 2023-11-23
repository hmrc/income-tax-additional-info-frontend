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

case class GainsSubmissionModel(
                                 lifeInsurance: Option[Seq[LifeInsuranceModel]] = None,
                                 capitalRedemption: Option[Seq[CapitalRedemptionModel]] = None,
                                 lifeAnnuity: Option[Seq[LifeAnnuityModel]] = None,
                                 voidedIsa: Option[Seq[VoidedIsaModel]] = None,
                                 foreign: Option[Seq[ForeignModel]] = None
                               ){
  def clearModel: GainsSubmissionModel = {
    GainsSubmissionModel(
      lifeInsurance = if (lifeInsurance.exists(_.isEmpty)) None else lifeInsurance,
      capitalRedemption = if (capitalRedemption.exists(_.isEmpty)) None else capitalRedemption,
      lifeAnnuity = if (lifeAnnuity.exists(_.isEmpty)) None else lifeAnnuity,
      voidedIsa = if (voidedIsa.exists(_.isEmpty)) None else voidedIsa,
      foreign = if (foreign.exists(_.isEmpty)) None else foreign
    )
  }
}

object GainsSubmissionModel {
  implicit val format: OFormat[GainsSubmissionModel] = Json.format[GainsSubmissionModel]
}