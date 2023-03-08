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

import forms.validation.mappings.MappingUtil.{optionString}
import play.api.data.Forms.{of, tuple}
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, Form, FormError}

object RadioButtonPolicyEventForm {

  val selectedOption = "policy-event"
  private val policyEventType1 = "Full or part surrender"
  private val policyEventType2 = "Policy matured or a death"
  private val policyEventType3 = "Sale or assignment of a policy"
  private val policyEventType4 = "Personal Portfolio Bond"
  private val policyEventType5 = "Other"

  val input = "other-text"

  def formatter(missingInputError: String): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      data.get(key) match {
        case Some(`policyEventType1`) => Right(policyEventType1)
        case Some(`policyEventType2`) => Right(policyEventType2)
        case Some(`policyEventType3`) => Right(policyEventType3)
        case Some(`policyEventType4`) => Right(policyEventType4)
        case Some(`policyEventType5`) => Right(policyEventType5)
        case _ => Left(Seq(FormError(key, missingInputError)))
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(
        key -> value
      )
    }
  }

  def radioButtonCustomOptionForm(missingInputError: String,
                                  emptyFieldKey: String,
                                  wrongFormatKey: String,
                                  emptyFieldArguments: Seq[String] = Seq.empty[String]
                                 ): Form[(String,String)] = {
    Form(
      tuple(
        selectedOption -> of(formatter(missingInputError)),
        input -> of(stringFormatter(
          requiredKey = emptyFieldKey,
          wrongFormatKey= wrongFormatKey,
          missingInputError = missingInputError,
          args = emptyFieldArguments)
        )
      )
    )
  }

  private def stringFormatter(
                               requiredKey: String,
                               wrongFormatKey: String,
                               missingInputError: String,
                               args: Seq[String] = Seq.empty[String]): Formatter[String] = {
    new Formatter[String] {

      val optionalString: FieldMapping[String] = optionString(
        missingInputError = missingInputError,
        wrongFormatKey = wrongFormatKey
      )

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
        data.get(selectedOption) match {
          case Some(`policyEventType5`) => optionString(
            missingInputError = missingInputError,
            wrongFormatKey = wrongFormatKey
          ).binder.bind(key, data)
          case _ => Right("")
        }
      }

      override def unbind(key: String, value: String): Map[String, String] =
        optionalString.binder.unbind(key, value)
    }
  }
}