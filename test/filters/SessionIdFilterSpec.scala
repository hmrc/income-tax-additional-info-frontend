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

package filters

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalamock.scalatest.MockFactory
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import support.UnitTest
import support.mocks.MockUUIDGenerator
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionIdFilterSpec extends UnitTest
  with FutureAwaits with DefaultAwaitTimeout
  with MockUUIDGenerator
  with MockFactory {

  private val materializer: Materializer = Materializer.matFromSystem(ActorSystem.create("some-name"))
  private val cookieBaker = mock[SessionCookieBaker]
  private val cookieHeaderEncoding = mock[CookieHeaderEncoding]

  private val underTest = new SessionIdFilter(materializer, mockUUIDGenerator, cookieBaker, cookieHeaderEncoding)

  ".mat" should {
    "return the given materializer" in {
      underTest.mat shouldBe materializer
    }
  }

  "apply" should {
    "return function with unchanged requestHeader when sessionId exists" in {
      val requestHeader = FakeRequest().withSession(SessionKeys.sessionId -> "some-id")
      val function = (_: RequestHeader) => Future.successful(Ok("success"))

      await(underTest.apply(function)(requestHeader)) shouldBe await(function(requestHeader))
    }

    "enrich requestHeader when sessionId if it does not exist" in {
      val sessionId = "session-" + "some-uuid"
      val requestHeader = FakeRequest().withSession("some-key" -> "some-value")
      val function = (_: RequestHeader) => Future.successful(Ok("success"))
      val session: Session = requestHeader.session + (SessionKeys.sessionId -> sessionId)

      mockRandomUUID(result = "some-uuid")
      (cookieBaker.encodeAsCookie(_: Session)).expects(session).returning(Cookie("some-name", "some-value"))
      (cookieHeaderEncoding.encodeCookieHeader(_: Seq[Cookie])).expects(Seq(Cookie("some-name", "some-value"))).returning("some-value")

      val result = await(underTest.apply(function)(requestHeader))

      result.session(requestHeader) shouldBe Session(Map(
        "some-key" -> "some-value",
        SessionKeys.sessionId -> sessionId
      ))
    }
  }
}
