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

import play.api.http.Status._
import support.UnitTest
import uk.gov.hmrc.http.HttpResponse

class PagerDutyLoggerServiceSpec extends UnitTest {

  private val body = "any-body"

  private val underTest = new PagerDutyLoggerService()

  ".messageToLog" should {
    "return correct message to log when OK" in {
      underTest.messageToLog(HttpResponse.apply(OK, body), parserName = "some-parser-name") shouldBe
        "BAD_SUCCESS_JSON_FROM_IF [some-parser-name][read] Invalid Json from Integration Framework."
    }

    "return correct message to log when INTERNAL_SERVER_ERROR" in {
      val response = HttpResponse.apply(INTERNAL_SERVER_ERROR, body, headers = Map("CorrelationId" -> Seq("some-id")))

      underTest.messageToLog(response, parserName = "some-parser-name") shouldBe s"INTERNAL_SERVER_ERROR_FROM_IF [some-parser-name][read] " +
        s"Received ${response.status} from Integration Framework. Body:${response.body} CorrelationId: some-id"
    }

    "return correct message to log when SERVICE_UNAVAILABLE" in {
      val response = HttpResponse.apply(SERVICE_UNAVAILABLE, body)

      underTest.messageToLog(response, parserName = "some-parser-name") shouldBe
        s"SERVICE_UNAVAILABLE_FROM_IF [some-parser-name][read] Received ${response.status} from Integration Framework. Body:${response.body}"
    }

    "return correct message to log when BAD_REQUEST" in {
      val response = HttpResponse.apply(BAD_REQUEST, body)

      underTest.messageToLog(response, parserName = "some-parser-name") shouldBe
        s"FOURXX_RESPONSE_FROM_IF [some-parser-name][read] Received ${response.status} from Integration Framework. Body:${response.body}"
    }

    "return correct message to log when UNPROCESSABLE_ENTITY" in {
      val response = HttpResponse.apply(UNPROCESSABLE_ENTITY, body)

      underTest.messageToLog(response, parserName = "some-parser-name") shouldBe
        s"FOURXX_RESPONSE_FROM_IF [some-parser-name][read] Received ${response.status} from Integration Framework. Body:${response.body}"
    }

    "return correct message to log when ANYTHING_ELSE" in {
      val response = HttpResponse.apply(UNAUTHORIZED, body)
      underTest.pagerDutyLog(response, parserName = "some-name")

      underTest.messageToLog(response, parserName = "some-parser-name") shouldBe
        s"UNEXPECTED_RESPONSE_FROM_IF [some-parser-name][read] Received ${response.status} from Integration Framework. Body:${response.body}"
    }
  }
}
