/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.businessTaxReliefs

import actions.{AuthorisedAction, JourneyDataRetrievalAction}
import config.AppConfig
import controllers.BaseController
import models.BusinessTaxReliefs
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.businessTaxReliefs.OtherReliefsService
import views.html.pages.businessTaxReliefs.CheckAnswersPostCessationTradeReliefView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CheckAnswersPostCessationTradeReliefController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                                               authorisedAction: AuthorisedAction,
                                                               retrieveJourney: JourneyDataRetrievalAction,
                                                               view: CheckAnswersPostCessationTradeReliefView,
                                                               service: OtherReliefsService
                                                              )(implicit appConfig: AppConfig, ec: ExecutionContext) extends BaseController {

  def show(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen retrieveJourney(taxYear, BusinessTaxReliefs)) { implicit request =>
      Ok(view(taxYear, routes.CheckAnswersPostCessationTradeReliefController.submit(taxYear)))
    }

  def submit(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen retrieveJourney(taxYear, BusinessTaxReliefs)).async { implicit request =>
      service.submit(taxYear, request.userAnswers).map {
        _ => Redirect(appConfig.incomeTaxSubmissionOverviewUrl(taxYear))
      }
    }
}
