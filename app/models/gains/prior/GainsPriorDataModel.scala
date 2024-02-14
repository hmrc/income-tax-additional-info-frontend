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

package models.gains.prior

import models.gains.{CapitalRedemptionModel, ForeignModel, LifeAnnuityModel, LifeInsuranceModel, PolicyCyaModel, VoidedIsaModel}
import play.api.libs.json.{Json, OFormat}

case class IncomeSourceObject(gains: Option[GainsPriorDataModel])

object IncomeSourceObject {
  implicit val format: OFormat[IncomeSourceObject] = Json.format[IncomeSourceObject]
}
case class GainsPriorDataModel(
                                submittedOn: String,
                                lifeInsurance: Option[Seq[LifeInsuranceModel]] = None,
                                capitalRedemption: Option[Seq[CapitalRedemptionModel]] = None,
                                lifeAnnuity: Option[Seq[LifeAnnuityModel]] = None,
                                voidedIsa: Option[Seq[VoidedIsaModel]] = None,
                                foreign: Option[Seq[ForeignModel]] = None
                              ) {
  def toPolicyCya: Seq[PolicyCyaModel] = {
    val lifeInsuranceConverted = lifeInsurance.map(_.map(_.toPolicyCya)).getOrElse(Seq.empty)
    val capitalRedemptionConverted = capitalRedemption.map(_.map(_.toPolicyCya)).getOrElse(Seq.empty)
    val lifeAnnuityConverted = lifeAnnuity.map(_.map(_.toPolicyCya)).getOrElse(Seq.empty)
    val voidedIsaConverted = voidedIsa.map(_.map(_.toPolicyCya)).getOrElse(Seq.empty)
    val foreignConverted = foreign.map(_.map(_.toPolicyCya)).getOrElse(Seq.empty)
    lifeInsuranceConverted ++ capitalRedemptionConverted ++ lifeAnnuityConverted ++ voidedIsaConverted ++ foreignConverted
  }
}

object GainsPriorDataModel {
  implicit val format: OFormat[GainsPriorDataModel] = Json.format[GainsPriorDataModel]
}