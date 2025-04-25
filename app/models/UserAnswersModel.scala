/*
 * Copyright 2025 HM Revenue & Customs
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

import pages.{QuestionPage, Settable}
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

case class UserAnswersModel(mtdItId: String,
                            nino: String,
                            taxYear: Int,
                            journey: Journey,
                            data: JsObject = Json.obj(),
                            lastUpdated: Instant = Instant.now) {

  def get[A](page: QuestionPage[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).asOpt.flatten

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): UserAnswersModel =
    handleResult { data.setObject(page.path, Json.toJson(value)) }

  def remove[A](page: Settable[A]): UserAnswersModel =
    handleResult { data.removeObject(page.path) }

  private[models] def handleResult: JsResult[JsObject] => UserAnswersModel = {
    case JsSuccess(updatedAnswers, _) => copy(data = updatedAnswers)
    case JsError(errors) => throw JsResultException(errors)
  }
}

object UserAnswersModel {
  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[UserAnswersModel] = Json.format[UserAnswersModel]
}
