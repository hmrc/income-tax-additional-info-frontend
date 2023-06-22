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
import models.gains.PolicyCyaModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GainsSessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.GainsGatewayPageView
import com.google.inject.{Inject, Singleton}
import models.AllGainsSessionModel

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GainsGatewayController @Inject()(authorisedAction: AuthorisedAction,
                                       view: GainsGatewayPageView,
                                       gainsSessionService: GainsSessionService,
                                       errorHandler: ErrorHandler)
                                      (implicit appConfig: AppConfig,
                                       mcc: MessagesControllerComponents,
                                       ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def form(isAgent: Boolean): Form[Boolean] = YesNoForm.yesNoForm(
    s"gains.gateway.question.error.${if (isAgent) "agent" else "individual"}")

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getAndHandle(taxYear)(Future.successful(errorHandler.internalServerError())) {
      (cya, prior) =>
        (cya, prior) match {
          case (Some(cya), Some(prior)) =>
            val filteredPrior = prior.toPolicyCya.filter(el => cya.allGains.contains(el))
            gainsSessionService.updateSessionData(
              AllGainsSessionModel(cya.allGains ++ filteredPrior, gateway = true), taxYear)(errorHandler.internalServerError()
            ) {
              Redirect(controllers.gains.routes.GainsSummaryController.show(taxYear))
            }
          case (_, Some(prior)) => if (prior.toPolicyCya.nonEmpty) {
            gainsSessionService.createSessionData(AllGainsSessionModel(prior.toPolicyCya, gateway = true), taxYear)(errorHandler.internalServerError()) {
              Redirect(controllers.gains.routes.GainsSummaryController.show(taxYear))
            }
          } else {
            Future.successful(Ok(view(taxYear, form(request.user.isAgent))))
          }
          case (Some(cya), _) => if (!cya.gateway) {
              Future.successful(Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, request.user.sessionId)))
          } else {
            Future.successful(Redirect(controllers.gains.routes.GainsSummaryController.show(taxYear)))
          }
          case (_, _) => Future.successful(Ok(view(taxYear, form(request.user.isAgent))))
        }
    }.flatten
  }

  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    form(request.user.isAgent).bindFromRequest().fold(formWithErrors => {
      Future.successful(BadRequest(view(taxYear, formWithErrors)))
    }, {
      yesNoValue =>
        if (yesNoValue) {
          gainsSessionService.getAndHandle(taxYear)(Future.successful(errorHandler.internalServerError())) {
            (cya, prior) =>
              (cya, prior) match {
                case (None, None) => gainsSessionService.createSessionData(
                  AllGainsSessionModel(
                    cya.map(_.allGains).getOrElse(Seq[PolicyCyaModel]()) ++ prior.map(_.toPolicyCya).getOrElse(Seq[PolicyCyaModel]()), yesNoValue
                  ), taxYear
                )(errorHandler.internalServerError()) {
                  Redirect(controllers.gains.routes.PolicyTypeController.show(taxYear, sessionId))
                }
                case (_, _) =>
                  gainsSessionService.updateSessionData(
                    AllGainsSessionModel(
                      cya.map(_.allGains).getOrElse(Seq[PolicyCyaModel]()) ++ prior.map(_.toPolicyCya).getOrElse(Seq[PolicyCyaModel]()), yesNoValue
                    ), taxYear
                  )(errorHandler.internalServerError()) {
                    Redirect(controllers.gains.routes.PolicyTypeController.show(taxYear, sessionId))
                  }
              }
          }.flatten
        }
        else {
          gainsSessionService.getAndHandle(taxYear)(Future.successful(errorHandler.internalServerError())) {
            (cya, prior) =>
              (cya, prior) match {
                case (None, None) => gainsSessionService.createSessionData(
                  AllGainsSessionModel(
                    cya.map(_.allGains).getOrElse(Seq[PolicyCyaModel]()) ++ prior.map(_.toPolicyCya).getOrElse(Seq[PolicyCyaModel]()), gateway = false
                  ), taxYear
                )(errorHandler.internalServerError()) {
                  Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, sessionId))
                }
                case (_, _) =>
                  gainsSessionService.updateSessionData(
                    AllGainsSessionModel(
                      cya.map(_.allGains).getOrElse(Seq[PolicyCyaModel]()) ++ prior.map(_.toPolicyCya).getOrElse(Seq[PolicyCyaModel]()), gateway = false
                    ), taxYear
                  )(errorHandler.internalServerError()) {
                    Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, sessionId))
                  }
              }
          }.flatten
        }
    }
    )
  }

  def change(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getAndHandle(taxYear)(Future.successful(errorHandler.internalServerError())) {
      (cya, prior) =>
        (cya, prior) match {
          case (_, _) => Future.successful(Ok(view(taxYear, form(request.user.isAgent))))
        }
    }.flatten
  }
}
