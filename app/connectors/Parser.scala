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

package connectors

import connectors.errors.{ApiError, ApiErrorBody, SingleErrorBody}
import play.api.Logging
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsPath, JsonValidationError}
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys.BAD_SUCCESS_JSON_FROM_IF
import utils.PagerDutyHelper.formatErrorMessage

import scala.util.control.NonFatal

trait Parser extends Logging {

  protected val parserName: String
  protected val service: String

  def logMessage(response: HttpResponse): Option[String] = {
    Some(s"[$parserName][read] Received ${response.status} from $service API. Body:${response.body}")
  }

  def badSuccessJsonFromAPIWithErrors[Response](
    validationErrors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]
  ): Either[ApiError, Response] = {
    val otherDetails = s"[$parserName][badSuccessJsonFromAPIWithErrors] Invalid Json response. " + validationErrors
    logger.error(formatErrorMessage(BAD_SUCCESS_JSON_FROM_IF, otherDetails))
    Left(
      ApiError(
        INTERNAL_SERVER_ERROR,
        SingleErrorBody("PARSING_ERROR", "Error parsing response from API - " + validationErrors)
      )
    )
  }

  def handleError[Response](response: HttpResponse, status: Int): Either[ApiError, Response] = {
    try {
      response.json.asOpt[ApiErrorBody] match {
        case Some(error) => Left(ApiError(status, error))
        case None => Left(ApiError(status, SingleErrorBody.parsingError))
      }
    } catch {
      case NonFatal(_) => Left(ApiError(status, SingleErrorBody.parsingError))
    }
  }
}
