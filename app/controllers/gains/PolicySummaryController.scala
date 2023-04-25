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
import models.{AllGainsSessionModel, User}
import models.gains.{GainsSubmissionModel, PolicyCyaModel}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{ExcludeJourneyService, GainsSessionService, GainsSubmissionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicySummaryPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class PolicySummaryController @Inject()(authorisedAction: AuthorisedAction,
                                        view: PolicySummaryPageView,
                                        gainsSessionService: GainsSessionService,
                                        gainsSubmissionService: GainsSubmissionService,
                                        excludeJourneyService: ExcludeJourneyService,
                                        errorHandler: ErrorHandler)
                                       (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getAndHandle(taxYear)(errorHandler.internalServerError()) {
      (cya, prior) =>
        (cya, prior) match {
          case (Some(cya), Some(prior)) =>
            val filteredPrior = prior.toPolicyCya.filter(el => cya.allGains.contains(el))
            Await.result(
              gainsSessionService.updateSessionData(
                AllGainsSessionModel(cya.allGains ++ filteredPrior, cya.gateway), taxYear)(errorHandler.internalServerError()) {
                Ok(view(taxYear, cya.allGains ++ filteredPrior, sessionId))
              }, Duration.Inf
            )
          case (Some(cya), _) =>
            Await.result(
              gainsSessionService.updateSessionData(
                AllGainsSessionModel(cya.allGains, cya.gateway), taxYear)(errorHandler.internalServerError()) {
                Ok(view(taxYear, cya.allGains, sessionId))
              }, Duration.Inf
            )
          case (_, _) =>
            Await.result(
              gainsSessionService.createSessionData(
                AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, "")), cya.getOrElse(AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, "")), gateway = true)).gateway), taxYear)(errorHandler.internalServerError()) {
                Ok(view(taxYear, cya.getOrElse(AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, "")), gateway = true)).allGains, sessionId))
              }, Duration.Inf
            )
        }
    }
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getAndHandle(taxYear)(errorHandler.internalServerError()) {
      implicit val user: User = request.user
      (cya, prior) =>
        (cya, prior) match {
          case (Some(cya), _) => if (!cya.gateway) {
            Await.result(excludeJourneyService.excludeJourney("gains", taxYear, request.user.nino).flatMap {
              case Right(_) =>
                gainsSubmissionService.submitGains(Some(GainsSubmissionModel()), request.user.nino, request.user.mtditid, taxYear)
                Future(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
              case Left(_) =>
                Future(errorHandler.internalServerError())
            }, Duration.Inf)
          } else {
            gainsSubmissionService.submitGains(Some(cya.toSubmissionModel), request.user.nino, request.user.mtditid, taxYear)
            Redirect(controllers.gains.routes.GainsSummaryController.show(taxYear))
          }
          case (_, _) => errorHandler.internalServerError()
        }
    }
  }

}
