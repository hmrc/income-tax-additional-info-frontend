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

import connectors.GetGainsConnector
import connectors.httpParsers.GetGainsHttpParser.GetGainsResponse
import models.AllGainsSessionModel
import models.gains.prior.GainsPriorDataModel
import models.mongo.{DatabaseError, GainsUserDataModel}
import models.requests.AuthorisationRequest
import org.joda.time.{DateTime, DateTimeZone}
import play.api.i18n.Lang.logger
import repositories.GainsUserDataRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GainsSessionService @Inject()(
                                     gainsUserDataRepository: GainsUserDataRepository,
                                     getGainsDataConnector: GetGainsConnector
                                   ) {

  def getPriorData(taxYear: Int)(implicit request: AuthorisationRequest[_], hc: HeaderCarrier): Future[GetGainsResponse] = {
    getGainsDataConnector.getUserData(taxYear)(request.user, hc.withExtraHeaders("mtditid" -> request.user.mtditid))
  }

  def createSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext): Future[A] = {

    val userData = GainsUserDataModel(
      request.user.sessionId,
      request.user.mtditid,
      request.user.nino,
      taxYear,
      Some(cyaModel),
      DateTime.now(DateTimeZone.UTC)
    )

    gainsUserDataRepository.create(userData).map {
      case Right(_) => onSuccess
      case Left(_) => onFail

    }
  }

  def getSessionData(taxYear: Int)(implicit request: AuthorisationRequest[_],
                                   ec: ExecutionContext): Future[Either[DatabaseError, Option[GainsUserDataModel]]] = {

    gainsUserDataRepository.find(taxYear).map {
      case Left(error) =>
        logger.error("[GainsSessionService][getSessionData] Could not find user session.")
        Left(error)
      case Right(userData) => Right(userData)
    }
  }

  def updateSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext): Future[A] = {

    val userData = GainsUserDataModel(
      request.user.sessionId,
      request.user.mtditid,
      request.user.nino,
      taxYear,
      Some(cyaModel),
      DateTime.now(DateTimeZone.UTC)
    )

    gainsUserDataRepository.update(userData).map {
      case Right(_) => onSuccess
      case Left(_) => onFail
    }

  }

  def deleteSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext): Future[A] = {

    gainsUserDataRepository.clear(taxYear)(request.user).map {
      case true => onSuccess
      case _ => onFail
    }

  }

  def getAndHandle[R](taxYear: Int)(onFail: R)(block: (Option[AllGainsSessionModel], Option[GainsPriorDataModel]) => R)
                     (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[R] = {
    for {
      optionalCya <- getSessionData(taxYear)
      priorDataResponse <- getPriorData(taxYear)
    } yield {
      priorDataResponse match {
        case Right(prior) => optionalCya match {
          case Left(_) => onFail
          case Right(cyaData) => block(cyaData.flatMap(_.gains), Option(prior))
        }
        case Left(_) => onFail
      }
    }
  }
}
