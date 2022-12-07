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

package forms.validation.mappings

import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.control.Exception.nonFatalCatch

trait Formatters {

  private[mappings] def stringFormatter(errorKey: String, optional: Boolean = false): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None => Left(Seq(FormError(key, errorKey)))
        case Some(x) if x.trim.isEmpty && optional => Left(Seq(FormError(key, errorKey)))
        case Some(x) if x.trim.isEmpty && optional => Right(x.trim)
        case Some(s) => Right(s.trim)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value.trim)
  }

  private[mappings] def currencyFormatter(requiredKey: String,
                                          invalidNumericKey: String,
                                          maxAmountKey: String,
                                          minAmountKey: Option[String],
                                          args: Seq[String] = Seq.empty[String]
                                         ): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {
      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] = {
        betweenLimits(validAmount(baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .map(_.replace("£", ""))
          .map(_.replaceAll("""\s""", "")), key), key)
      }

      override def unbind(key: String, value: BigDecimal): Map[String, String] = baseFormatter.unbind(key, value.toString)

      private def validAmount(input: Either[Seq[FormError], String], key: String): Either[Seq[FormError], BigDecimal] = {

        val is2dp = """-?\d+|\d*\.\d{1,2}"""
        val validNumeric = """-?[0-9.]*"""

        input.flatMap {
          case s if s.isEmpty => Left(Seq(FormError(key, requiredKey, args)))
          case s if !s.matches(validNumeric) => Left(Seq(FormError(key, invalidNumericKey, args)))
          case s if !s.matches(is2dp) => Left(Seq(FormError(key, invalidNumericKey, args)))
          case s =>
            nonFatalCatch
              .either(BigDecimal(s.replaceAll("£", "")))
              .left.map(_ => Seq(FormError(key, invalidNumericKey, args)))
        }
      }

      private def betweenLimits(input: Either[Seq[FormError], BigDecimal], key: String) = {
        input.flatMap {
          case value if value >= BigDecimal("100000000000") => Left(Seq(FormError(key, maxAmountKey, args)))
          case value if value <= BigDecimal("0") && minAmountKey.isDefined => Left(Seq(FormError(key, minAmountKey.get, args)))
          case value => Right(value)
        }
      }
    }

  private[mappings] def yearFormatter(requiredKey: String,
                                      wrongFormatKey: String,
                                      maxYearKey: String,
                                      args: Seq[String] = Seq.empty[String]
                                     ): Formatter[Option[Int]] =
    new Formatter[Option[Int]] {
      private val baseFormatter = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Int]] = {
        validYear(baseFormatter
          .bind(key, data)
          .map(_.replaceAll("""\s""", "")), key)
      }

      override def unbind(key: String, value: Option[Int]): Map[String, String] = baseFormatter.unbind(key, value.getOrElse("").toString)

      private def validYear(input: Either[Seq[FormError], String], key: String): Either[Seq[FormError], Option[Int]] = {

        val validNumeric = """^[0-9]*"""

        input.flatMap {
          case s if s.isEmpty => Left(Seq(FormError(key, requiredKey, args)))
          case s if !s.matches(validNumeric) => Left(Seq(FormError(key, wrongFormatKey, args)))
          case s if s.toInt >= 100 => Left(Seq(FormError(key, maxYearKey, args)))
          case s => Right(Some(s.toInt))
        }
      }
    }
}