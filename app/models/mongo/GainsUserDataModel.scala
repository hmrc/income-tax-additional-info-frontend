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

import models.gains.{EncryptedGainsCyaModel, GainsCyaModel}
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.mongo.play.json.formats.MongoJodaFormats.dateTimeFormat

case class GainsUserDataModel(
                          sessionId: String,
                          mtdItId: String,
                          nino: String,
                          taxYear: Int,
                          gains: Option[GainsCyaModel] = None,
                          lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC)
                        ) extends UserDataTemplate

object GainsUserDataModel {

  implicit val mongoJodaDateTimeFormats: Format[DateTime] = dateTimeFormat

  implicit val format: OFormat[GainsUserDataModel] = Json.format[GainsUserDataModel]
}

case class EncryptedGainsUserDataModel(
                                   sessionId: String,
                                   mtdItId: String,
                                   nino: String,
                                   taxYear: Int,
                                   gains: Option[EncryptedGainsCyaModel] = None,
                                   lastUpdated: DateTime = DateTime.now(DateTimeZone.UTC)
                                 ) extends UserDataTemplate

object EncryptedGainsUserDataModel {

  implicit val mongoJodaDateTimeFormats: Format[DateTime] = dateTimeFormat


  implicit val format: Format[EncryptedGainsUserDataModel] = Json.format[EncryptedGainsUserDataModel]
}