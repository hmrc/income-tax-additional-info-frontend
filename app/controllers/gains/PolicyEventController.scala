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
import forms.gains.RadioButtonPolicyEventForm
import models.AllGainsSessionModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GainsSessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicyEventPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PolicyEventController @Inject()(authorisedAction: AuthorisedAction,
                                      view: PolicyEventPageView,
                                      gainsSessionService: GainsSessionService,
                                      errorHandler: ErrorHandler)
                                     (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {
  private def policyEventForm(isAgent: Boolean): Form[(String,String)] =
    RadioButtonPolicyEventForm.radioButtonCustomOptionForm(
      s"gains.policy-event.question.error-message",
      s"gains.policy-event.selection.error-message",
      s"gains.policy-event.question.incorrect-format.error-message")

  private val policyEventType1 = "Full or part surrender"
  private val policyEventType2 = "Policy matured or a death"
  private val policyEventType3 = "Sale or assignment of a policy"
  private val policyEventType4 = "Personal Portfolio Bond"
  def show(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getSessionData(taxYear).flatMap {
      case Left(_) => Future.successful(errorHandler.internalServerError())
      case Right(cya) =>
        Future.successful(cya.fold(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))) {
          cyaData =>
            cyaData.gains.fold(Ok(view(taxYear, policyEventForm(request.user.isAgent), sessionId))) {
              data =>
                data.allGains.filter(_.sessionId == sessionId).head.policyEvent match {
                  case None => Ok(view(taxYear, policyEventForm(request.user.isAgent), sessionId))
                  case Some(value) => Ok(view(taxYear, policyEventForm(request.user.isAgent).fill(value match {
                    case `policyEventType1` => (value, "")
                    case `policyEventType2` => (value, "")
                    case `policyEventType3` => (value, "")
                    case `policyEventType4` => (value, "")
                    case _ => ("Other", value)
                  }), sessionId))
                }
            }
        })
    }
  }

  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    policyEventForm(request.user.isAgent).bindFromRequest().fold(formWithErrors => {
      Future.successful(BadRequest(view(taxYear, formWithErrors, sessionId)))
    }, {
      policyEvent =>
        gainsSessionService.getSessionData(taxYear).flatMap {
          case Left(_) => Future.successful(errorHandler.internalServerError())
          case Right(sessionData) =>
            val cya = sessionData.flatMap(_.gains).getOrElse(AllGainsSessionModel(Seq.empty))
            val index = cya.allGains.indexOf(cya.allGains.find(_.sessionId == sessionId).get)
            val newData = cya.allGains(index).copy(policyEvent = if (policyEvent._1 != "Other") Some(policyEvent._1) else Some(policyEvent._2))
            val updated = cya.allGains.updated(index, newData)
            gainsSessionService.updateSessionData(AllGainsSessionModel(updated, cya.gateway), taxYear)(errorHandler.internalServerError()) {
              if (newData.isFinished) {
                Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, sessionId))
              } else {
                Redirect(controllers.gains.routes.GainsStatusController.show(taxYear, sessionId))
              }
            }
        }
    })
  }
}