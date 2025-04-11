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

import connectors.errors.UnexpectedErrorResponse
import models.requests.AuthorisationRequest
import models.{BusinessTaxReliefs, UserAnswersModel}
import org.apache.pekko.Done
import play.api.http.Status.INTERNAL_SERVER_ERROR
import support.UnitTest
import support.builders.UserBuilder.aUser
import support.builders.requests.AuthorisationRequestBuilder
import support.mocks.MockUserAnswersConnector
import support.providers.TaxYearProvider
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersServiceSpec extends UnitTest with MockUserAnswersConnector with TaxYearProvider {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val request: AuthorisationRequest[_] = AuthorisationRequestBuilder.anAuthorisationRequest

  lazy val service = new UserAnswersService(mockUserAnswersConnector)

  val emptyUserAnswers: UserAnswersModel = UserAnswersModel(
    mtdItId = aUser.mtditid,
    nino = aUser.nino,
    taxYear = taxYear,
    journey = BusinessTaxReliefs
  )

  ".get()" when {

    "connector returns success from downstream with data" should {

      "return Some(UserAnswers)" in {

        MockUserAnswersConnector.get(taxYear, BusinessTaxReliefs).returns(Future.successful(Right(Some(emptyUserAnswers))))
        await(service.get(taxYear, BusinessTaxReliefs)) shouldBe Some(emptyUserAnswers)
      }
    }

    "connector returns success from downstream with no data" should {

      "return None" in {

        MockUserAnswersConnector.get(taxYear, BusinessTaxReliefs).returns(Future.successful(Right(None)))
        await(service.get(taxYear, BusinessTaxReliefs)) shouldBe None
      }
    }

    "should throw Exception" when {

      "connector returns failure from downstream" in {

        MockUserAnswersConnector.get(taxYear, BusinessTaxReliefs).returns(Future.successful(Left(UnexpectedErrorResponse(INTERNAL_SERVER_ERROR, "bang"))))
        intercept[Exception](await(service.get(taxYear, BusinessTaxReliefs))).getMessage shouldBe
          s"Failed to retrieve UserAnswers from income-tax-additional-information for taxYear: '$taxYear', journey: '$BusinessTaxReliefs', mtditid: '${request.user.mtditid}'"
      }
    }
  }

  ".set()" when {

    "connector returns success from downstream" should {

      "return the saved UserAnswers" in {

        MockUserAnswersConnector.set(emptyUserAnswers).returns(Future.successful(Right(Done)))
        await(service.set(emptyUserAnswers)) shouldBe emptyUserAnswers
      }
    }

    "connector returns failure from downstream" should {

      "throw Exception" in {

        MockUserAnswersConnector.set(emptyUserAnswers).returns(Future.successful(Left(UnexpectedErrorResponse(INTERNAL_SERVER_ERROR, "bang"))))
        intercept[Exception](await(service.set(emptyUserAnswers))).getMessage shouldBe
          s"Failed to store UserAnswers in income-tax-additional-information for for taxYear: '${emptyUserAnswers.taxYear}', journey: '${emptyUserAnswers.journey}', mtditid: '${emptyUserAnswers.mtdItId}'"
      }
    }
  }

  ".delete()" when {

    "connector returns success from downstream" should {

      "return Done" in {

        MockUserAnswersConnector.delete(taxYear, BusinessTaxReliefs).returns(Future.successful(Right(Done)))
        await(service.delete(taxYear, BusinessTaxReliefs)) shouldBe Done
      }
    }

    "connector returns failure from downstream" should {

      "throw UserAnswersException" in {

        MockUserAnswersConnector.delete(taxYear, BusinessTaxReliefs).returns(Future.successful(Left(UnexpectedErrorResponse(INTERNAL_SERVER_ERROR, "bang"))))
        intercept[Exception](await(service.delete(taxYear, BusinessTaxReliefs))).getMessage shouldBe
          s"Failed to delete UserAnswers from income-tax-additional-information for taxYear: '$taxYear', journey: '$BusinessTaxReliefs', mtditid: '${request.user.mtditid}'"
      }
    }
  }
}
