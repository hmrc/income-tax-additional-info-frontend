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

import actions.AuthorisedAction
import config.AppConfig
import forms.AmountForm
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.pages.businessReliefs.BusinessReliefsNonDeductiblePageView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessReliefsNonDeductibleController @Inject()(authorisedAction: AuthorisedAction,
                                                       view: BusinessReliefsNonDeductiblePageView)
                                                      (implicit appConfig: AppConfig, mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def form(isAgent: Boolean): Form[BigDecimal] =
    AmountForm.amountForm(
      emptyFieldKey = "business-reliefs.non-deductible.question.input.error.empty_field",
      wrongFormatKey = "business-reliefs.non-deductible.question.input.error.incorrect-characters",
      exceedsMaxAmountKey = s"business-reliefs.non-deductible.question.input.error.max-amount.${if (isAgent) "agent" else "individual"}",
      underMinAmountKey = Some("business-reliefs.non-deductible.question.input.error.negative")
    )

  def show(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    Future(Ok(view(taxYear, form(request.user.isAgent))))
  }

  def submit(taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit request =>
    form(request.user.isAgent).bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(taxYear, formWithErrors))),
        //TODO: Temporary redirect. Should redirect to CYA page
        _ => Future(Redirect(controllers.businessTaxReliefs.routes.BusinessReliefsNonDeductibleController.show(taxYear)))
      )
  }
}
