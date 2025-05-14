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

import connectors.httpParsers.UserSessionDataHttpReads.{UserSessionDataResponse, SessionDataResponseReads}
import play.api.ConfigLoader
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait UserSessionDataConnector {
  def getSessionData(implicit hc: HeaderCarrier): Future[UserSessionDataResponse]
}

case class SessionDataConnectorConfig(vcSessionServiceBaseUrl: String)
object SessionDataConnectorConfig {
  implicit val configLoader: ConfigLoader[SessionDataConnectorConfig] = {
    ConfigLoader(
      config =>
        _ => {
          Try(config.getString("microservice.services.income-tax-session-data.url"))
            .fold(
              _ => {
                val protocol = config.getString("microservice.services.income-tax-session-data.protocol")
                val host     = config.getString("microservice.services.income-tax-session-data.host")
                val port     = config.getInt("microservice.services.income-tax-session-data.port")
                SessionDataConnectorConfig(s"$protocol://$host:$port")
              },
              SessionDataConnectorConfig(_),
            )
        }
    )
  }
}

@Singleton
class UserSessionDataConnectorImpl @Inject()(
  config: SessionDataConnectorConfig,
  httpClient: HttpClientV2
)(implicit ec: ExecutionContext) extends UserSessionDataConnector {

  def getSessionData(implicit hc: HeaderCarrier): Future[UserSessionDataResponse] = {
    val url = s"${config.vcSessionServiceBaseUrl}/income-tax-session-data"
    httpClient
      .get(url"$url")
      .execute[UserSessionDataResponse]
  }

}
