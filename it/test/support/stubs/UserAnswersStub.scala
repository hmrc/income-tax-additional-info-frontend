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

package support.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.{Journey, UserAnswersModel}
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.Json
import support.stubs.WireMockStubs

trait UserAnswersStub extends WireMockStubs {

  val putUrl = s"/income-tax-additional-information/income-tax/user-answers"
  def getUrl(taxYear: Int, journey: Journey): String = s"/income-tax-additional-information/income-tax/user-answers/$taxYear/$journey"
  def deleteUrl(taxYear: Int, journey: Journey): String = getUrl(taxYear, journey)

  def stubGetUserAnswers(taxYear: Int, journey: Journey)(response: UserAnswersModel): StubMapping =
    stubGet(getUrl(taxYear, journey), OK, Json.toJson(response).toString())

  def stubStoreUserAnswers(): StubMapping =
    stubPut(putUrl, NO_CONTENT, "")

  def stubDeleteUserAnswers(taxYear: Int, journey: Journey): StubMapping =
    stubDeleteWithoutResponseBody(deleteUrl(taxYear, journey), NO_CONTENT)

}
