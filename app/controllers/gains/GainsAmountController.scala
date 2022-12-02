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

package controllers.gains

import actions.AuthorisedAction
import config.AppConfig
import forms.gains.GainsAmountForm
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.GainsAmountPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GainsAmountController @Inject()(authorisedAction: AuthorisedAction,
                                            view: GainsAmountPageView)
                                           (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def gainsAmountForm(isAgent: Boolean): Form[String] = GainsAmountForm.gainsAmountForm(isAgent)

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    Future.successful(Ok(view(taxYear, gainsAmountForm(request.user.isAgent))))
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsAmountForm(request.user.isAgent).bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(taxYear, formWithErrors))),
      _ =>
        Future(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
    )
  }
}