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

package forms

import forms.validation.mappings.MappingUtil.optionYear
import play.api.data.Forms.{of, tuple}
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, Form, FormError}

object RadioButtonYearForm {

  val yesNo = "value"
  val yes = "true"
  val no = "false"
  val yearInput = "last-gain"

  def radioButtonAndYearForm(missingRadioInputError: String,
                             emptyYearFieldError: String,
                             wrongYearFormatError: String,
                             exceedsMaxYearError: String,
                             errorArgs: Seq[String] = Seq.empty
                            ): Form[(Boolean, Option[Int])] = {
    Form(
      tuple(
        yesNo -> of(formatter(missingRadioInputError, errorArgs)),
        yearInput -> of(yearFormatter(
          requiredKey = emptyYearFieldError,
          wrongFormatKey = wrongYearFormatError,
          maxYearKey = exceedsMaxYearError,
          args = errorArgs
        )))
    )
  }

  def form(isAgent: Boolean): Form[Boolean] = YesNoForm.yesNoForm(
    s"gains.paid-tax-status.question.error.1.${if (isAgent) "agent" else "individual"}"
  )
  def formatter(missingRadioInputError: String, errorArgs: Seq[Any] = Seq.empty): Formatter[Boolean] = new Formatter[Boolean] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] = {
      data.get(key) match {
        case Some(`yes`) => Right(true)
        case Some(`no`) => Right(false)
        case _ => Left(Seq(FormError(key, missingRadioInputError, errorArgs)))
      }
    }

    override def unbind(key: String, value: Boolean): Map[String, String] =
      Map(key -> value.toString)
  }

  def yearFormatter(requiredKey: String,
                    wrongFormatKey: String,
                    maxYearKey: String,
                    args: Seq[String] = Seq.empty[String]): Formatter[Option[Int]] = {
    new Formatter[Option[Int]] {

      val optionalYear: FieldMapping[Option[Int]] = optionYear(
        requiredKey = requiredKey,
        wrongFormatKey = wrongFormatKey,
        maxYearKey = maxYearKey,
        args = args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Int]] = {
        data.get(yesNo) match {
          case Some("true") => optionYear(requiredKey,
            wrongFormatKey,
            maxYearKey,
            args = args).binder.bind(key, data)
          case _ => Right(None)
        }
      }

      override def unbind(key: String, value: Option[Int]): Map[String, String] = optionalYear.binder.unbind(key, value)
    }
  }
}