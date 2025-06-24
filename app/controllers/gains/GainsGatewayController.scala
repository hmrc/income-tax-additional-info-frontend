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
import com.google.inject.{Inject, Singleton}
import config.{AppConfig, ErrorHandler}
import forms.YesNoForm
import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GainsSessionServiceProvider
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.GainsGatewayPageView

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GainsGatewayController @Inject()(authorisedAction: AuthorisedAction,
                                       view: GainsGatewayPageView,
                                       gainsSessionService: GainsSessionServiceProvider,
                                       errorHandler: ErrorHandler)
                                      (implicit appConfig: AppConfig,
                                       mcc: MessagesControllerComponents,
                                       ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {
  val sessionId = UUID.randomUUID().toString
  def form(isAgent: Boolean): Form[Boolean] = YesNoForm.yesNoForm(
    s"gains.gateway.question.error.${if (isAgent) "agent" else "individual"}")

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>

    gainsSessionService.getSessionData(taxYear).flatMap {
      case Left(_) => errorHandler.internalServerError()
      case Right(cya) =>
        cya match {
          case Some(cya) =>
            cya.gains.fold(Future.successful(Ok(view(taxYear, form(request.user.isAgent))))) {
              data =>
                data.gateway match {
                  case Some(value) =>
                    Future.successful(Ok(view(taxYear, form(request.user.isAgent).fill(value))))
                  case _ =>
                    Future.successful(Ok(view(taxYear, form(request.user.isAgent))))
                }
            }
          case None =>
            gainsSessionService.createSessionData(AllGainsSessionModel(Seq(PolicyCyaModel(sessionId))), taxYear)(errorHandler.internalServerError()) {
              Future.successful(Ok(view(taxYear, form(request.user.isAgent))))
            }
        }

    }
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    form(request.user.isAgent).bindFromRequest().fold(formWithErrors => {
      Future.successful(BadRequest(view(taxYear, formWithErrors)))
    }, {
      yesNoValue =>
        gainsSessionService.getSessionData(taxYear).flatMap {
          case Left(_) =>
            errorHandler.internalServerError()
          case Right(sessionData) =>
            val cya = sessionData.flatMap(_.gains).getOrElse(AllGainsSessionModel(Seq.empty)).copy(gateway = Some(yesNoValue))
            gainsSessionService.updateSessionData(cya, taxYear)(errorHandler.internalServerError()) {
              Future.successful(handleRedirect(yesNoValue, taxYear, sessionId))
            }
        }
    })
  }

  private def handleRedirect(yesNoValue: Boolean, taxYear: Int, sessionId: String): Result = {
    if (yesNoValue) {
      Redirect(controllers.gains.routes.PolicyTypeController.show(taxYear, sessionId))
    } else {
      Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, sessionId))
    }
  }
}
