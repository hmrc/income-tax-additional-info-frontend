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
    (for {
      lifeInsurance <- lifeInsurance.map(_.map(_.toPolicyCya))
      capitalRedemption <- capitalRedemption.map(_.map(_.toPolicyCya))
      lifeAnnuity <- lifeAnnuity.map(_.map(_.toPolicyCya))
      voidedIsa <- voidedIsa.map(_.map(_.toPolicyCya))
      foreign <- foreign.map(_.map(_.toPolicyCya))
      allPolicies <- Some(lifeInsurance ++ capitalRedemption ++ lifeAnnuity ++ voidedIsa ++ foreign)
    } yield {
      allPolicies
    }).getOrElse(Seq[PolicyCyaModel]())
  }
}

object GainsPriorDataModel {
  implicit val format: OFormat[GainsPriorDataModel] = Json.format[GainsPriorDataModel]
}