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

package forms.gains

import filters.InputFilters
import forms.validation.StringConstraints.{nonEmpty, validateAlphabetsWithSpace, validatePolicyNumber}
import forms.validation.mappings.MappingUtil.trimmedText
import play.api.data.Form
import play.api.data.validation.Constraint
import forms.validation.utils.ConstraintUtil.ConstraintUtil

object InputFieldForm extends InputFilters {

  val value: String = "value"

  def notEmpty(isAgent: Boolean, emptyFieldKey: String): Constraint[String] = nonEmpty(emptyFieldKey)

  def isValidAlphabetsWithSpace(wrongFormatKey: String): Constraint[String] = validateAlphabetsWithSpace(wrongFormatKey)

  def isValidPolicyNumber(wrongFormatKey: String): Constraint[String] = validatePolicyNumber(wrongFormatKey)

  def inputFieldForm(isAgent: Boolean, inputFormat: String, emptyFieldKey: String, wrongFormatKey: String): Form[String] = Form(
    inputFormat match {
      case "alphabetsWithSpace" => value -> trimmedText.transform[String](filter, identity).verifying(
        notEmpty(isAgent, emptyFieldKey) andThen isValidAlphabetsWithSpace(wrongFormatKey))
      case "policyNumber" => value -> trimmedText.transform[String](filter, identity)
        .verifying(notEmpty(isAgent, emptyFieldKey) andThen isValidPolicyNumber(wrongFormatKey))
    }
  )

}