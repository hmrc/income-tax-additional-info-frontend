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
import forms.gains.RadioButtonPolicyTypeForm
import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GainsSessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicyTypePageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PolicyTypeController @Inject()(authorisedAction: AuthorisedAction,
                                     view: PolicyTypePageView,
                                     gainsSessionService: GainsSessionService,
                                     errorHandler: ErrorHandler)
                                    (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  private def form(implicit isAgent: Boolean): Form[String] =
    RadioButtonPolicyTypeForm.radioButtonCustomOptionForm(s"gains.policy-type.error.missing-input.${if (isAgent) "agent" else "individual"}")

  def show(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getSessionData(taxYear).flatMap {
      case Left(_) => Future.successful(errorHandler.internalServerError())
      case Right(cya) =>
        cya match {
          case Some(cyaData) =>
                cyaData.gains.fold(Future.successful(Ok(view(taxYear, form(request.user.isAgent), sessionId)))) {
                  data =>
                    data.allGains.filter(_.sessionId == sessionId) match {
                      case value => if (value.nonEmpty) {
                        Future.successful(Ok(view(taxYear, form(request.user.isAgent).fill(value.head.policyType.getOrElse("")), sessionId)))
                      }
                      else {
                        Future.successful(Ok(view(taxYear, form(request.user.isAgent), sessionId)))
                      }
                    }
                }
          case None =>
            gainsSessionService.createSessionData(AllGainsSessionModel(Seq.empty, Some(true)), taxYear)(errorHandler.internalServerError()) {
              Ok(view(taxYear, form(request.user.isAgent), sessionId))
            }

        }

    }
  }

  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    form(request.user.isAgent).bindFromRequest().fold(formWithErrors => {
      Future.successful(BadRequest(view(taxYear, formWithErrors, sessionId)))
    }, {
      policy =>
        gainsSessionService.getSessionData(taxYear).flatMap {
          case Left(_) => Future.successful(errorHandler.internalServerError())
          case Right(sessionData) =>
            val cya = sessionData.flatMap(_.gains).getOrElse(AllGainsSessionModel(Seq.empty))
            val newData =
              if (!cya.allGains.map(_.sessionId).contains(sessionId)) {
                cya.allGains ++ Seq(PolicyCyaModel(sessionId = sessionId, policyType = Some(policy)))
              } else {
                val gains = cya.allGains
                val newG = cya.allGains.filter(_.sessionId == sessionId).head.copy(policyType = Some(policy))
                gains.updated(gains.indexOf(gains.find(_.sessionId == sessionId).get), newG)
              }
            gainsSessionService.updateSessionData(AllGainsSessionModel(newData, cya.gateway), taxYear)(errorHandler.internalServerError()) {
              if (newData.filter(_.sessionId == sessionId).head.isFinished) {
                Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, sessionId))
              } else {
                Redirect(controllers.gains.routes.PolicyNameController.show(taxYear, sessionId))
              }
            }
        }
    })
  }
}