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

package connectors.httpParsers

import connectors.Parser
import connectors.errors.ApiError
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys.{FOURXX_RESPONSE_FROM_IF, INTERNAL_SERVER_ERROR_FROM_IF, SERVICE_UNAVAILABLE_FROM_IF, UNEXPECTED_RESPONSE_FROM_IF}
import utils.PagerDutyHelper._

object DeleteGainsSessionHttpParser extends Parser with Logging {
  type DeleteGainsSessionResponse = Either[ApiError, Boolean]

  override val parserName: String = "DeleteGainsSessionHttpParser"
  override val service: String = "income-tax-additional-information"

  implicit object DeleteGainsHttpReads extends HttpReads[DeleteGainsSessionResponse] {
    override def read(method: String, url: String, response: HttpResponse): DeleteGainsSessionResponse = response.status match {
      case NO_CONTENT => Right(true)
      case INTERNAL_SERVER_ERROR =>
        pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_IF, response.body)
        handleError(response, response.status)
      case SERVICE_UNAVAILABLE =>
        pagerDutyLog(SERVICE_UNAVAILABLE_FROM_IF, response.body)
        handleError(response, response.status)
      case BAD_REQUEST | NOT_FOUND =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_IF, response.body)
        handleError(response, response.status)
      case _ =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_IF, response.body)
        handleError(response, response.status)
    }
  }
}