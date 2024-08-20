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

package models.mongo

import models.{AllGainsSessionModel, EncryptedAllGainsSessionModel}

import java.time.Instant
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

case class GainsUserDataModel(
                               sessionId: String,
                               mtdItId: String,
                               nino: String,
                               taxYear: Int,
                               gains: Option[AllGainsSessionModel] = None,
                               lastUpdated: Instant = Instant.now
                             ) extends UserDataTemplate

object GainsUserDataModel {
  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[GainsUserDataModel] = Json.format[GainsUserDataModel]
}

case class EncryptedGainsUserDataModel(
                                        sessionId: String,
                                        mtdItId: String,
                                        nino: String,
                                        taxYear: Int,
                                        gains: Option[EncryptedAllGainsSessionModel] = None,
                                        lastUpdated: Instant = Instant.now
                                      ) extends UserDataTemplate

object EncryptedGainsUserDataModel {
  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val format: Format[EncryptedGainsUserDataModel] = Json.format[EncryptedGainsUserDataModel]
}
