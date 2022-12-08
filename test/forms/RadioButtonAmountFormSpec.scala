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

package forms

import forms.RadioButtonAmountForm._
import play.api.data.{FormError, Form}
import support.UnitTest

class RadioButtonAmountFormSpec extends UnitTest {

  private def theForm() = RadioButtonAmountForm.radioButtonAndAmountForm(
    "no radio input",
    "nothing to see here",
    "this not good",
    "too big",
    ""
  )

  private val testCurrencyValid = 1000
  private val testCurrencyWithSpaces = "100 0. 00"
  private val testCurrencyWithSpacesExpected = 1000.00
  private val testCurrencyEmpty = ""
  private val testCurrencyInvalidInt = "!"
  private val testCurrencyInvalidFormat = 12345.123
  private val testCurrencyTooBig = "100000000000.00"
  private val testCurrencyZeroAmount = 0
  private val testCurrencyNegativeAmount = -100

  "The RadioButtonAmountForm" should {
    "correctly validate a currency" when {
      "a valid currency is entered" in {
        val testInput = Map(
          yesNo -> yes,
          amount -> testCurrencyValid.toString
        )
        val expected = Some((yes.toBoolean, Option[BigDecimal](testCurrencyValid)))
        val actual = theForm().bind(testInput).value
        actual shouldBe expected
      }

      "zero is entered" in {
        val testInput = Map(
          yesNo -> yes,
          amount -> testCurrencyZeroAmount.toString
        )
        val expected = Some((yes.toBoolean, Option[BigDecimal](testCurrencyZeroAmount)))
        val actual = theForm().bind(testInput).value
        actual shouldBe expected
      }

      "a negative number is entered" in {
        val testInput = Map(
          yesNo -> yes,
          amount -> testCurrencyNegativeAmount.toString
        )
        val expected = Some((yes.toBoolean, Option[BigDecimal](testCurrencyNegativeAmount)))
        val actual = theForm().bind(testInput).value
        actual shouldBe expected
      }
    }

    "correctly validate a currency with spaces" when {
      "a valid currency is entered" in {
        val testInput = Map(
          yesNo -> yes,
          amount -> testCurrencyWithSpaces
        )
        val expected = Some((yes.toBoolean, Option[BigDecimal](testCurrencyWithSpacesExpected)))
        val actual = theForm().bind(testInput).value
        actual shouldBe expected
      }
    }

    "invalidate an empty currency" in {
      val testInput = Map(
        yesNo -> yes,
        amount -> testCurrencyEmpty
      )
      val emptyTest = theForm().bind(testInput)
      emptyTest.errors should contain(FormError(amount, "nothing to see here"))
    }

    "invalidate currency that includes invalid characters" in {
      val testInput = Map(
        yesNo -> yes,
        amount -> testCurrencyInvalidInt
      )
      val invalidCharTest = theForm().bind(testInput)
      invalidCharTest.errors should contain(FormError(amount, "this not good"))
    }

    "invalidate a currency that has incorrect formatting" in {
      val testInput = Map(
        yesNo -> yes,
        amount -> testCurrencyInvalidFormat.toString
      )
      val invalidFormatTest = theForm().bind(testInput)
      invalidFormatTest.errors should contain(FormError(amount, "this not good"))
    }

    "invalidate a currency that is too big" in {
      val testInput = Map(
        yesNo -> yes,
        amount -> testCurrencyTooBig
      )
      val bigCurrencyTest = theForm().bind(testInput)
      bigCurrencyTest.errors should contain(FormError(amount, "too big"))
    }

    "return a Map of strings" when {
      "containing the input values using unbind" in {
        val testInput = (yes.toBoolean, Option[BigDecimal](testCurrencyValid))
        val expected = Map(
          yesNo -> yes,
          amount -> testCurrencyValid.toString
        )
        val actual = theForm().mapping.unbind(testInput)

        actual shouldBe expected
      }
    }

    "return a Boolean" when {
      "the answer is yes and valid amount entered" in {
        val expectedResult = Some((true, Some(testCurrencyValid)))
        val result = theForm().bind(
          Map(
            yesNo -> RadioButtonAmountForm.yes,
            amount -> testCurrencyValid.toString
          )
        ).value

        result shouldBe expectedResult
      }

      "the answer is no" in {
        val expectedResult = Some((false, None))
        val result = theForm().bind(Map(yesNo -> RadioButtonAmountForm.no)).value

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
