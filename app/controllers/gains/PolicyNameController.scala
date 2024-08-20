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
import forms.gains.InputFieldForm
import models.AllGainsSessionModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GainsSessionServiceProvider
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicyNamePageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PolicyNameController @Inject()(authorisedAction: AuthorisedAction,
                                     view: PolicyNamePageView,
                                     gainsSessionService: GainsSessionServiceProvider,
                                     errorHandler: ErrorHandler)
                                    (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  val inputFormat = "policyNumber"

  def form(isAgent: Boolean): Form[String] = InputFieldForm.inputFieldForm(isAgent, inputFormat,
    s"gains.policy-name.question.error-message.1.${if (isAgent) "agent" else "individual"}",
    s"gains.policy-name.question.error-message.2.${if (isAgent) "agent" else "individual"}"
  )

  def show(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getSessionData(taxYear).flatMap {
      case Left(_) => Future.successful(errorHandler.internalServerError())
      case Right(cya) =>
        Future.successful(cya.fold(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))) {
          cyaData =>
            cyaData.gains.fold(Ok(view(taxYear, form(request.user.isAgent), sessionId))) {
              data =>
                data.allGains.find(_.sessionId == sessionId) match {
                  case None => errorHandler.internalServerError()
                  case Some(value) => Ok(view(taxYear, form(request.user.isAgent).fill(value.policyNumber.getOrElse("")), sessionId))
                }
            }
        })
    }
  }

  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    form(request.user.isAgent).bindFromRequest().fold(formWithErrors => {
      Future.successful(BadRequest(view(taxYear, formWithErrors, sessionId)))
    }, {
      policyNumber =>
        gainsSessionService.getSessionData(taxYear).flatMap {
          case Left(_) => Future.successful(errorHandler.internalServerError())
          case Right(sessionData) =>
            val cya = sessionData.flatMap(_.gains).getOrElse(AllGainsSessionModel(Seq.empty))
            val currentSession = cya.allGains.find(_.sessionId == sessionId)
            currentSession match {
              case None => Future.successful(errorHandler.internalServerError())
              case Some(session) => {
                val index = cya.allGains.indexOf(session)
                val newData = cya.allGains(index).copy(policyNumber = Some(policyNumber))
                val updated = cya.allGains.updated(index, newData)
                gainsSessionService.updateSessionData(AllGainsSessionModel(updated, cya.gateway), taxYear)(errorHandler.internalServerError()) {
                  if (newData.isFinished) {
                    Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, sessionId))
                  } else {
                    Redirect(controllers.gains.routes.GainsAmountController.show(taxYear, sessionId))
                  }
                }
              }
            }
        }
    })
  }
}