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
import forms.validation.mappings.MappingUtil.optionYear
import play.api.data.Form

object InputYearForm extends InputFilters {

  val numberOfYears: String = "policyHeld"

  def inputYearsForm(emptyFieldKey: String,
                     wrongFormatKey: String,
                     maxYearKey: String,
                     emptyFieldArguments: Seq[String] = Seq.empty[String]
                ): Form[Option[Int]] = Form(
    numberOfYears -> optionYear(
      requiredKey = emptyFieldKey,
      wrongFormatKey = wrongFormatKey,
      maxYearKey = maxYearKey,
      args = emptyFieldArguments
    )
  )

}
