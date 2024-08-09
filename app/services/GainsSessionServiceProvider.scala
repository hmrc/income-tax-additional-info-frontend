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

import connectors.httpParsers.GetGainsHttpParser.GetGainsResponse
import models.AllGainsSessionModel
import models.gains.prior.GainsPriorDataModel
import models.mongo.{DatabaseError, GainsUserDataModel}
import models.requests.AuthorisationRequest
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}


trait GainsSessionServiceProvider {

  def getPriorData(taxYear: Int)(implicit request: AuthorisationRequest[_], hc: HeaderCarrier): Future[GetGainsResponse]

  def createSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[A]

  def getSessionData(taxYear: Int)(implicit request: AuthorisationRequest[_],
                                   ec: ExecutionContext, hc: HeaderCarrier): Future[Either[DatabaseError, Option[GainsUserDataModel]]]

  def updateSessionData[A](cyaModel: AllGainsSessionModel, taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[A]

  def deleteSessionData[A](taxYear: Int)(onFail: A)(onSuccess: A)
                          (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[A]

  def getAndHandle[R](taxYear: Int)(onFail: R)(block: (Option[AllGainsSessionModel], Option[GainsPriorDataModel]) => R)
                     (implicit request: AuthorisationRequest[_], ec: ExecutionContext, hc: HeaderCarrier): Future[R]

}
