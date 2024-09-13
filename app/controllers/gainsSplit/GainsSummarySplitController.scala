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
import models.AllGainsSessionModel
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.GainsSessionServiceProvider
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.HMRCHeaderNames.CORRELATION_ID
import views.html.pages.gains.GainsSummaryPageView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GainsSummarySplitController @Inject()(authorisedAction: AuthorisedAction,
                                            view: GainsSummaryPageView,
                                            gainsSessionService: GainsSessionServiceProvider,
                                            errorHandler: ErrorHandler)
                                           (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging {
  private def getCorrelationid(implicit hc: HeaderCarrier): Serializable = hc.extraHeaders.find(_._1 == CORRELATION_ID).getOrElse("-")

  private val LIFE_INSURANCE: String = "Life Insurance"
  private val LIFE_ANNUITY: String = "Life Annuity"
  private val CAPITAL_REDEMPTION: String = "Capital Redemption"
  private val VOIDED_ISA: String = "Voided ISA"

  def show(taxYear: Int, policyType: Option[String]): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.deleteSessionData(taxYear)(Future.successful(errorHandler.internalServerError())) {
      gainsSessionService.getAndHandle(taxYear)(Future.successful(errorHandler.internalServerError())) {
        (_, prior) =>
          prior match {
            case Some(prior) =>
              val priorData = policyType.getOrElse("") match {
                case LIFE_INSURANCE => prior.toPolicyCya.filter(_.policyType.contains(LIFE_INSURANCE))
                case LIFE_ANNUITY => prior.toPolicyCya.filter(_.policyType.contains(LIFE_ANNUITY))
                case CAPITAL_REDEMPTION => prior.toPolicyCya.filter(_.policyType.contains(CAPITAL_REDEMPTION))
                case VOIDED_ISA => prior.toPolicyCya.filter(_.policyType.contains(VOIDED_ISA))
                case _ => Seq.empty
              }

              if (priorData.isEmpty) {
                logger.info("[GainsSummarySplitController][show] No policy type in request or no prior exists, redirecting to task list.")
                Future.successful(Redirect(s"${appConfig.incomeTaxSubmissionBaseUrl}/$taxYear/tasklist"))
              } else {
                gainsSessionService.createSessionData(AllGainsSessionModel(priorData, gateway = Some(true)), taxYear)(errorHandler.internalServerError())(
                  Ok(view(taxYear, priorData))
                )
              }

            case None =>
              logger.info("[GainsSummarySplitController][show] No cya or prior data found, redirecting to task list. CorrelationId: " + getCorrelationid)
              Future.successful(Redirect(s"${appConfig.incomeTaxSubmissionBaseUrl}/$taxYear/tasklist"))
          }
      }.flatten
    }.flatten
  }
}
