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

package controllers.businessTaxReliefs

import actions.{AuthorisedAction, JourneyDataRetrievalAction}
import config.AppConfig
import controllers.BaseController
import forms.businessTaxReliefs.PostCessationTradeReliefForm
import models.BusinessTaxReliefs
import models.requests.JourneyDataRequest
import pages.businessTaxReliefs.PostCessationTradeReliefPage
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserAnswersService
import views.html.pages.businessTaxReliefs.PostCessationTradeReliefView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostCessationTradeReliefController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                                   authorisedAction: AuthorisedAction,
                                                   retrieveJourney: JourneyDataRetrievalAction,
                                                   userAnswersService: UserAnswersService,
                                                   view: PostCessationTradeReliefView)
                                                  (implicit appConfig: AppConfig, ec: ExecutionContext) extends BaseController {

  def show(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen retrieveJourney(taxYear, BusinessTaxReliefs)).async { implicit request =>
      renderView(Ok, taxYear, fillForm(PostCessationTradeReliefPage, PostCessationTradeReliefForm()))
    }

  def submit(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen retrieveJourney(taxYear, BusinessTaxReliefs)).async { implicit request =>
      PostCessationTradeReliefForm().bindFromRequest().fold(
        renderView(BadRequest, taxYear, _),
        amount => {
          val updatedAnswers = request.userAnswers.set(PostCessationTradeReliefPage, amount)
          userAnswersService.set(updatedAnswers).map { _ =>
            NotImplemented
          }
        }
      )
    }

  private def renderView(status: Status, taxYear: Int, form: Form[BigDecimal])(implicit request: JourneyDataRequest[_]): Future[Result] =
    Future.successful(status(view(taxYear, form, routes.PostCessationTradeReliefController.submit(taxYear))))
}
