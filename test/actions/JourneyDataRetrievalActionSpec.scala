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
import models.{BusinessTaxReliefs, UserAnswersModel}
import play.api.mvc.ActionTransformer
import support.UnitTest
import support.mocks.MockUserAnswersService
import support.utils.TaxYearUtils.taxYear

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyDataRetrievalActionSpec extends UnitTest with MockUserAnswersService {

  lazy val action: ActionTransformer[AuthorisationRequest, JourneyDataRequest] =
    new JourneyDataRetrievalActionImpl(mockUserAnswersService).apply(taxYear, BusinessTaxReliefs)

  val userAnswers: UserAnswersModel = emptyUserAnswers(taxYear, BusinessTaxReliefs)

  "Journey Data Retrieval Action" when {

    "there is no data in the cache" should {

      "setup some empty UserAnswers for the journey" in {

        MockUserAnswersService.get(taxYear, BusinessTaxReliefs).returns(Future.successful(None))
        MockUserAnswersService.set(userAnswers).returns(Future.successful(userAnswers))

        val result = await(action.refine(individualRequest))
        result shouldBe Right(
          JourneyDataRequest(
            user = individualRequest.user,
            request = individualRequest,
            userAnswers = userAnswers
          )
        )
      }
    }

    "when there is data in the cache" should {

      "must build a userAnswers object and add it to the request" in {

        MockUserAnswersService.get(taxYear, BusinessTaxReliefs).returns(Future.successful(Some(userAnswers)))

        val result = await(action.refine(individualRequest))
        result shouldBe Right(
          JourneyDataRequest(
            user = individualRequest.user,
            request = individualRequest,
            userAnswers = userAnswers
          )
        )
      }
    }
  }
}
