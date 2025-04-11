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

package support.mocks

import models.requests.AuthorisationRequest
import models.{Journey, UserAnswersModel}
import org.apache.pekko.Done
import org.scalamock.handlers.{CallHandler2, CallHandler4}
import org.scalamock.scalatest.MockFactory
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockUserAnswersService extends MockFactory {

  lazy val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]

  object MockUserAnswersService {

    def get(taxYear: Int, journey: Journey): CallHandler4[Int, Journey, HeaderCarrier, AuthorisationRequest[_], Future[Option[UserAnswersModel]]] =
      (mockUserAnswersService.get(_: Int, _: Journey)(_: HeaderCarrier, _: AuthorisationRequest[_]))
        .expects(taxYear, journey, *, *)

    def set(userAnswers: UserAnswersModel): CallHandler2[UserAnswersModel, HeaderCarrier, Future[UserAnswersModel]] =
      (mockUserAnswersService.set(_: UserAnswersModel)(_: HeaderCarrier))
        .expects( where { (actualAnswers, _) =>
          actualAnswers.mtdItId == userAnswers.mtdItId &&
            actualAnswers.taxYear == userAnswers.taxYear &&
            actualAnswers.nino == userAnswers.nino &&
            actualAnswers.data == userAnswers.data
        })

    def delete(taxYear: Int, journey: Journey): CallHandler4[Int, Journey, HeaderCarrier, AuthorisationRequest[_], Future[Done]] =
      (mockUserAnswersService.delete(_: Int, _: Journey)(_: HeaderCarrier, _: AuthorisationRequest[_]))
        .expects(taxYear, journey, *, *)
  }
}
