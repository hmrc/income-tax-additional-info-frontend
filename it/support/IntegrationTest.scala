/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.actor.ActorSystem
import config.AppConfig
import models.authorisation.SessionValues
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.mvc.Result
import play.api.{Application, Environment, Mode}
import support.builders.models.UserBuilder.aUser
import support.helpers.{PlaySessionCookieBaker, TaxYearProvider, WireMockServer}
import support.stubs.WireMockStubs
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

trait IntegrationTest extends AnyWordSpec
  with Matchers
  with GuiceOneServerPerSuite
  with WireMockServer
  with WireMockStubs
  with BeforeAndAfterAll
  with TaxYearProvider {

  protected implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  protected implicit val headerCarrier: HeaderCarrier = HeaderCarrier().withExtraHeaders(headers = "mtditid" -> aUser.mtditid)
  protected implicit val actorSystem: ActorSystem = ActorSystem()
  protected implicit lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
  protected implicit lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  protected lazy val appUrl = s"http://localhost:$port/update-and-submit-income-tax-return/construction-industry-scheme-deductions"

  protected val config: Map[String, String] = Map()

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build()

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  protected def fullUrl(endOfUrl: String): String = s"http://localhost:$port" + endOfUrl

  protected def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  protected def status(awaitable: Future[Result]): Int = await(awaitable).header.status

  protected def playSessionCookies(taxYear: Int,
                                   validTaxYears: Seq[Int] = validTaxYearList,
                                   extraData: Map[String, String] = Map.empty): String = PlaySessionCookieBaker.bakeSessionCookie(Map(
    SessionValues.TAX_YEAR -> taxYear.toString,
    SessionValues.VALID_TAX_YEARS -> validTaxYears.mkString(","),
    SessionKeys.sessionId -> aUser.sessionId,
    SessionValues.CLIENT_NINO -> aUser.nino,
    SessionValues.CLIENT_MTDITID -> aUser.mtditid,
    SessionKeys.authToken -> "mock-bearer-token"
  ) ++ extraData)
}