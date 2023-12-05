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

package audit

import models.gains.{CapitalRedemptionModel, ForeignModel, GainsSubmissionModel, LifeAnnuityModel, LifeInsuranceModel, VoidedIsaModel}
import models.gains.prior.GainsPriorDataModel
import play.api.libs.json.{Json, OWrites}

case class CreateOrAmendGainsAuditDetail(lifeInsurance: Option[Seq[LifeInsuranceModel]] = None,
                                         capitalRedemption: Option[Seq[CapitalRedemptionModel]] = None,
                                         lifeAnnuity: Option[Seq[LifeAnnuityModel]] = None,
                                         voidedIsa: Option[Seq[VoidedIsaModel]] = None,
                                         foreign: Option[Seq[ForeignModel]] = None,
                                         prior: Option[GainsPriorDataModel],
                                         isUpdate: Boolean,
                                         nino: String,
                                         mtditid: String,
                                         userType: String,
                                         taxYear: Int)

object CreateOrAmendGainsAuditDetail {

  def createFromCyaData(cyaData: Option[GainsSubmissionModel],
                        prior: Option[GainsPriorDataModel],
                        isUpdate: Boolean,
                        nino: String,
                        mtditid: String,
                        userType: String,
                        taxYear: Int): CreateOrAmendGainsAuditDetail = {
    CreateOrAmendGainsAuditDetail(
      cyaData.flatMap(_.lifeInsurance),
      cyaData.flatMap(_.capitalRedemption),
      cyaData.flatMap(_.lifeAnnuity),
      cyaData.flatMap(_.voidedIsa),
      cyaData.flatMap(_.foreign),
      prior, isUpdate, nino, mtditid, userType, taxYear
    )
  }
  implicit def writes: OWrites[CreateOrAmendGainsAuditDetail] = Json.writes[CreateOrAmendGainsAuditDetail]
}
