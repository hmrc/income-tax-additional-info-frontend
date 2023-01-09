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

package controllers.gains

import actions.AuthorisedAction
import config.{AppConfig, ErrorHandler}
import forms.gains.InputYearForm
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicyHeldPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PolicyHeldController @Inject()(authorisedAction: AuthorisedAction,
                                     view: PolicyHeldPageView)
                                    (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def policyHeldForm(isAgent: Boolean): Form[Option[Int]] = InputYearForm.inputYearsForm(
     s"gains.policy-held.question.error-empty.${if (isAgent) "agent" else "individual"}",
     s"gains.policy-held.question.error-incorrect.format.${if (isAgent) "agent" else "individual"}",
     "gains.policy-held.question.error-yearsExceedsMaximum"
  )

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    Future.successful(Ok(view(taxYear, policyHeldForm(request.user.isAgent))))
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    policyHeldForm(request.user.isAgent).bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(taxYear, formWithErrors))),
      _ =>
        Future(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
    )
  }
}