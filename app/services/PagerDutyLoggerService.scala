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

package services

import utils.PagerDutyHelper.PagerDutyKeys._
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.HttpResponse

import javax.inject.Singleton

@Singleton
class PagerDutyLoggerService() extends Logging {

  def pagerDutyLog(httpResponse: HttpResponse, parserName: String): Unit = {
    val logMessage = messageToLog(httpResponse, parserName)
    logger.error(message = logMessage)
  }

  private[services] def messageToLog(httpResponse: HttpResponse, parserName: String): String = {
    val (pagerDutyKey, otherDetail) = httpResponse.status match {
      case OK => (BAD_SUCCESS_JSON_FROM_IF, s"[$parserName][read] Invalid Json from Integration Framework.")
      case INTERNAL_SERVER_ERROR => (INTERNAL_SERVER_ERROR_FROM_IF, logMessage(httpResponse, parserName))
      case SERVICE_UNAVAILABLE => (SERVICE_UNAVAILABLE_FROM_IF, logMessage(httpResponse, parserName))
      case BAD_REQUEST | UNPROCESSABLE_ENTITY => (FOURXX_RESPONSE_FROM_IF, logMessage(httpResponse, parserName))
      case _ => (UNEXPECTED_RESPONSE_FROM_IF, logMessage(httpResponse, parserName))
    }

    s"$pagerDutyKey $otherDetail"
  }

  private def logMessage(response: HttpResponse, parserName: String): String = {
    val correlationId = response.header(key = "CorrelationId").map(id => s" CorrelationId: $id").getOrElse("")
    s"[$parserName][read] Received ${response.status} from Integration Framework. Body:${response.body}" + correlationId
  }
}
