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

package connectors.httpParsers

import connectors.errors.{ErrorResponse, FailedToDeleteUserAnswers, FailedToParseJson, FailedToStoreUserAnswers, UnexpectedErrorResponse}
import models.UserAnswersModel
import org.apache.pekko.Done
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object UserAnswersHttpReads {

  type UserAnswersResponse[T] = Either[ErrorResponse, T]

  implicit object StoreOrDeleteUserAnswersResponseReads extends HttpReads[UserAnswersResponse[Done]] with Logging {
    override def read(method: String, url: String, response: HttpResponse): UserAnswersResponse[Done] = response.status match {
      case NO_CONTENT => Right(Done)
      case status =>
        val message = s"Received status '$status' from income-tax-additional-information, with message: ${response.body}"
        logger.warn(s"$message")
        Left(if(method == "PUT") {
          FailedToStoreUserAnswers(status, response.body)
        } else {
          FailedToDeleteUserAnswers(status, response.body)
        })
    }
  }

  implicit object GetUserAnswersResponseReads extends HttpReads[UserAnswersResponse[Option[UserAnswersModel]]] with Logging {
    override def read(method: String, url: String, response: HttpResponse): UserAnswersResponse[Option[UserAnswersModel]] =
      response.status match {
        case NO_CONTENT => Right(None)
        case OK =>
          response.json.validate[UserAnswersModel] match {
            case JsSuccess(answers, _) => Right(Some(answers))
            case JsError(errors) =>
              logger.warn(s"Failed to parse JSON response. Errors:\n${errors.mkString("\n")}")
              Left(FailedToParseJson(errors.toSeq))
          }
        case status =>
          logger.warn(s"Received status '$status' from income-tax-additional-information, with message: ${response.body}")
          Left(UnexpectedErrorResponse(status, response.body))
      }
  }
}
