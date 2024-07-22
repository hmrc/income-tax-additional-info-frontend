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
import connectors.errors.ApiError
import connectors.httpParsers.GetGainsHttpParser.GetGainsResponse
import connectors.session.{CreateGainsSessionConnector, DeleteGainsSessionConnector, GetGainsSessionConnector, UpdateGainsSessionConnector}
import models.AllGainsSessionModel
import models.gains.prior.GainsPriorDataModel
import models.mongo.{DataNotFound, DatabaseError, GainsUserDataModel}
import models.requests.AuthorisationRequest
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GainsSessionService @Inject()(getGainsDataConnector: GetGainsConnector,
                                    createGainsSessionConnector: CreateGainsSessionConnector,
                                    updateGainsSessionConnector: UpdateGainsSessionConnector,
                                    deleteGainsSessionConnector: DeleteGainsSessionConnector,
                                    getGainsSessionConnector: GetGainsSessionConnector
                                   )(implicit correlationId: String) extends Logging {

  def getPriorData(taxYear: Int)(implicit request: AuthorisationRequest[_], hc: HeaderCarrier): Future[GetGainsResponse] = {
    getGainsDataConnector.getUserData(taxYear)(request.user, hc.withExtraHeaders("mtditid" -> request.user.mtditid))
  }

  def createSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[A] = {

    createGainsSessionConnector.createSessionData(cyaModel, taxYear)(
      hc.withExtraHeaders("mtditid" -> request.user.mtditid).withExtraHeaders("X-CorrelationId" -> correlationId)).map {
      case Right(res) =>
        println(s"CREATE ONSUCCESS BEING CALLED, STATUS = $res")
        onSuccess
      case Left(value: ApiError) =>
        println("CREATE ONFAIL BEING CALLED")
        logger.error(s"[GainsSessionService][createSessionData] session create failed. correlation id: " + correlationId)
        onFail
    }
  }

  def getSessionData(taxYear: Int)(implicit request: AuthorisationRequest[_],
                                   ec: ExecutionContext, hc: HeaderCarrier): Future[Either[DatabaseError, Option[GainsUserDataModel]]] = {

    getGainsSessionConnector.getSessionData(taxYear)(
      hc.withExtraHeaders("mtditid" -> request.user.mtditid).withExtraHeaders("X-CorrelationId" -> correlationId)).map {
      case Right(userData) =>
        println(s"GET ONSUCCESS BEING CALLED, STATUS = $userData")
        Right(userData)
      case Left(_) =>
        println("GET ONFAIL BEING CALLED")
        logger.error("[GainsSessionService][getSessionData] Could not find user session. correlation id: " + correlationId)
        Left(DataNotFound)
    }
  }

  def updateSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[A] = {

    println(s"UPDATESESSIONDATA")
    updateGainsSessionConnector.updateGainsSession(cyaModel, taxYear)(
      hc.withExtraHeaders("mtditid" -> request.user.mtditid).withExtraHeaders("X-CorrelationId" -> correlationId)).map {
      case Right(_) =>
        onSuccess
      case Left(_) =>
        logger.error(s"[GainsSessionService][updateSessionData] session update failure. correlation id: " + correlationId)
        onFail
    }
  }

  def deleteSessionData[A](taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[A] = {

    deleteGainsSessionConnector.deleteGainsData(taxYear)(
      hc.withExtraHeaders("mtditid" -> request.user.mtditid).withExtraHeaders("X-CorrelationId" -> correlationId)).map {
      case Right(res) =>
        println(s"ONSUCCESS BEING CALLED IN DELETE, STATUS = $res")
        onSuccess
      case _ =>
        logger.error(s"[GainsSessionService][deleteSessionData] session delete failure. correlation id: " + correlationId)
        onFail
    }
  }

  def getAndHandle[R](taxYear: Int)(onFail: R)(block: (Option[AllGainsSessionModel], Option[GainsPriorDataModel]) => R)
                     (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[R] = {
    for {
      optionalCya <- getSessionData(taxYear)
      priorDataResponse: GetGainsResponse <- getPriorData(taxYear)
    } yield {
      priorDataResponse match {
        case Right(prior) => optionalCya match {
          case Left(error) =>
            println(s"OPTIONALCYA_ERROR == $error")
            logger.error(s"[GainsSessionService][getAndHandle] No session data. correlation id: " + correlationId)
            onFail
          case Right(cyaData) => block(cyaData.flatMap(_.gains), prior)
        }
        case Left(_) =>
          logger.error(s"[GainsSessionService][getAndHandle] No prior data. correlation id: " + correlationId)
          onFail
      }
    }
  }
}
