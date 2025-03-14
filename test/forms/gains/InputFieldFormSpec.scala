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

import play.api.data.FormError
import support.UnitTest

class InputFieldFormSpec extends UnitTest{
  private def theForm(inputFormat: String) = InputFieldForm.inputFieldForm(isAgent = false, inputFormat, "nothing to see here", "wrong format")

  private val testInputNumber = "123"
  private val testInputAlphabetSlash = "test/"
  private val testInputRepeatedSlash = "P/89879/123"
  private val testInputAlphabetDash = "test-"
  private val testInputAlphabetHash = "P#89879#123"
  private val testInputAlphabetsWithSpace = "test this input"
  private val testInputPolicyNumber = "P-89879-123"
  private val testInputEmpty = ""
  private val testInputEmptySpace = ""

  "The InputFieldForm" should {
    "correctly validate a policy number string" when {
      "a valid string is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputPolicyNumber)
        val expected = testInputPolicyNumber
        val actual = theForm("policyNumber").bind(testInput).value
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

      "an invalid policy number is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputAlphabetHash)
        val actual = theForm("policyNumber").bind(testInput)

        actual.errors should contain(FormError(InputFieldForm.value, "wrong format"))
      }
    }

    "replace a '/' for a '-'" when {
      "an policy number with a '/' is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputAlphabetSlash)
        val expected = testInputAlphabetDash
        val actual = theForm("policyNumber").bind(testInput).value
        actual shouldBe Some(expected)
      }

      "an policy number with repeated '/' is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputRepeatedSlash)
        val expected = testInputPolicyNumber
        val actual = theForm("policyNumber").bind(testInput).value
        actual shouldBe Some(expected)
      }
    }

    "clear empty spaces" when {
      "an policy number with a ' ' is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputAlphabetsWithSpace)
        val expected = "testthisinput"
        val actual = theForm("policyNumber").bind(testInput).value
        actual shouldBe Some(expected)
      }
    }
  }
}