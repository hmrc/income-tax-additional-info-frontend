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

package support.mocks

import actions.ActionsProvider
import models.requests.AuthorisationRequest
import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import play.api.mvc._
import support.builders.UserBuilder.aUser

import scala.concurrent.{ExecutionContext, Future}

trait MockActionsProvider extends MockFactory
  with MockAuthorisedAction
  with MockErrorHandler {

  protected val mockActionsProvider: ActionsProvider = mock[ActionsProvider]

  def mockEndOfYear(taxYear: Int): CallHandler1[Int, ActionBuilder[AuthorisationRequest, AnyContent]] = {
    (mockActionsProvider.endOfYear(_: Int))
      .expects(taxYear)
      .returns(value = authorisationRequestActionBuilder)
  }

  private def authorisationRequestActionBuilder: ActionBuilder[AuthorisationRequest, AnyContent] =
    new ActionBuilder[AuthorisationRequest, AnyContent] {
      override def parser: BodyParser[AnyContent] = BodyParser("anyContent")(_ => throw new NotImplementedError)

      override def invokeBlock[A](request: Request[A], block: AuthorisationRequest[A] => Future[Result]): Future[Result] =
        block(AuthorisationRequest(aUser, request))

      override protected def executionContext: ExecutionContext = ExecutionContext.Implicits.global
    }
}
