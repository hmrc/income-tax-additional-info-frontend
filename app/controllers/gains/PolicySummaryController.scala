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
import models.gains.{DecodedGainsSubmissionPayload, GainsSubmissionModel, PolicyCyaModel}
import models.{AllGainsSessionModel, User}
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{ExcludeJourneyService, GainsSessionService, GainsSubmissionService, NrsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
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
                                        errorHandler: ErrorHandler,
                                        auditService: AuditService,
                                        nrsService: NrsService)
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
                AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, "")),
                  cya.getOrElse(AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, "")), gateway = Some(true))).gateway),
                  taxYear)(errorHandler.internalServerError()) {
                Ok(view(taxYear, cya.getOrElse(AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, "")), gateway = Some(true))).allGains, sessionId))
              }, Duration.Inf
            )
        }
    }
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
                  case Right(_) =>
                    gainsSubmissionService.submitGains(Some(GainsSubmissionModel()), request.user.nino, request.user.mtditid, taxYear)
                    nrsSubmission(Some(GainsSubmissionModel()), prior, user.nino, user.mtditid, user.affinityGroup, taxYear)
                    Future.successful(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
                  case Left(_) =>
                    Future.successful(errorHandler.internalServerError())
                }
              case _ =>
                gainsSubmissionService.submitGains(Some(cya.toSubmissionModel), request.user.nino, request.user.mtditid, taxYear)
                nrsSubmission(Some(cya.toSubmissionModel), prior, user.nino, user.mtditid, user.affinityGroup, taxYear)
                Future.successful(Redirect(controllers.gains.routes.GainsSummaryController.show(taxYear)))
            }
          }
          case (_, _) => Future.successful(errorHandler.internalServerError())
        }
    }.flatten
  }

  private def nrsSubmission(body: Option[GainsSubmissionModel], prior: Option[GainsPriorDataModel],
                            nino: String, mtditid: String, affinityGroup: String, taxYear: Int)
                           (implicit request: Request[_]): Unit = {
    val model = CreateOrAmendGainsAuditDetail.createFromCyaData(body, prior.flatMap(result => if (result.submittedOn.nonEmpty) prior else None),
      !prior.exists(_.submittedOn.isEmpty), nino, mtditid, affinityGroup.toLowerCase, taxYear)
    auditSubmission(model)
    if (appConfig.nrsEnabled) {
      nrsService.submit(nino, new DecodedGainsSubmissionPayload(body, prior), mtditid)
    }
  }

  private def auditSubmission(details: CreateOrAmendGainsAuditDetail)
                             (implicit hc: HeaderCarrier): Future[AuditResult] = {
    val event = AuditModel("CreateOrAmendGainsUpdate", "createOrAmendGainsUpdate", details)
    auditService.auditModel(event)
  }

}
