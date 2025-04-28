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
import views.html.pages.businessTaxReliefs.CheckAnswersQualifyingLoanPageView

import javax.inject.Inject

class CheckAnswersQualifyingLoanController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                                     authorisedAction: AuthorisedAction,
                                                     retrieveJourney: JourneyDataRetrievalAction,
                                                     view: CheckAnswersQualifyingLoanPageView)
                                                    (implicit appConfig: AppConfig) extends BaseController {

  def show(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen retrieveJourney(taxYear, BusinessTaxReliefs)) { implicit request =>
      Ok(view(taxYear, routes.CheckAnswersQualifyingLoanController.submit(taxYear)))
    }

  def submit(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen retrieveJourney(taxYear, BusinessTaxReliefs)) { _ =>
      //TODO: In future, this will redirect to the 'have you completed this section' page
      NotImplemented
    }

}
