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
import models.gains.{GainsSubmissionModel, PolicyCyaModel}
import models.requests.AuthorisationRequest
import models.{AllGainsSessionModel, User}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.{ExcludeJourneyService, GainsSessionServiceProvider, GainsSubmissionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicySummaryPageView

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PolicySummaryController @Inject()(authorisedAction: AuthorisedAction,
                                        view: PolicySummaryPageView,
                                        gainsSessionService: GainsSessionServiceProvider,
                                        gainsSubmissionService: GainsSubmissionService,
                                        excludeJourneyService: ExcludeJourneyService,
                                        errorHandler: ErrorHandler,
                                        auditService: AuditService)
                                       (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with Logging with I18nSupport {

  def show(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getSessionData(taxYear).flatMap {
      case Left(_) =>
        Future.successful(errorHandler.internalServerError())
      case Right(cya) =>
        cya.fold(Future.successful(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))) {
          cyaData =>
            cyaData.gains.fold(Future.successful(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))) {
              data: AllGainsSessionModel =>
                if (data.gateway.contains(true)) {
                  data.allGains.find(_.sessionId == sessionId) match {
                    case Some(policyCya) if !policyCya.isFinished =>
                      Future.successful(handleUnfinishedRedirect(policyCya, taxYear))
                    case Some(_) =>
                      gainsSessionService.updateSessionData(
                        AllGainsSessionModel(data.allGains, data.gateway), taxYear)(Future.successful(errorHandler.internalServerError())) {
                        Future.successful(Ok(view(taxYear, data.allGains, true, sessionId)))
                      }.flatten
                    case None => {
                      logger.info("[PolicySummaryController][show] No CYA data in session. Redirecting to the overview page.")
                      Future.successful(Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
                    }
                  }
                } else {
                  gainsSessionService.updateSessionData(
                    AllGainsSessionModel(data.allGains, data.gateway), taxYear)(errorHandler.internalServerError()) {
                    Ok(view(taxYear, data.allGains, false, sessionId))
                  }
                }
            }
        }
    }
  }

  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getAndHandle(taxYear) {
      Future.successful(errorHandler.internalServerError())
    } {
      implicit val user: User = request.user
      (cya, prior) =>
        (cya, prior) match {
          case (Some(cya), _) =>
            cya.gateway match {
              case Some(false) =>
                excludeJourneyService.excludeJourney("gains", taxYear, request.user.nino).flatMap {
                  case Right(_) =>
                    submitGainsAndAudit(Some(GainsSubmissionModel()), taxYear, user, prior, cya,
                      Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear)))
                  case Left(_) =>
                    Future.successful(errorHandler.internalServerError())
                }
              case _ =>
                val currentPolicyList: Seq[PolicyCyaModel] = cya.allGains.filter(_.sessionId == sessionId)
                val priorData: Seq[PolicyCyaModel] = cya.allGains.filterNot(_.sessionId == sessionId).filter(_.isFinished)
                val submissionData: Seq[PolicyCyaModel] = currentPolicyList ++ priorData
                val optionalQueryParam: Option[String] = if (appConfig.isSplitGains) {
                  Some(currentPolicyList.head.policyType.getOrElse(""))
                } else {
                  None
                }

                submitGainsAndAudit(
                  Some(AllGainsSessionModel(submissionData).toSubmissionModel),
                  taxYear,
                  user,
                  prior,
                  cya,
                  Redirect(controllers.gainsBase.routes.GainsSummaryBaseController.show(taxYear, optionalQueryParam)))
            }
          case (_, _) =>
            Future.successful(errorHandler.internalServerError())
        }
    }.flatten
  }

  private def submitGainsAndAudit(body: Option[GainsSubmissionModel], taxYear: Int, user: User,
                                  prior: Option[GainsPriorDataModel], cya: AllGainsSessionModel, successResult: Result)
                                 (implicit hc: HeaderCarrier, request: AuthorisationRequest[AnyContent]): Future[Result] = {
    gainsSubmissionService.submitGains(body, request.user.nino, request.user.mtditid, taxYear).flatMap {
      case Left(error) =>
        logger.info("[PolicySummaryController][submit] Error while submitting gains data. Redirecting to 500 error page. " +
          "Error status: " + error.status)
        Future.successful(errorHandler.internalServerError())
      case Right(_) =>
        auditSubmission(body, prior, user.nino, user.mtditid, user.affinityGroup, taxYear)
        gainsSessionService.deleteSessionData(taxYear)(errorHandler.internalServerError())(successResult)
    }
  }

  private def auditSubmission(body: Option[GainsSubmissionModel], prior: Option[GainsPriorDataModel],
                              nino: String, mtditid: String, affinityGroup: String, taxYear: Int)
                             (implicit hc: HeaderCarrier): Future[AuditResult] = {
    val details: CreateOrAmendGainsAuditDetail = CreateOrAmendGainsAuditDetail.createFromCyaData(body,
      prior.flatMap(result => if (result.submittedOn.nonEmpty) prior else None),
      prior.isDefined, nino, mtditid, affinityGroup.toLowerCase, taxYear)

    val event = AuditModel("CreateOrAmendGainsUpdate", "create-or-amend-gains-update", details)

    auditService.auditModel(event)
  }

  private def handleUnfinishedRedirect(cyaModel: PolicyCyaModel, taxYear: Int)
                                      (implicit request: AuthorisationRequest[AnyContent]): Result = {

    val optionalQueryParam: Option[String] = if (appConfig.isSplitGains) {
      cyaModel.policyType
    } else {
      None
    }
    val cyaCommon = ListMap[Call, Option[_]](
      controllers.gains.routes.PolicyTypeController.show(taxYear, cyaModel.sessionId) -> cyaModel.policyType,
      controllers.gainsBase.routes.PolicyNameBaseController.show(taxYear, cyaModel.sessionId, optionalQueryParam) -> cyaModel.policyNumber,
      controllers.gains.routes.GainsAmountController.show(taxYear, cyaModel.sessionId) -> cyaModel.amountOfGain,
      controllers.gains.routes.PolicyEventController.show(taxYear, cyaModel.sessionId) -> cyaModel.policyEvent
    )

    val cyaPolicyHeldPrevious = {
      if (cyaModel.previousGain.contains(true)) {
        ListMap[Call, Option[_]](
          controllers.gains.routes.PolicyHeldPreviousController.show(taxYear, cyaModel.sessionId) -> cyaModel.yearsPolicyHeldPrevious,
          controllers.gains.routes.PolicyHeldController.show(taxYear, cyaModel.sessionId) -> cyaModel.yearsPolicyHeld
        )
      } else {
        ListMap[Call, Option[_]](
          controllers.gains.routes.GainsStatusController.show(taxYear, cyaModel.sessionId) -> cyaModel.previousGain,
          controllers.gains.routes.PolicyHeldController.show(taxYear, cyaModel.sessionId) -> cyaModel.yearsPolicyHeld
        )
      }
    }

    val deficiencyRelief = if (cyaModel.entitledToDeficiencyRelief.contains(true)) {
      cyaModel.deficiencyReliefAmount
    } else {
      cyaModel.entitledToDeficiencyRelief
    }

    val cyaSpecific = {
      if (cyaModel.policyType.contains("Voided ISA")) {
        ListMap[Call, Option[_]](
          controllers.gains.routes.PaidTaxAmountController.show(taxYear, cyaModel.sessionId) -> cyaModel.taxPaidAmount
        )
      } else {
        ListMap[Call, Option[_]](
          controllers.gains.routes.PaidTaxStatusController.show(taxYear, cyaModel.sessionId) -> cyaModel.treatedAsTaxPaid,
          controllers.gains.routes.GainsDeficiencyReliefController.show(taxYear, cyaModel.sessionId) -> deficiencyRelief
        )
      }
    }

    val cya: ListMap[Call, Option[_]] = cyaCommon ++ cyaPolicyHeldPrevious ++ cyaSpecific
    cya.find(x => x._2.isEmpty)
      .fold(Ok(view(taxYear, Seq(cyaModel), true, cyaModel.sessionId)))(emptyValue => Redirect(emptyValue._1))
  }
}
