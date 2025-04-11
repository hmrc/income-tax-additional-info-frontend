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

package connectors.session

import config.AppConfig
import connectors.httpParsers.UserAnswersHttpParser._
import models.{Journey, UserAnswersModel}
import org.apache.pekko.Done
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserAnswersConnector @Inject()(val http: HttpClientV2,
                                     appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  def set(body: UserAnswersModel)(implicit hc: HeaderCarrier): Future[UserAnswersResponse[Done]] =
    http
      .put(url"${appConfig.additionalInformationServiceBaseUrl + s"/income-tax/user-answers"}")
      .withBody(Json.toJson(body))
      .execute[UserAnswersResponse[Done]]

  def get(taxYear: Int, journey: Journey)(implicit hc: HeaderCarrier): Future[UserAnswersResponse[Option[UserAnswersModel]]] =
    http
      .get(url"${appConfig.additionalInformationServiceBaseUrl + s"/income-tax/user-answers/$taxYear/$journey"}")
      .execute[UserAnswersResponse[Option[UserAnswersModel]]]

  def delete(taxYear: Int, journey: Journey)(implicit hc: HeaderCarrier): Future[UserAnswersResponse[Done]] =
    http
      .delete(url"${appConfig.additionalInformationServiceBaseUrl + s"/income-tax/user-answers/$taxYear/$journey"}")
      .execute[UserAnswersResponse[Done]]
}
