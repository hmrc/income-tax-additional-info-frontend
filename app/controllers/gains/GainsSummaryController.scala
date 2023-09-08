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
import models.gains.prior.GainsPriorDataModel
import models.requests.AuthorisationRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.GainsSessionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.gains.GainsSummaryPageView

import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class GainsSummaryController @Inject()(authorisedAction: AuthorisedAction,
                                       view: GainsSummaryPageView,
                                       gainsSessionService: GainsSessionService,
                                       errorHandler: ErrorHandler)
                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    gainsSessionService.getPriorData(taxYear).map {
      case Left(_) => errorHandler.internalServerError()
      case Right(prior) => Ok(view(taxYear, prior.toPolicyCya))
    }
  }
}