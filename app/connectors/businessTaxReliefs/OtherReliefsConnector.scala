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

package connectors.businessTaxReliefs

import connectors.ConnectorConfig
import connectors.errors.OtherReliefsSubmissionException
import models.{Done, User}
import models.businessTaxReliefs.OtherReliefs
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.HttpErrorFunctions._
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import utils.PagerDutyHelper.PagerDutyKeys.{FIVEXX_RESPONSE, FOURXX_RESPONSE, UNEXPECTED_RESPONSE}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


trait OtherReliefsConnector {
  def submit(taxYear: Int, user: User, otherReliefs: OtherReliefs)(implicit hc: HeaderCarrier): Future[Done]
}

@Singleton
class OtherReliefsConnectorImpl @Inject()(config: ConnectorConfig,
                                          httpClient: HttpClientV2
                                          )(implicit ec: ExecutionContext) extends OtherReliefsConnector with Logging {

  def submit(taxYear: Int, user: User, otherReliefs: OtherReliefs)(implicit hc: HeaderCarrier): Future[Done] = {
    val url = url"${config.vcSessionServiceBaseUrl}/income-tax/reliefs/other/${user.nino}/$taxYear"
    httpClient
      .put(url)
      .withBody(Json.toJson(otherReliefs))
      .execute[HttpResponse]
      .map { res =>
        res.status match {
          case status if is2xx(status) =>
            Done

          case status if is4xx(status) =>
            logger.error(s"$FOURXX_RESPONSE status=$status message=${res.body}")
            throw OtherReliefsSubmissionException(status)

          case status if is5xx(status) =>
            logger.warn(s"$FIVEXX_RESPONSE status=$status message=${res.body}")
            throw OtherReliefsSubmissionException(status)

          case status =>
            logger.error(s"$UNEXPECTED_RESPONSE status=$status message=${res.body}")
            throw OtherReliefsSubmissionException(status)
        }
      }
  }

}
