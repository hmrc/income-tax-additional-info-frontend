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
import models.AllGainsSessionModel
import models.gains.PolicyCyaModel
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.GainsSessionServiceProvider
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.HMRCHeaderNames.CORRELATION_ID
import views.html.pages.gains.GainsSummaryPageView

import javax.inject.Inject
import scala.concurrent.Future

class GainsSummaryController @Inject()(authorisedAction: AuthorisedAction,
                                       view: GainsSummaryPageView,
                                       gainsSessionService: GainsSessionServiceProvider,
                                       errorHandler: ErrorHandler)
                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents)
  extends FrontendController(mcc) with I18nSupport with Logging{
  private def getCorrelationid(implicit hc:HeaderCarrier) = hc.extraHeaders.find(_._1 == CORRELATION_ID).getOrElse("-")
  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
  //Clear the session and load it only with prior data to avoid any incomplete session data that may exists

    gainsSessionService.deleteSessionData(taxYear)(errorHandler.internalServerError()) {
      gainsSessionService.getAndHandle(taxYear)(errorHandler.internalServerError()) {
        (cya, prior) =>
          prior match {
            case Some(prior) =>
              logger.info("[GainsSummaryController][show] only prior exists. CorrelationId: " + getCorrelationid)
              val priorData = prior.toPolicyCya
              gainsSessionService.createSessionData(AllGainsSessionModel(priorData, gateway = Some(true)), taxYear)(
                errorHandler.internalServerError())(
                Future.successful(Ok(view(taxYear, priorData)))
              )
            case None =>
              logger.info("[GainsSummaryController][show] No cya and prior data found. CorrelationId: " + getCorrelationid)
              Future.successful(Ok(view(taxYear, Seq[PolicyCyaModel]())))
          }
      }
    }
  }
}