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
import forms.AmountForm
import models.BusinessTaxReliefs
import pages.businessTaxReliefs.QualifyingLoanReliefPage
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import views.html.pages.businessTaxReliefs.QualifyingLoanPageView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessReliefsQualifyingLoanController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                                        authorisedAction: AuthorisedAction,
                                                        retrieveJourney: JourneyDataRetrievalAction,
                                                        userAnswersService: UserAnswersService,
                                                        view: QualifyingLoanPageView)
                                                       (implicit appConfig: AppConfig, ec: ExecutionContext) extends BaseController {

  def form(isAgent: Boolean): Form[BigDecimal] =
    AmountForm.amountForm(
      emptyFieldKey = "business-reliefs.qualifying-loan.question.input.error.empty_field",
      wrongFormatKey = "business-reliefs.qualifying-loan.question.input.error.incorrect-characters",
      exceedsMaxAmountKey = s"business-reliefs.qualifying-loan.question.input.error.max-amount.${if (isAgent) "agent" else "individual"}",
      underMinAmountKey = Some("business-reliefs.qualifying-loan.question.input.error.negative")
    )

  def show(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen retrieveJourney(taxYear, BusinessTaxReliefs)).async { implicit request =>
      Future(Ok(view(taxYear, fillForm(QualifyingLoanReliefPage, form(request.user.isAgent)))))
    }

  def submit(taxYear: Int): Action[AnyContent] =
    (authorisedAction andThen retrieveJourney(taxYear, BusinessTaxReliefs)).async { implicit request =>
      form(request.user.isAgent).bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(taxYear, formWithErrors))),
          amount => {
            val updatedAnswers = request.userAnswers.set(QualifyingLoanReliefPage, amount)
            userAnswersService.set(updatedAnswers).map { _ =>
              Redirect(controllers.businessTaxReliefs.routes.BusinessReliefsQualifyingLoanController.show(taxYear))
            }
          }
        )
    }
}
