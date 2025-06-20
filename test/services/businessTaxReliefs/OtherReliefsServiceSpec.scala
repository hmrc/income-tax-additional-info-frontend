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

package services.businessTaxReliefs

import connectors.businessTaxReliefs.OtherReliefsConnector
import connectors.errors.OtherReliefsSubmissionException
import models.businessTaxReliefs.OtherReliefs
import models.{BusinessTaxReliefs, Done, UserAnswersModel}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.businessTaxReliefs._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class OtherReliefsServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with ScalaFutures with OptionValues {

  val taxYear = 2099
  val emptyUserAnswers: UserAnswersModel =
    UserAnswersModel("", "NINO-VALUE", 8888, BusinessTaxReliefs)

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val connector = mock[OtherReliefsConnector]
    val service = new OtherReliefsService(connector)
  }

  "OtherReliefsServiceImpl" - {
    "returns a successful Future" - {
      "when the session doesn't have any of the value" in new Setup {
        val userAnswers = emptyUserAnswers

        val result = service.submit(taxYear, userAnswers)

        result.futureValue mustEqual Done
      }

      "when qualifyingLoanInterestPayments is defined" in new Setup {
        val otherReliefs = OtherReliefs(Some(BigDecimal(1)), None, None)

        val userAnswers =
          emptyUserAnswers
            .set(QualifyingLoanReliefPage, otherReliefs.qualifyingLoanInterestPayments.head)

        when(connector.submit(any(), any(), any())(any()))
          .thenReturn(Future.successful(Done))

        val result = service.submit(taxYear, userAnswers)

        result.futureValue mustEqual Done
        verify(connector, times(1)).submit(eqTo(emptyUserAnswers.nino), eqTo(taxYear), eqTo(otherReliefs))(any())
      }

      "when postCessationTradeReliefAndCertainOtherLosses is defined" in new Setup {
        val otherReliefs = OtherReliefs(None, Some(BigDecimal(1)), None)

        val userAnswers =
          emptyUserAnswers
            .set(PostCessationTradeReliefPage, otherReliefs.postCessationTradeReliefAndCertainOtherLosses.head)

        when(connector.submit(any(), any(), any())(any()))
          .thenReturn(Future.successful(Done))

        val result = service.submit(taxYear, userAnswers)

        result.futureValue mustEqual Done
        verify(connector, times(1)).submit(eqTo(emptyUserAnswers.nino), eqTo(taxYear), eqTo(otherReliefs))(any())
      }

      "when nonDeductibleReliefs is defined" in new Setup {
        val otherReliefs = OtherReliefs(None, None, Some(BigDecimal(1)))

        val userAnswers =
          emptyUserAnswers
            .set(NonDeductibleReliefsPage, otherReliefs.nonDeductableLoanInterest.head)

        when(connector.submit(any(), any(), any())(any()))
          .thenReturn(Future.successful(Done))

        val result = service.submit(taxYear, userAnswers)

        result.futureValue mustEqual Done
        verify(connector, times(1)).submit(eqTo(emptyUserAnswers.nino), eqTo(taxYear), eqTo(otherReliefs))(any())
      }


      "when qualifyingLoanInterestPayments, postCessationTradeReliefAndCertainOtherLosses, nonDeductibleReliefs is defined" in new Setup {
        val otherReliefs = OtherReliefs(Some(BigDecimal(1)), Some(BigDecimal(2)), Some(BigDecimal(3)))

        val userAnswers =
          emptyUserAnswers
            .set(QualifyingLoanReliefPage, otherReliefs.qualifyingLoanInterestPayments.head)
            .set(PostCessationTradeReliefPage, otherReliefs.postCessationTradeReliefAndCertainOtherLosses.head)
            .set(NonDeductibleReliefsPage, otherReliefs.nonDeductableLoanInterest.head)

        when(connector.submit(any(), any(), any())(any()))
          .thenReturn(Future.successful(Done))

        val result = service.submit(taxYear, userAnswers)

        result.futureValue mustEqual Done
        verify(connector, times(1)).submit(eqTo(emptyUserAnswers.nino), eqTo(taxYear), eqTo(otherReliefs))(any())
      }
    }

    "returns a future failed when there is an error when submitting" in new Setup {
      val otherReliefs = OtherReliefs(None, None, Some(BigDecimal(1)))

      val userAnswers =
        emptyUserAnswers
          .set(NonDeductibleReliefsPage, otherReliefs.nonDeductableLoanInterest.head)

      val exception = OtherReliefsSubmissionException(500)
      when(connector.submit(any(), any(), any())(any()))
        .thenReturn(Future.failed(exception))

      val result = service.submit(taxYear, userAnswers).failed

      result.futureValue mustEqual exception
      verify(connector, times(1)).submit(eqTo(emptyUserAnswers.nino), eqTo(taxYear), eqTo(otherReliefs))(any())

    }
  }
}
