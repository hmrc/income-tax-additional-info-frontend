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

package utils

import java.time.{LocalDate, LocalDateTime, ZoneId}

object InYearUtil {

  private val londonZoneId = ZoneId.of("Europe/London")
  private val taxYearStartDay = 6
  private val taxYearStartMonth = 4
  private val taxYearStartHour = 0
  private val taxYearStartMinute = 0

  def inYear(taxYear: Int, now: LocalDateTime = LocalDateTime.now): Boolean = {
    val endOfYearCutOffDate = LocalDateTime.of(taxYear, taxYearStartMonth, taxYearStartDay, taxYearStartHour, taxYearStartMinute)
    now.atZone(londonZoneId).isBefore(endOfYearCutOffDate.atZone(londonZoneId))
  }

  def toDateWithinTaxYear(taxYear: Int, localDate: LocalDate): LocalDate = {
    val startOfFinancialYear = LocalDateTime.of(taxYear - 1, taxYearStartMonth, taxYearStartDay, taxYearStartHour, taxYearStartMinute)
    if (localDate.atStartOfDay(londonZoneId).isBefore(startOfFinancialYear.atZone(londonZoneId))) startOfFinancialYear.toLocalDate else localDate
  }
}
