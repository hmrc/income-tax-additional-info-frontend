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

package controllers.gainsSplit

import actions.AuthorisedAction
import config.{AppConfig, ErrorHandler}
import forms.gains.InputFieldForm
import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import models.requests.AuthorisationRequest
import play.api.Logging
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GainsSessionServiceProvider
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.PolicyNamePageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PolicyNameSplitController @Inject()(authorisedAction: AuthorisedAction,
                                          view: PolicyNamePageView,
                                          gainsSessionService: GainsSessionServiceProvider,
                                          errorHandler: ErrorHandler)
                                         (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging {

  private val inputFormat = "policyNumber"

  private def form(isAgent: Boolean): Form[String] = InputFieldForm.inputFieldForm(isAgent, inputFormat,
    s"gains.policy-name.question.error-message.1.${if (isAgent) "agent" else "individual"}",
    s"gains.policy-name.question.error-message.2.${if (isAgent) "agent" else "individual"}"
  )

  private val LIFE_INSURANCE: String = "Life Insurance"
  private val LIFE_ANNUITY: String = "Life Annuity"
  private val CAPITAL_REDEMPTION: String = "Capital Redemption"
  private val VOIDED_ISA: String = "Voided ISA"

  def show(taxYear: Int, sessionId: String, policyType: Option[String]): Action[AnyContent] = authorisedAction.async { implicit request =>

    def gainWithPolicyType(policyType: String): AllGainsSessionModel = AllGainsSessionModel(Seq(PolicyCyaModel(sessionId, Some(policyType))), Some(true))

    policyType.getOrElse("") match {
      case LIFE_INSURANCE => loadView(taxYear, sessionId, Some(gainWithPolicyType(LIFE_INSURANCE)))
      case LIFE_ANNUITY => loadView(taxYear, sessionId, Some(gainWithPolicyType(LIFE_ANNUITY)))
      case CAPITAL_REDEMPTION => loadView(taxYear, sessionId, Some(gainWithPolicyType(CAPITAL_REDEMPTION)))
      case VOIDED_ISA => loadView(taxYear, sessionId, Some(gainWithPolicyType(VOIDED_ISA)))
      case _ => loadView(taxYear, sessionId, None)
    }
  }

  private def loadView(taxYear: Int, sessionId: String, gainsModel: Option[AllGainsSessionModel])
                      (implicit request: AuthorisationRequest[AnyContent]): Future[Result] = {
    gainsSessionService.getSessionData(taxYear).flatMap {
      case Left(_) => errorHandler.internalServerError()
      case Right(cya) =>
        cya.fold(createSession(taxYear, sessionId, gainsModel.getOrElse(AllGainsSessionModel(Seq.empty, None)))) {
          cyaData =>
            cyaData.gains.fold(errorHandler.internalServerError()) {
              data =>
                data.allGains.find(_.sessionId == sessionId) match {
                  case None =>
                    logger.info("[PolicyNameSplitController][submit] No policy exists with session id passed")
                    gainsSessionService.updateSessionData(
                      gainsModel.get.copy(allGains = Seq(
                        PolicyCyaModel(
                          sessionId,
                          gainsModel.getOrElse(AllGainsSessionModel(Seq.empty, None)).allGains.headOption.getOrElse(PolicyCyaModel(sessionId)).policyType)
                      )),
                      taxYear
                    )(errorHandler.internalServerError())(
                      Future.successful(Ok(view(taxYear, form(request.user.isAgent), sessionId)))
                    )
                  case Some(value) => Future.successful(Ok(view(taxYear, form(request.user.isAgent).fill(value.policyNumber.getOrElse("")), sessionId)))
                }
            }
        }
    }
  }

  private def createSession(taxYear: Int, sessionId: String, gainsModel: AllGainsSessionModel)
                           (implicit request: AuthorisationRequest[AnyContent]): Future[Result] = {
    gainsSessionService.createSessionData(gainsModel, taxYear)(errorHandler.internalServerError()) {
      Future.successful(Ok(view(taxYear, form(request.user.isAgent), sessionId)))
    }
  }

  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = authorisedAction.async { implicit request =>
    form(request.user.isAgent).bindFromRequest().fold(formWithErrors => {
      Future.successful(BadRequest(view(taxYear, formWithErrors, sessionId)))
    }, {
      policyNumber =>
        gainsSessionService.getSessionData(taxYear).flatMap {
          case Left(_) => errorHandler.internalServerError()
          case Right(sessionData) =>
            val cya = sessionData.flatMap(_.gains).getOrElse(AllGainsSessionModel(Seq.empty))
            val currentSession = cya.allGains.find(_.sessionId == sessionId)
            currentSession match {
              case None => errorHandler.internalServerError()
              case Some(session) =>
                val index = cya.allGains.indexOf(session)
                val newData = cya.allGains(index).copy(policyNumber = Some(policyNumber))
                val updated = cya.allGains.updated(index, newData)
                gainsSessionService.updateSessionData(AllGainsSessionModel(updated, cya.gateway), taxYear)(errorHandler.internalServerError()) {
                  Future.successful(if (newData.isFinished) {
                    Redirect(controllers.gains.routes.PolicySummaryController.show(taxYear, sessionId))
                  } else {
                    Redirect(controllers.gains.routes.GainsAmountController.show(taxYear, sessionId))
                  })
                }
            }
        }
    })
  }
}
