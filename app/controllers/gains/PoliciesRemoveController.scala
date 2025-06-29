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
import models.AllGainsSessionModel
import models.gains.prior.GainsPriorDataModel
import models.gains.{GainsSubmissionModel, PolicyCyaModel}
import models.requests.AuthorisationRequest
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{DeleteGainsService, GainsSessionServiceProvider, GainsSubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PoliciesRemovePageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PoliciesRemoveController @Inject()(authorisedAction: AuthorisedAction,
                                         view: PoliciesRemovePageView,
                                         gainsSessionService: GainsSessionServiceProvider,
                                         gainsSubmissionService: GainsSubmissionService,
                                         deleteGainsService: DeleteGainsService,
                                         auditService: AuditService,
                                         errorHandler: ErrorHandler)
                                        (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  private def findGainBySessionId(gainsOpt: Option[AllGainsSessionModel], sessionId: String): PolicyCyaModel = {
    val emptyPolicyCyaModel: PolicyCyaModel = PolicyCyaModel("", Some(""))

    gainsOpt
      .getOrElse(AllGainsSessionModel(Seq(emptyPolicyCyaModel), gateway = Some(true)))
      .allGains
      .find(_.sessionId == sessionId)
      .getOrElse(emptyPolicyCyaModel)
  }


  def show(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getSessionData(taxYear).flatMap {
      case Left(_) => errorHandler.internalServerError()
      case Right(cya) => Future.successful(cya.fold(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))) {
        cyaData => Ok(view(taxYear, sessionId, findGainBySessionId(cyaData.gains, sessionId)))
      })
    }
  }

  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getAndHandle(taxYear)(errorHandler.internalServerError()) { (cya, prior) =>
      (cya, prior) match {
        case (Some(cya), _) =>
          val newData = AllGainsSessionModel(cya.allGains.filterNot(_.sessionId == sessionId), cya.gateway)
          val policyType = findGainBySessionId(Some(cya), sessionId).policyType

          if (newData.allGains.isEmpty) {
            deleteGainsService.deleteGainsData(request.user.nino, taxYear, request.user.mtditid).flatMap {
              case Left(_) => errorHandler.internalServerError()
              case Right(_) => auditAndDeleteSessionData(taxYear, prior, policyType)
            }
          } else {
            //As API deletes all policies, rather deleting one policy updating with full data except that one
            gainsSubmissionService.submitGains(Some(newData.toSubmissionModel), request.user.nino, request.user.mtditid, taxYear).flatMap {
              case Left(_) => errorHandler.internalServerError()
              case Right(_) => auditAndDeleteSessionData(taxYear, prior, policyType)
            }
          }

        case _ => Future.successful(Redirect(controllers.gainsBase.routes.GainsSummaryBaseController.show(taxYear, None)))
      }
    }
  }

  private def auditAndDeleteSessionData(taxYear: Int,
                                        prior: Option[GainsPriorDataModel],
                                        policyType: Option[String])
                                       (implicit hc: HeaderCarrier, request: AuthorisationRequest[AnyContent]): Future[Result] = {
    auditSubmission(None, prior, request.user.nino, request.user.mtditid, request.user.affinityGroup, taxYear)
    gainsSessionService.deleteSessionData(taxYear)(errorHandler.internalServerError()) {
      Future.successful(Redirect(controllers.gainsBase.routes.GainsSummaryBaseController.show(taxYear, policyType)))
    }
  }
  private def auditSubmission(body: Option[GainsSubmissionModel], prior: Option[GainsPriorDataModel],
                              nino: String, mtditid: String, affinityGroup: String, taxYear: Int)
                             (implicit hc: HeaderCarrier): Future[AuditResult] = {
    val details: CreateOrAmendGainsAuditDetail = CreateOrAmendGainsAuditDetail.createFromCyaData(body,
      prior.flatMap(result => if (result.submittedOn.nonEmpty) prior else None),
      !prior.exists(_.submittedOn.isEmpty), nino, mtditid, affinityGroup.toLowerCase, taxYear)
    val event = AuditModel("CreateOrAmendGainsUpdate", "create-or-amend-gains-update", details)
    auditService.auditModel(event)
  }

}
