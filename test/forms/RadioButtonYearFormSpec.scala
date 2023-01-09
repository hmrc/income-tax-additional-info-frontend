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

import forms.RadioButtonYearForm._
import play.api.data.{Form, FormError}
import support.UnitTest

class RadioButtonYearFormSpec extends UnitTest {

  private def theForm() = RadioButtonYearForm.radioButtonAndYearForm(
    "no radio input",
    "nothing to see here",
    "this not good",
    "exceeds max"
  )

  private val testYearValid = 20
  private val testYearWithSpaces = " 2 0 "
  private val testYearEmpty = ""
  private val testYearInvalidInt = "!"
  private val testYearInvalidFormat = "wrong"
  private val testYearExceedsMax = "120"
  private val testYearZero = 0
  private val testYearNegative = -10

  "The RadioButtonYearForm" should {
    "correctly validate a year" when {
      "a valid year is entered" in {
        val testInput = Map(
          yesNo -> yes,
          yearInput -> testYearValid.toString
        )
        val expected = (yes.toBoolean, Option[Int](testYearValid))
        val actual = theForm().bind(testInput).value
        actual shouldBe Some(expected)
      }

      "zero is entered" in {
        val testInput = Map(
          yesNo -> yes,
          yearInput -> testYearZero.toString
        )
        val expected = Some((true, Some(0)))
        val actual = theForm().bind(testInput).value
        actual shouldBe expected
      }

      "a negative number is entered" in {
        val testInput = Map(
          yesNo -> yes,
          yearInput -> testYearNegative.toString
        )
        val expected = None
        val actual = theForm().bind(testInput).value
        actual shouldBe expected
      }
    }

    "correctly validate a year with spaces" when {
      "a valid year is entered" in {
        val testInput = Map(
          yesNo -> yes,
          yearInput -> testYearWithSpaces
        )
        val expected = (yes.toBoolean, Option[Int](20))
        val actual = theForm().bind(testInput).value
        actual shouldBe Some(expected)
      }
    }

    "invalidate an empty year" in {
      val testInput = Map(
        yesNo -> yes,
        yearInput -> testYearEmpty
      )
      val emptyTest = theForm().bind(testInput)
      emptyTest.errors should contain(FormError(yearInput, "nothing to see here"))
    }

    "invalidate year that includes invalid characters" in {
      val testInput = Map(
        yesNo -> yes,
        yearInput -> testYearInvalidInt
      )
      val invalidCharTest = theForm().bind(testInput)
      invalidCharTest.errors should contain(FormError(yearInput, "this not good"))
    }

    "invalidate a year that has incorrect formatting" in {
      val testInput = Map(
        yesNo -> yes,
        yearInput -> testYearInvalidFormat
      )
      val invalidFormatTest = theForm().bind(testInput)
      invalidFormatTest.errors should contain(FormError(yearInput, "this not good"))
    }

    "invalidate a year that exceeds max" in {
      val testInput = Map(
        yesNo -> yes,
        yearInput -> testYearExceedsMax
      )
      val maxYearTest = theForm().bind(testInput)
      maxYearTest.errors should contain(FormError(yearInput, "exceeds max"))
    }

    "return a Map of strings" when {
      "containing the input values using unbind" in {
        val testInput = (yes.toBoolean, Option[Int](testYearValid))
        val expected = Map(
          yesNo -> yes,
          yearInput -> testYearValid.toString
        )
        val actual = theForm().mapping.unbind(testInput)

        actual shouldBe expected
      }
    }

    "return a Boolean" when {
      "the answer is yes and valid year entered" in {
        val expectedResult = Some((true, Some(testYearValid)))
        val result = theForm().bind(
          Map(
            yesNo -> RadioButtonYearForm.yes,
            yearInput -> testYearValid.toString
          )
        ).value

        result shouldBe expectedResult
      }

      "the answer is no" in {
        val expectedResult = Some((false, None))
        val result = theForm().bind(Map(yesNo -> RadioButtonYearForm.no)).value

        result shouldBe expectedResult
      }
    }

    "return an error" when {
      "no option is returned" in {
        val expectedResult = Seq(FormError(yesNo, Seq("no radio input")))
        val result = theForm().bind(Map[String, String]()).errors

        result shouldBe expectedResult
      }

      "an option that isn't yes or no is returned" in {
        val expectedResult = Seq(FormError(yesNo, Seq("no radio input")))
        val result = theForm().bind(Map[String, String](yesNo -> "asdf")).errors

        result shouldBe expectedResult
      }
    }

    "return an error with arguments" in {
      val yesNoForm: Form[Boolean] = YesNoForm.yesNoForm("someError", Seq("someArgument"))
      val expectedResult = Seq(FormError(yesNo, Seq("someError"), Seq("someArgument")))
      val result = yesNoForm.bind(Map[String, String](yesNo -> "invalid")).errors

      result shouldBe expectedResult
    }
  }

  ".formatter(...)" should {
    "return an formatter with relevant unbind implementation" in {
      val anyBoolean = true
      formatter("any-string").unbind("some-key", anyBoolean) shouldBe Map("some-key" -> "true")
    }
  }
}