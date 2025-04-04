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

import actions.AuthorisedAction
import config.{AppConfig, ErrorHandler}
import forms.AmountForm
import forms.businessTaxReliefs.PostCessationTradeReliefForm
import models.requests.AuthorisationRequest
import models.{AllGainsSessionModel, User}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GainsSessionServiceProvider
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.businessTaxReliefs.PostCessationTradeReliefView
import views.html.pages.gains.PaidTaxAmountPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PostCessationTradeReliefController @Inject()(authorisedAction: AuthorisedAction,
                                                   view: PostCessationTradeReliefView)
                                                  (implicit mcc: MessagesControllerComponents, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = authorisedAction { implicit request =>
    renderView(Ok, taxYear, PostCessationTradeReliefForm())
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction { implicit request =>
    PostCessationTradeReliefForm().bindFromRequest().fold(
      renderView(BadRequest, taxYear, _),
      _ =>
        //TODO:
        // - Save the data to the User Answers (future story)
        // - Redirect to CYA (future story)
        NotImplemented
    )
  }

  private def renderView(status: Status, taxYear: Int, form: Form[BigDecimal])(implicit request: AuthorisationRequest[_]): Result =
    status(view(taxYear, form, routes.PostCessationTradeReliefController.submit(taxYear)))
}
