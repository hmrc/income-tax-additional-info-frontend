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

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.http.HeaderNames
import play.api.mvc._
import uk.gov.hmrc.http.{SessionKeys, HeaderNames => HMRCHeaderNames}
import utils.UUIDGenerator

import scala.concurrent.{ExecutionContext, Future}


class SessionIdFilter @Inject()(materializer: Materializer,
                                uuidGenerator: UUIDGenerator,
                                cookieBaker: SessionCookieBaker,
                                cookieHeaderEncoding: CookieHeaderEncoding)
                               (implicit val ec: ExecutionContext) extends Filter {

  override implicit def mat: Materializer = materializer

  override def apply(function: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    if (requestHeader.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId: String = s"session-${uuidGenerator.randomUUID()}"
      val headers = headersWithSessionDataFor(requestHeader, sessionId)

      function(requestHeader.withHeaders(headers))
        .map(_.addingToSession(SessionKeys.sessionId -> sessionId)(requestHeader.withHeaders(headers)))
    } else {
      function(requestHeader)
    }
  }

  private def headersWithSessionDataFor(requestHeader: RequestHeader, sessionId: String): Headers = {
    val session: Session = requestHeader.session + (SessionKeys.sessionId -> sessionId)
    Headers(
      SessionKeys.sessionId -> sessionId,
      HMRCHeaderNames.xSessionId -> sessionId,
      HeaderNames.COOKIE -> cookieHeaderEncoding.encodeCookieHeader(requestHeader.cookies.toSeq ++ Seq(cookieBaker.encodeAsCookie(session)))
    ).add(requestHeader.headers.remove(HeaderNames.COOKIE).headers: _*)
  }
}
