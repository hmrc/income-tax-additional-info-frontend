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
import services.GainsSessionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.GainsSummaryPageView

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GainsSummaryController @Inject()(authorisedAction: AuthorisedAction,
                                       view: GainsSummaryPageView,
                                       gainsSessionService: GainsSessionService,
                                       errorHandler: ErrorHandler)
                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging{

  private def getCorrelationid(implicit hc:HeaderCarrier) = hc.extraHeaders.find(_._1 == "X-CorrelationId").getOrElse(UUID.randomUUID())

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>

    gainsSessionService.getAndHandle(taxYear)(Future.successful(errorHandler.internalServerError())) {
      (cya, prior) =>
        (cya, prior) match {
          case (Some(cya), Some(prior)) if cya.allGains.nonEmpty =>
            val allGainsPolicies: Seq[PolicyCyaModel] = (cya.allGains ++ prior.toPolicyCya).distinctBy(_.policyNumber)
            gainsSessionService.updateSessionData(AllGainsSessionModel(allGainsPolicies, cya.gateway), taxYear)(
              errorHandler.internalServerError())(Ok(view(taxYear, allGainsPolicies)))
          case (None, Some(prior)) =>
            val priorData = prior.toPolicyCya
            gainsSessionService.createSessionData(AllGainsSessionModel(priorData, gateway = Some(true)), taxYear)(
              errorHandler.internalServerError())(
              Ok(view(taxYear, priorData))
            )
          case _ =>
            Future.successful(Ok(view(taxYear, Seq[PolicyCyaModel]())))
        }
    }.flatten
  }
}