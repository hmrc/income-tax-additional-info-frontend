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

import connectors.GainsSubmissionConnector
import connectors.httpParsers.GainsSubmissionHttpParser._
import models.gains._
import play.api.Logger
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GainsSubmissionService @Inject()(gainsSubmissionConnector: GainsSubmissionConnector) {

  def submitGains(body: Option[GainsSubmissionModel], nino: String, mtditid: String, taxYear: Int)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GainsSubmissionResponse] = {

    lazy val logger: Logger = Logger(this.getClass.getName)

    body match {
      case Some(GainsSubmissionModel(None, None, None, None, None)) | None =>
        logger.info("[GainsSubmissionService][submitGains] User has no data inSession to submit" +
          "Not submitting data to IF.")
        Future(Right(NO_CONTENT))
      case Some(gainsSubmissionModel) =>
        gainsSubmissionConnector.submitGains(gainsSubmissionModel, nino, taxYear)(hc.withExtraHeaders("mtditid" -> mtditid))
    }
  }

}