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

package utils

import play.api.i18n.Messages

import java.time.LocalDate
import scala.util.Try

object ViewUtils {

  def bigDecimalCurrency(value: String, currencySymbol: String = "£"): String =
    Try(BigDecimal(value))
      .map(amount => currencySymbol + f"$amount%1.2f".replace(".00", ""))
      .getOrElse(value)
      .replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",")

  def translatedDateFormatter(date: LocalDate)(implicit messages: Messages): String = {
    val translatedMonth = messages("common." + date.getMonth.toString.toLowerCase)
    date.getDayOfMonth + " " + translatedMonth + " " + date.getYear
  }
}
