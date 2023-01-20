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
import models.gains.prior.GainsPriorDataModel
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK, SERVICE_UNAVAILABLE}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys.{INTERNAL_SERVER_ERROR_FROM_IF, SERVICE_UNAVAILABLE_FROM_IF, UNEXPECTED_RESPONSE_FROM_IF}
import utils.PagerDutyHelper.pagerDutyLog

object GetGainsHttpParser extends Parser {
  type GetGainsResponse = Either[ApiError, GainsPriorDataModel]

  override val parserName: String = "GetGainsHttpParser"
  override val service: String = "income-tax-additional-information"

  implicit object GetGainsDataHttpReads extends HttpReads[GetGainsResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetGainsResponse = {
      response.status match {
        case OK => response.json.validate[GainsPriorDataModel].fold[GetGainsResponse](
          _ => badSuccessJsonResponse,
          parsedModel => Right(parsedModel)
        )
        case NO_CONTENT => Right(GainsPriorDataModel())
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_IF, response.body)
          handleError(response, response.status)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_IF, response.body)
          handleError(response, response.status)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_IF, response.body)
          handleError(response, INTERNAL_SERVER_ERROR)
      }
    }
  }
}
