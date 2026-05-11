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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockUserAnswersConnector extends MockitoSugar {

  val mockUserAnswersConnector: UserAnswersConnector = mock[UserAnswersConnector]

  def mockUserAnswersConnectorGet(taxYear: Int, journey: Journey)(result: UserAnswersResponse[Option[UserAnswersModel]]): Unit =
    when(mockUserAnswersConnector.get(eqTo(taxYear), eqTo(journey))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockUserAnswersConnectorSet(userAnswers: UserAnswersModel)(result: UserAnswersResponse[Done]): Unit =
    when(mockUserAnswersConnector.set(eqTo(userAnswers))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))

  def mockUserAnswersConnectorDelete(taxYear: Int, journey: Journey)(result: UserAnswersResponse[Done]): Unit =
    when(mockUserAnswersConnector.delete(eqTo(taxYear), eqTo(journey))(any[HeaderCarrier]()))
      .thenReturn(Future.successful(result))
}
