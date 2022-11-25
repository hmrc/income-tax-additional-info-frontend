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
import forms.YesNoForm
import forms.gains.CustomerReferenceForm
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.GainsGatewayPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GainsGatewayController @Inject()(authorisedAction: AuthorisedAction,
                                       view: GainsGatewayPageView)
                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def form(isAgent: Boolean): Form[Boolean] = YesNoForm.yesNoForm(
    s"gains.gateway.question.error.${if (isAgent) "agent" else "individual"}")

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    Future.successful(Ok(view(form(request.user.isAgent),taxYear)))
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    form(request.user.isAgent).bindFromRequest().fold(formWithErrors =>{
        Future.successful(BadRequest(view(formWithErrors,taxYear)))
  }, {
    yesNoValue =>
        Future.successful(Ok)
      })
  }
}
