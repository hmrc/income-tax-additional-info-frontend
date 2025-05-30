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

package support

import models.{Journey, UserAnswersModel}
import models.requests.AuthorisationRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import support.builders.UserBuilder.{aUser, anAgentUser}
import uk.gov.hmrc.http.HeaderCarrier

trait UnitTest extends AnyWordSpec with Matchers with MockFactory with BeforeAndAfterEach
  with FutureAwaits with DefaultAwaitTimeout {

  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val agentRequest: AuthorisationRequest[_] = AuthorisationRequest(anAgentUser, FakeRequest())
  val individualRequest: AuthorisationRequest[_] = AuthorisationRequest(aUser, FakeRequest())

  def emptyUserAnswers(taxYear: Int, journey: Journey) = UserAnswersModel(aUser.mtditid, aUser.nino, taxYear, journey)
}
