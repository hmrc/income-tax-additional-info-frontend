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

package services

import connectors.session.UserAnswersConnector
import models.requests.AuthorisationRequest
import models.{Journey, UserAnswersModel}
import org.apache.pekko.Done
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class UserAnswersService @Inject()(userAnswersConnector: UserAnswersConnector)(implicit ec: ExecutionContext) {

  def get(taxYear: Int, journey: Journey)(implicit hc: HeaderCarrier, request: AuthorisationRequest[_]): Future[Option[UserAnswersModel]] =
    userAnswersConnector.get(taxYear, journey)(hc.withExtraHeaders("mtditid" -> request.user.mtditid)).map {
      case Right(answers) => answers
      case Left(_) => throw new Exception(
        s"Failed to retrieve UserAnswers from income-tax-additional-information for taxYear: '$taxYear', journey: '$journey', mtditid: '${request.user.mtditid}'"
      )
    }

  def set(answers: UserAnswersModel)(implicit hc: HeaderCarrier): Future[UserAnswersModel] = {
    userAnswersConnector.set(answers)(hc.withExtraHeaders("mtditid" -> answers.mtdItId)).map {
      case Right(_) => answers
      case Left(_) => throw new Exception(
        s"Failed to store UserAnswers in income-tax-additional-information for for taxYear: '${answers.taxYear}', journey: '${answers.journey}', mtditid: '${answers.mtdItId}'"
      )
    }
  }

  def delete(taxYear: Int, journey: Journey)(implicit hc: HeaderCarrier, request: AuthorisationRequest[_]): Future[Done] =
    userAnswersConnector.delete(taxYear, journey)(hc.withExtraHeaders("mtditid" -> request.user.mtditid)).map {
      case Right(response) => response
      case Left(_) => throw new Exception(
        s"Failed to delete UserAnswers from income-tax-additional-information for taxYear: '$taxYear', journey: '$journey', mtditid: '${request.user.mtditid}'"
      )
    }
}
