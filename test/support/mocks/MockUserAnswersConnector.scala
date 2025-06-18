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

import connectors.httpParsers.UserAnswersHttpReads.UserAnswersResponse
import connectors.session.UserAnswersConnector
import models.{Journey, UserAnswersModel}
import org.apache.pekko.Done
import org.scalamock.handlers.{CallHandler2, CallHandler3}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockUserAnswersConnector extends MockFactory { _: TestSuite =>

  lazy val mockUserAnswersConnector: UserAnswersConnector = mock[UserAnswersConnector]

  object MockUserAnswersConnector {

    def get(taxYear: Int, journey: Journey): CallHandler3[Int, Journey, HeaderCarrier, Future[UserAnswersResponse[Option[UserAnswersModel]]]] =
      (mockUserAnswersConnector.get(_: Int, _: Journey)(_: HeaderCarrier)).expects(taxYear, journey, *)

    def set(userAnswers: UserAnswersModel): CallHandler2[UserAnswersModel, HeaderCarrier, Future[UserAnswersResponse[Done]]] =
      (mockUserAnswersConnector.set(_: UserAnswersModel)(_: HeaderCarrier)).expects(userAnswers, *)

    def delete(taxYear: Int, journey: Journey): CallHandler3[Int, Journey, HeaderCarrier, Future[UserAnswersResponse[Done]]] =
      (mockUserAnswersConnector.delete(_: Int, _: Journey)(_: HeaderCarrier)).expects(taxYear, journey, *)
  }
}
