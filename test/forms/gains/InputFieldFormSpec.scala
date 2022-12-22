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

package forms.gains

import play.api.data.FormError
import support.UnitTest

class InputFieldFormSpec extends UnitTest{
  private def theForm(inputFormat: String) = InputFieldForm.inputFieldForm(isAgent = false, inputFormat, "nothing to see here", "wrong format")

  private val testInputNumber = "123"
  private val testInputAlphabet = "test"
  private val testInputAlphabetsWithSpace = "test this input"
  private val testInputMixedAlphaNumeric = "INPOLY123A"
  private val testInputEmpty = ""
  private val testInputEmptySpace = ""

  "The InputFieldForm" should {
    "correctly validate a mixed alphanumeric string" when {
      "a valid string is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputMixedAlphaNumeric)
        val expected = testInputMixedAlphaNumeric
        val actual = theForm("mixedAlphaNumeric").bind(testInput).value
        actual shouldBe Some(expected)
      }

      "a valid alphabet is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputAlphabetsWithSpace)
        val expected = testInputAlphabetsWithSpace
        val actual = theForm("alphabetsWithSpace").bind(testInput).value
        actual shouldBe Some(expected)
      }

      "an empty string is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputEmpty)
        val actual = theForm("alphabetsWithSpace").bind(testInput)

        actual.errors should contain(FormError(InputFieldForm.value, "nothing to see here"))
      }

      "an empty string with is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputEmptySpace)
        val actual = theForm("alphabetsWithSpace").bind(testInput)

        actual.errors should contain(FormError(InputFieldForm.value, "nothing to see here"))
      }

      "an invalid alphabet is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputNumber)
        val actual = theForm("alphabetsWithSpace").bind(testInput)

        actual.errors should contain(FormError(InputFieldForm.value, "wrong format"))
      }

      "an invalid mixed alphanumeric is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputAlphabet)
        val actual = theForm("mixedAlphaNumeric").bind(testInput)

        actual.errors should contain(FormError(InputFieldForm.value, "wrong format"))
      }
    }
  }
}