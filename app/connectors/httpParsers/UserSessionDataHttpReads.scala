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

import models.session.UserSessionData
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.HttpResponse
import connectors.Parser
import connectors.errors._
import utils.PagerDutyHelper.formatErrorMessage
import utils.PagerDutyHelper.PagerDutyKeys._
import play.api.Logging


object UserSessionDataHttpReads extends Parser {
  type UserSessionDataResponse = Either[ApiError, Option[UserSessionData]]

  override val parserName: String = getClass().getSimpleName().replace("$", "")

  override val service: String = "income-tax-session-data"

  implicit object SessionDataResponseReads extends HttpReads[UserSessionDataResponse] with Logging {
    override def read(method: String, url: String, response: HttpResponse): UserSessionDataResponse = {
      response.status match  {
        case OK =>
          response.json.validate[UserSessionData].fold[UserSessionDataResponse](
            validationErrors => badSuccessJsonFromAPIWithErrors(validationErrors),
            parsedModel => Right(Some(parsedModel))
          )
        case NOT_FOUND | NO_CONTENT =>
          logger.error(formatErrorMessage(FOURXX_RESPONSE_FROM_IF, logMessage(response).get))
          Right(None)
        case SERVICE_UNAVAILABLE =>
          logger.error(formatErrorMessage(SERVICE_UNAVAILABLE_FROM_IF, logMessage(response).get))
          handleError(response, response.status)
        case INTERNAL_SERVER_ERROR =>
          logger.error(formatErrorMessage(INTERNAL_SERVER_ERROR_FROM_IF, logMessage(response).get))
          handleError(response, response.status)
        case _ =>
          logger.error(formatErrorMessage(UNEXPECTED_RESPONSE_FROM_IF, logMessage(response).get))
          handleError(response, INTERNAL_SERVER_ERROR)
      }
    }
  }
}
