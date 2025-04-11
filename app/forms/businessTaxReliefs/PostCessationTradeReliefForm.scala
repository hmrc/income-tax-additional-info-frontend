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

package forms.businessTaxReliefs

import forms.validation.mappings.MappingUtil.currency
import models.requests.JourneyDataRequest
import play.api.data.Form
import play.api.i18n.Messages
import utils.ViewUtils.dynamicMessage

object PostCessationTradeReliefForm {

  val key: String = "amount"

  def apply()(implicit messages: Messages, request: JourneyDataRequest[_]): Form[BigDecimal] =
    Form(
      key -> currency(
        requiredKey = dynamicMessage("postCessationTradeRelief.amount.error.required"),
        wrongFormatKey = messages("postCessationTradeRelief.amount.error.invalid"),
        maxAmountKey = dynamicMessage("postCessationTradeRelief.amount.error.max"),
        minAmountKey = Some(dynamicMessage("postCessationTradeRelief.amount.error.min"))
      )
    )
}
