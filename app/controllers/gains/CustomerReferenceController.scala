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
import forms.gains.InputFieldForm
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.CustomerReferencePageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomerReferenceController @Inject()(authorisedAction: AuthorisedAction,
                                            view: CustomerReferencePageView)
                                           (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  val inputFormat = "mixedAlphaNumeric"

  def customerReferenceForm(isAgent: Boolean): Form[String] = InputFieldForm.inputFieldForm(isAgent, inputFormat,
    s"gains.customer-reference.question.error-message.1.${if (isAgent) "agent" else "individual"}",
    s"gains.customer-reference.question.error-message.2.${if (isAgent) "agent" else "individual"}"
  )

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    Future.successful(Ok(view(taxYear, customerReferenceForm(request.user.isAgent))))
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    customerReferenceForm(request.user.isAgent).bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(taxYear, formWithErrors))),
      _ =>
        Future(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
    )
  }
}