/*
 * Copyright 2025 HM Revenue & Customs
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

package views.utils

trait BaseSelectors {

  val h1 = "h1"
  def h2(nth: Int): String = s"h2:nth-of-type($nth)"
  def p(nth: Int): String = s"p:nth-of-type($nth)"
  def bullet(nth: Int): String = s"ul li:nth-of-type($nth)"
  val summary = "summary"
  def summaryKey(row: Int): String = s".govuk-summary-list__row:nth-of-type($row) .govuk-summary-list__key"
  def summaryValue(row: Int): String = s".govuk-summary-list__row:nth-of-type($row) .govuk-summary-list__value"
  def summaryActions(row: Int, link: Int): String = s".govuk-summary-list__row:nth-of-type($row) .govuk-summary-list__actions a:nth-of-type($link)"
  val ammountHint = "#amount-hint"

}
