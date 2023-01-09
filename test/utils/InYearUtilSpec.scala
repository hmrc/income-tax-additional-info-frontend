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

import org.scalamock.scalatest.MockFactory
import support.UnitTest
import utils.InYearUtil.{inYear, toDateWithinTaxYear}

import java.time.{LocalDate, LocalDateTime}

class InYearUtilSpec extends UnitTest
  with MockFactory {

  private val year2022: Int = 2022
  private val month4: Int = 4
  private val month5: Int = 4
  private val day5: Int = 5
  private val day6: Int = 6
  private val hour23: Int = 23
  private val minute59: Int = 59

  "InYearAction.inYear" when {
    "return true when taxYear is 2022 and current date is 5th April 2022 and time is just before midnight" in {
      val currentDate: LocalDateTime = LocalDateTime.of(year2022, month4, day5, hour23, minute59)
      inYear(year2022, currentDate) shouldBe true
    }

    "return false when taxYear is 2022 and current date is 6th April 2022 at midnight" in {
      val currentDate: LocalDateTime = LocalDateTime.of(year2022, month4, day6, 0, 0)
      inYear(year2022, currentDate) shouldBe false
    }

    "return false when taxYear is 2022 and current date is 6th April 2022 at one minute past midnight" in {
      val currentDate: LocalDateTime = LocalDateTime.of(year2022, month4, day6, 0, 1)
      inYear(year2022, currentDate) shouldBe false
    }

    "return false when taxYear is 2022 and current date is 6th April 2022 at one hour past midnight" in {
      val currentDate: LocalDateTime = LocalDateTime.of(year2022, month4, day6, 1, 1)
      inYear(year2022, currentDate) shouldBe false
    }
  }

  "InYearAction.toDateWithinTaxYear" should {
    "return given date when date is after the beginning of given tax year" in {
      toDateWithinTaxYear(year2022, LocalDate.of(year2022, month5, day5)) shouldBe LocalDate.of(year2022, month5, day5)
    }
    "return date at start of taxYear when date is before the beginning of given tax year" in {
      toDateWithinTaxYear(year2022, LocalDate.of(year2022 - 1, month4, day5)) shouldBe LocalDate.of(year2022 - 1, month4, day6)
    }
  }
}
