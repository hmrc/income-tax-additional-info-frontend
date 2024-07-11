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
import forms.YesNoForm
import models.mongo.JourneyStatus
import models.mongo.JourneyStatus.{Completed, InProgress, NotStarted}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.GainsSessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.SectionCompletedStateView
import javax.inject.Inject
import scala.annotation.unused
import scala.concurrent.{ExecutionContext, Future}

class SectionCompletedStateController @Inject() (authorisedAction: AuthorisedAction,
                                                 gainsSessionService: GainsSessionService,
                                                 view: SectionCompletedStateView,
                                                 errorHandler: ErrorHandler
                                                )
                                                (implicit appConfig: AppConfig,
                                                 mcc: MessagesControllerComponents,
                                                 ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def form: Form[Boolean] = YesNoForm.yesNoForm(
    "sectionCompletedState.error.required")

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>

    gainsSessionService.getSessionData(taxYear).map {
      case Left(_) => errorHandler.internalServerError()
      case Right(sessionData) =>
        val valueCheck: Option[Boolean] = None //TODO: Get this value from backend mongo session
        valueCheck match {
          case None => Ok(view(form, taxYear))
          case Some(value) => Ok(view(form.fill(value), taxYear))
        }
    }
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear))),
        answer => saveAndRedirect(answer, taxYear)
      )
  }

  private def fill(form: Form[Boolean], status: JourneyStatus): Form[Boolean] =
    status match {
      case Completed => form.fill(true)
      case InProgress => form.fill(false)
      case NotStarted => form
    }

  private def saveAndRedirect(answer: Boolean, taxYear: Int): Future[Result] = { // TODO save status to backend mongo session
    @unused
    val status = if (answer) Completed else InProgress
    Future.successful(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
  }

}
