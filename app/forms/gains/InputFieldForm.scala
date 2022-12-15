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
import forms.validation.StringConstraints.validateAlphanumeric
import forms.validation.mappings.MappingUtil.trimmedText
import play.api.data.Form
import play.api.data.validation.Constraint
import play.api.data.validation.Constraints.{nonEmpty, pattern}

object InputFieldForm extends InputFilters {

  val value: String = "value"

  private val mixedAlphaNumericRegex = "^$|^(?=.*[0-9])(?=.*[a-zA-Z])([a-zA-Z0-9]+)$".r

  private def notEmpty(isAgent: Boolean, emptyFieldKey: String): Constraint[String] = nonEmpty(emptyFieldKey)

  private def isMixedAlphanumeric(wrongFormatKey: String): Constraint[String] = pattern(mixedAlphaNumericRegex, value, wrongFormatKey)

  def inputFieldForm(isAgent: Boolean, emptyFieldKey: String, wrongFormatKey: String): Form[String] = Form(
    if (wrongFormatKey == "") {
      value -> trimmedText.transform[String](filter, identity)
        .verifying(notEmpty(isAgent, emptyFieldKey))
    } else {
      value -> trimmedText.transform[String](filter, identity)
        .verifying(notEmpty(isAgent, emptyFieldKey), isMixedAlphanumeric(wrongFormatKey))
    }
  )

}
