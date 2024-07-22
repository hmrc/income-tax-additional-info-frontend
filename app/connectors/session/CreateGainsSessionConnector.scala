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

package connectors.session

import config.AppConfig
import connectors.httpParsers.CreateGainsSessionHttpParser.CreateGainsSessionResponse
import connectors.httpParsers.GainsSubmissionHttpParser._
import models.AllGainsSessionModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CreateGainsSessionConnector @Inject()(http: HttpClient, appConfig: AppConfig)
                                           (implicit ec: ExecutionContext) {

  def createSessionData(body: AllGainsSessionModel, taxYear: Int)
                       (implicit hc: HeaderCarrier): Future[CreateGainsSessionResponse] = {
    val createGainsSessionUrl: String = appConfig.additionalInformationServiceBaseUrl + s"/income-tax/income/insurance-policies/$taxYear/session"
    println(s"REQUEST BODY === $body")

    http.POST[AllGainsSessionModel, CreateGainsSessionResponse](createGainsSessionUrl, body)
  }
}