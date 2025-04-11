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

package connectors

import connectors.errors.{FailedToDeleteUserAnswers, FailedToParseJson, FailedToStoreUserAnswers, UnexpectedErrorResponse}
import connectors.httpParsers.UserAnswersHttpParser.UserAnswersResponse
import connectors.session.UserAnswersConnector
import models.{BusinessTaxReliefs, Journey, UserAnswersModel}
import org.apache.pekko.Done
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.OK
import test.support.{ConnectorIntegrationTest, IntegrationTest}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UserAnswersConnectorISpec extends IntegrationTest with ConnectorIntegrationTest {

  val connector: UserAnswersConnector = app.injector.instanceOf[UserAnswersConnector]

  implicit override val headerCarrier: HeaderCarrier = HeaderCarrier().withExtraHeaders("mtditid" -> mtditid, "X-Session-ID" -> sessionId)

  val putUrl = s"/income-tax-additional-information/income-tax/user-answers"
  def getUrl(taxYear: Int, journey: Journey): String = s"/income-tax-additional-information/income-tax/user-answers/$taxYear/$journey"
  def deleteUrl(taxYear: Int, journey: Journey): String = getUrl(taxYear, journey)

  val userAnswers: UserAnswersModel = UserAnswersModel(
    mtdItId = mtditid,
    nino = nino,
    taxYear = taxYear,
    journey = BusinessTaxReliefs,
    lastUpdated = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  )

  "UserAnswersConnector" when {

    "calling .get()" when {

      "downstream response is NO_CONTENT" should {

        "return Success with Right(None) as the response" in {

          stubGetWithHeadersCheck(getUrl(taxYear, BusinessTaxReliefs), NO_CONTENT, "")

          val result: UserAnswersResponse[Option[UserAnswersModel]] = Await.result(connector.get(taxYear, BusinessTaxReliefs), Duration.Inf)
          result shouldBe Right(None)
        }
      }

      "downstream response is OK" when {

        "the payload returned is valid and can be parsed successfully" should {

          "return Success with Right(Some(UserAnswersModel)) as the response" in {

            stubGetWithHeadersCheck(getUrl(taxYear, BusinessTaxReliefs), OK, Json.toJson(userAnswers).toString())

            val result: UserAnswersResponse[Option[UserAnswersModel]] = Await.result(connector.get(taxYear, BusinessTaxReliefs), Duration.Inf)
            result shouldBe Right(Some(userAnswers))
          }
        }

        "the payload returned is NOT valid and cannot be parsed successfully" should {

          "return Success with Left(ApiError) as the response" in {

            stubGetWithHeadersCheck(getUrl(taxYear, BusinessTaxReliefs), OK, Json.obj().toString())

            val result: UserAnswersResponse[Option[UserAnswersModel]] = Await.result(connector.get(taxYear, BusinessTaxReliefs), Duration.Inf)
            result shouldBe a[Left[FailedToParseJson, _]]
          }
        }
      }

      "downstream response is any other status" should {

        "return Error with Left(UnexpectedErrorResponse) as the response" in {

          stubGetWithHeadersCheck(getUrl(taxYear, BusinessTaxReliefs), INTERNAL_SERVER_ERROR, "bang")

          val result: UserAnswersResponse[Option[UserAnswersModel]] = Await.result(connector.get(taxYear, BusinessTaxReliefs), Duration.Inf)
          result shouldBe Left(UnexpectedErrorResponse(INTERNAL_SERVER_ERROR, "bang"))
        }
      }
    }

    "calling .delete()" when {

      "downstream response is NO_CONTENT" should {

        "return Success with Right(Done) as the response" in {

          stubDeleteWithoutResponseBody(deleteUrl(taxYear, BusinessTaxReliefs), NO_CONTENT)

          val result: UserAnswersResponse[Done] = Await.result(connector.delete(taxYear, BusinessTaxReliefs), Duration.Inf)
          result shouldBe Right(Done)
        }
      }

      "downstream response is any other status" should {

        "return Error with Left(FailedToDeleteUserAnswers) as the response" in {

          stubDeleteWithResponseBody(deleteUrl(taxYear, BusinessTaxReliefs), INTERNAL_SERVER_ERROR, "bang")

          val result: UserAnswersResponse[Done] = Await.result(connector.delete(taxYear, BusinessTaxReliefs), Duration.Inf)
          result shouldBe Left(FailedToDeleteUserAnswers(INTERNAL_SERVER_ERROR, "bang"))
        }
      }
    }

    "calling .set()" when {

      "downstream response is NO_CONTENT" should {

        "return Success with Right(Done) as the response" in {

          stubPut(putUrl, NO_CONTENT, "")

          val result: UserAnswersResponse[Done] = Await.result(connector.set(userAnswers), Duration.Inf)
          result shouldBe Right(Done)
        }
      }

      "downstream response is any other status" should {

        "return Error with Left(FailedToStoreUserAnswers) as the response" in {

          stubPut(putUrl, INTERNAL_SERVER_ERROR, "bang")

          val result: UserAnswersResponse[Done] = Await.result(connector.set(userAnswers), Duration.Inf)
          result shouldBe Left(FailedToStoreUserAnswers(INTERNAL_SERVER_ERROR, "bang"))
        }
      }
    }
  }
}
