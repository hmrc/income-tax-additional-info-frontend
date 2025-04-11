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

package actions

import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.util.UUID

trait HeaderCarrierHelper extends Logging {

  def hcWithCorrelationId(request: Request[_]): HeaderCarrier =
    HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      .withExtraHeaders("X-CorrelationId" -> correlationId(request.headers.get("CorrelationId")))

  private def correlationId(correlationIdHeader: Option[String]): String =
    if (correlationIdHeader.isDefined) {
      logger.info("[AuthorisedAction]Valid CorrelationId header found.")
      correlationIdHeader.get
    } else {
      lazy val id = UUID.randomUUID().toString
      logger.warn(s"[AuthorisedAction]No valid CorrelationId found in headers. Defaulting Correlation Id. $id")
      id
    }
}
