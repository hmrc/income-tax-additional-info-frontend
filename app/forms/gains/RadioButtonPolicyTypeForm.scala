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

import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object RadioButtonPolicyTypeForm {

  val selectedOption = "policy-type"
  private val lifeInsuranceOption = "Life Insurance"
  private val lifeAnnuityOption = "Life Annuity"
  private val capitalRedemptionOption = "Capital Redemption"
  private val voidedIsaOption = "Voided ISA"
  private val foreignPolicyOption = "Foreign Policy"

  def formatter(missingInputError: String, errorArgs: Seq[Any] = Seq.empty): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      data.get(key) match {
        case Some("lifeInsurance") => Right(lifeInsuranceOption)
        case Some("lifeAnnuity") => Right(lifeAnnuityOption)
        case Some("capitalRedemption") => Right(capitalRedemptionOption)
        case Some("voidedIsa") => Right(voidedIsaOption)
        case Some("foreignPolicy") => Right(foreignPolicyOption)
        case _ => Left(Seq(FormError(key, missingInputError, errorArgs)))
      }
    }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  def radioButtonCustomOptionForm(missingInputError: String, errorArgs: Seq[Any] = Seq.empty): Form[String] = Form(
    single(selectedOption -> of(formatter(missingInputError, errorArgs)))
  )

}
