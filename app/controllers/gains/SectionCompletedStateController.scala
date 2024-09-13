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
import config.AppConfig
import forms.YesNoForm
import models.mongo.JourneyStatus.{Completed, InProgress}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.SectionCompletedStateView

import javax.inject.Inject
import scala.annotation.unused
import scala.concurrent.Future

class SectionCompletedStateController @Inject() (authorisedAction: AuthorisedAction, view: SectionCompletedStateView)
                                                (implicit appConfig: AppConfig,
                                                 mcc: MessagesControllerComponents) extends FrontendController(mcc) with I18nSupport {

  def form: Form[Boolean] = YesNoForm.yesNoForm(
    "gains.sectionCompletedState.error.required")

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    Future.successful(Ok(view(form, taxYear)))
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear))),
        answer => saveAndRedirect(answer, taxYear)
      )
  }

  private def saveAndRedirect(answer: Boolean, taxYear: Int): Future[Result] = { // TODO save status to backend mongo session
    @unused
    val status = if (answer) Completed else InProgress
    Future.successful(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))) //TODO: Redirect to common tasklist page and stub tailoring responses for unit testing
  }

}
