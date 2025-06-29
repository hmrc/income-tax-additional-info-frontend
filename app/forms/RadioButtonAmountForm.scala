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

import forms.validation.mappings.MappingUtil.optionCurrency
import play.api.data.Forms.{of, tuple}
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, Form, FormError}

object RadioButtonAmountForm {

  val yesNo = "value"
  val yes = "true"
  val no = "false"
  val amount = "amount"

  def formatter(missingInputError: String): Formatter[Boolean] = new Formatter[Boolean] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] = {
      data.get(key) match {
        case Some(`yes`) => Right(true)
        case Some(`no`) => Right(false)
        case _ => Left(Seq(FormError(key, missingInputError)))
      }
    }

    override def unbind(key: String, value: Boolean): Map[String, String] = {
      Map(
        key -> value.toString
      )
    }
  }

  def radioButtonAndAmountForm(missingInputError: String,
                               emptyFieldKey: String,
                               wrongFormatKey: String,
                               exceedsMaxAmountKey: String,
                               minAmountKey: Option[String] = None
                              ): Form[(Boolean, Option[BigDecimal])] = {
    Form(
      tuple(
        yesNo -> of(formatter(missingInputError)),
        amount -> of(amountFormatter(
          requiredKey = emptyFieldKey,
          wrongFormatKey = wrongFormatKey,
          maxAmountKey = exceedsMaxAmountKey,
          minAmountKey = minAmountKey
        ))
      )
    )
  }

  private def amountFormatter(
                               requiredKey: String,
                               wrongFormatKey: String,
                               maxAmountKey: String,
                               minAmountKey: Option[String],
                               args: Seq[String] = Seq.empty[String]): Formatter[Option[BigDecimal]] = {
    new Formatter[Option[BigDecimal]] {

      val optionalCurrency: FieldMapping[Option[BigDecimal]] = optionCurrency(
        requiredKey = requiredKey,
        wrongFormatKey = wrongFormatKey,
        maxAmountKey = maxAmountKey,
        minAmountKey = minAmountKey,
        args = args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[BigDecimal]] = {
        data.get(yesNo) match {
          case Some("true") => optionCurrency(
            requiredKey = requiredKey,
            wrongFormatKey = wrongFormatKey,
            maxAmountKey = maxAmountKey,
            minAmountKey = minAmountKey,
            args = args).binder.bind(key, data)
          case _ => Right(None)
        }
      }

      override def unbind(key: String, value: Option[BigDecimal]): Map[String, String] =
        optionalCurrency.binder.unbind(key, value)
    }
  }
}
