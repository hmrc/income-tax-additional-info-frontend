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
import models.AllGainsSessionModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GainsSessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicyHeldPreviousPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PolicyHeldPreviousController @Inject()(authorisedAction: AuthorisedAction,
                                             view: PolicyHeldPreviousPageView,
                                             gainsSessionService: GainsSessionService,
                                             errorHandler: ErrorHandler)
                                            (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def form(isAgent: Boolean): Form[Option[Int]] = InputYearForm.inputYearsForm(
    s"gains.policy-held-previous.question.error-empty.${if (isAgent) "agent" else "individual"}",
    s"gains.policy-held-previous.question.error-incorrect.format",
    s"common.gains.policy.question.error-yearsExceedsMaximum"
  )

  def show(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getSessionData(taxYear).flatMap {
      case Left(_) => Future.successful(errorHandler.internalServerError())
      case Right(cya) =>
        Future.successful(cya.fold(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))) {
          cyaData =>
            cyaData.gains.fold(Ok(view(taxYear, form(request.user.isAgent), sessionId))) {
              data =>
                data.allGains.filter(_.sessionId == sessionId).head.yearsPolicyHeldPrevious match {
                  case None => Ok(view(taxYear, form(request.user.isAgent), sessionId))
                  case Some(value) => Ok(view(taxYear, form(request.user.isAgent).fill(Some(value)), sessionId))
                }
            }
        })
    }
  }


  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    form(request.user.isAgent).bindFromRequest().fold(formWithErrors => {
      Future.successful(BadRequest(view(taxYear, formWithErrors, sessionId)))
    }, {
      amount =>
        gainsSessionService.getSessionData(taxYear).flatMap {
          case Left(_) => Future.successful(errorHandler.internalServerError())
          case Right(sessionData) =>
            val cya = sessionData.flatMap(_.gains).getOrElse(AllGainsSessionModel(Seq.empty))
            val index = cya.allGains.indexOf(cya.allGains.filter(_.sessionId == sessionId).head)
            val newData = cya.allGains(index).copy(yearsPolicyHeldPrevious = amount)
            val updated = cya.allGains.updated(index, newData)
            gainsSessionService.updateSessionData(AllGainsSessionModel(updated, cya.gateway), taxYear)(errorHandler.internalServerError()) {
              if (newData.isFinished) {
                Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, sessionId))
              } else {
                Redirect(controllers.gains.routes.PolicyHeldController.show(taxYear, sessionId))
              }
            }
        }
    })
  }
}