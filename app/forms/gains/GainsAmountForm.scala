/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.gains

import filters.InputFilters
import forms.validation.mappings.MappingUtil.trimmedText
import play.api.data.Form
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints.nonEmpty

object GainsAmountForm extends InputFilters {

  val GainAmount: String = "gainAmountNumber"

  def notEmpty(isAgent: Boolean): Constraint[String] =
    nonEmpty(s"gains.gain-amount.question.no-entry-error.${if (isAgent) "agent" else "individual"}")

  //need to add:
  //no entry
  //incorrect format error
  //amount exceeds maximum

  def gainsAmountForm(isAgent: Boolean): Form[String] = Form(
    GainAmount -> trimmedText.transform[String](filter, identity).verifying(notEmpty(isAgent))
  )

}
