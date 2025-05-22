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

import config.FeatureConfig
import connectors.session.UserSessionDataConnector
import models.authorisation.SessionValues
import models.session.UserSessionData
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

case class MissingAgentClientDetails(message: String) extends Exception(message) with NoStackTrace


@Singleton
class SessionDetailsService @Inject()(sessionDataConnector: UserSessionDataConnector,
                                      featureConfig: FeatureConfig)
                                     (implicit ec: ExecutionContext) extends Logging {

  logger.info(s"Feature switch sessionCookieServiceEnabled is ${featureConfig.sessionCookieServiceEnabled}")

  def getSessionData[A](sessionId: String)(implicit request: Request[A], hc: HeaderCarrier): Future[UserSessionData] = {
      logger.debug("Checking income-tax-session-store for session data")
      getViewAndChangeSessionData.map { viewAndChangeSessionData =>
        (viewAndChangeSessionData orElse extractSessionHeaders(sessionId)).getOrElse {
          throw new MissingAgentClientDetails("Session Data service and Session Cookie both returned empty data")
        }
      }
  }

  private def getViewAndChangeSessionData(implicit hc: HeaderCarrier): Future[Option[UserSessionData]] =
    if (featureConfig.sessionCookieServiceEnabled) {
      sessionDataConnector.getSessionData.map(_.toOption.flatten)
    } else {
      Future.successful(None)
    }

  private def extractSessionHeaders[A](sessionId: String)(implicit request: Request[A]): Option[UserSessionData] = {
    logger.debug("Extracting session headers for session data")
    (
      request.session.get(SessionValues.CLIENT_NINO),
      request.session.get(SessionValues.CLIENT_MTDITID)
    ) match {
      case (Some(nino), Some(mtdItId)) => Some(UserSessionData(sessionId, mtdItId, nino))
      case _ =>
        logger.debug(s"Extracting session headers failed for ${SessionValues.CLIENT_NINO}, ${SessionValues.CLIENT_MTDITID}")
        None
    }
  }
}
