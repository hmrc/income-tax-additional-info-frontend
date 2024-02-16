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
import audit.{AuditModel, AuditService, CreateOrAmendGainsAuditDetail}
import config.{AppConfig, ErrorHandler}
import models.gains.prior.GainsPriorDataModel
import models.gains.GainsSubmissionModel
import models.requests.AuthorisationRequest
import models.{AllGainsSessionModel, User}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{ExcludeJourneyService, GainsSessionService, GainsSubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicySummaryPageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PolicySummaryController @Inject()(authorisedAction: AuthorisedAction,
                                        view: PolicySummaryPageView,
                                        gainsSessionService: GainsSessionService,
                                        gainsSubmissionService: GainsSubmissionService,
                                        excludeJourneyService: ExcludeJourneyService,
                                        errorHandler: ErrorHandler,
                                        auditService: AuditService)
                                       (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with Logging with I18nSupport {

  def show(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getAndHandle(taxYear)(Future.successful(errorHandler.internalServerError())) {
      (cya, prior) =>
        (cya, prior) match {
          case (Some(cya), Some(prior)) =>
            val filteredPrior = prior.toPolicyCya.filter(el => cya.allGains.contains(el))
            if(cya.allGains.map(_.sessionId).contains(sessionId))
            {
              gainsSessionService.updateSessionData(
                AllGainsSessionModel(cya.allGains ++ filteredPrior, cya.gateway), taxYear)(errorHandler.internalServerError()) {
                Ok(view(taxYear, cya.allGains ++ filteredPrior, sessionId))
              }
            } else {
              Future.successful(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
            }
          case (Some(cya), None) =>
              gainsSessionService.updateSessionData(
                AllGainsSessionModel(cya.allGains, cya.gateway), taxYear)(errorHandler.internalServerError()) {
                Ok(view(taxYear, cya.allGains, sessionId))
              }
          case (_, _) =>
            logger.info("[PolicySummaryController][show] No CYA data in session. Redirecting to the overview page.")
            Future.successful(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
        }
    }.flatten
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getAndHandle(taxYear)(Future.successful(errorHandler.internalServerError())) {
      implicit val user: User = request.user
      (cya, prior) =>
        (cya, prior) match {
          case (Some(cya), _) => {
            cya.gateway match {
              case Some(false) =>
                excludeJourneyService.excludeJourney("gains", taxYear, request.user.nino).flatMap {
                  case Right(_) => submitGainsAndAudit(Some(GainsSubmissionModel()), taxYear, user, prior, cya,
                    Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
                  case Left(_) => Future.successful(errorHandler.internalServerError())
                }
              case _ =>
                submitGainsAndAudit(Some(cya.toSubmissionModel), taxYear, user, prior, cya,
                  Redirect(controllers.gains.routes.GainsSummaryController.show(taxYear)))
            }
          }
          case (_, _) => Future.successful(errorHandler.internalServerError())
        }
    }.flatten
  }

  private def submitGainsAndAudit(body: Option[GainsSubmissionModel], taxYear: Int, user: User,
                                  prior: Option[GainsPriorDataModel], cya: AllGainsSessionModel, successResult: Result)
                                 (implicit hc: HeaderCarrier, request: AuthorisationRequest[AnyContent]):Future[Result]= {
    gainsSubmissionService.submitGains(body, request.user.nino, request.user.mtditid, taxYear).flatMap {
      case Left(error) => {
        logger.info("[PolicySummaryController][submit] Error while submitting gains data. Redirecting to 500 error page. " +
          "Error status: " + error.status)
        Future.successful(errorHandler.internalServerError())
      }
      case Right(_) => {
        auditSubmission(body, prior, user.nino, user.mtditid, user.affinityGroup, taxYear)
        gainsSessionService.deleteSessionData(cya, taxYear)(errorHandler.internalServerError())(successResult)
      }
    }
  }

  private def auditSubmission(body: Option[GainsSubmissionModel], prior: Option[GainsPriorDataModel],
                              nino: String, mtditid: String, affinityGroup: String, taxYear: Int)
                             (implicit hc: HeaderCarrier): Future[AuditResult] = {
   val details: CreateOrAmendGainsAuditDetail = CreateOrAmendGainsAuditDetail.createFromCyaData(body,
      prior.flatMap(result => if (result.submittedOn.nonEmpty) prior else None),
      !prior.exists(_.submittedOn.isEmpty), nino, mtditid, affinityGroup.toLowerCase, taxYear)
    val event = AuditModel("CreateOrAmendGainsUpdate", "createOrAmendGainsUpdate", details)
    auditService.auditModel(event)
  }

}
