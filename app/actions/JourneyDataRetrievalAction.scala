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

package actions

import models.requests.{AuthorisationRequest, JourneyDataRequest}
import models.{Journey, UserAnswersModel}
import play.api.Logging
import play.api.mvc.ActionTransformer
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyDataRetrievalActionImpl @Inject()(val userAnswersService: UserAnswersService)
                                              (implicit val ec: ExecutionContext) extends JourneyDataRetrievalAction with HeaderCarrierHelper with Logging {

  def apply(taxYear: Int, journey: Journey): ActionTransformer[AuthorisationRequest, JourneyDataRequest] =
    new ActionTransformer[AuthorisationRequest, JourneyDataRequest] {

      override val executionContext: ExecutionContext = ec

      override protected def transform[A](request: AuthorisationRequest[A]): Future[JourneyDataRequest[A]] = {

        implicit val hc: HeaderCarrier = hcWithCorrelationId(request)

        userAnswersService.get(taxYear, journey)(hc, request).flatMap {
          case Some(answers) =>
            Future.successful(JourneyDataRequest(request.user, request, answers))
          case _ =>
            logger.info(s"No user answers found for taxYear: $taxYear and journey: $journey. Creating new user answers entry.")
            userAnswersService.set(UserAnswersModel(request.user.mtditid, request.user.nino, taxYear, journey)).map {
              JourneyDataRequest(request.user, request, _)
            }
        }
      }
    }
}

trait JourneyDataRetrievalAction {
  def apply(taxYear: Int, journey: Journey): ActionTransformer[AuthorisationRequest, JourneyDataRequest]
}
