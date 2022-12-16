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
  private def theForm() = InputFieldForm.inputFieldForm(isAgent = false, "error", "wrongFormat")

  private val testInputValid = "valid1"
  private val testInputEmpty = ""

  "The InputFieldForm" should {
    "correctly validate a mixed alphanumeric string" when {
      "a valid string is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputValid)
        val expected = testInputValid
        val actual = theForm().bind(testInput).value
        actual shouldBe Some(expected)
      }

      "an empty string is entered" in {
        val testInput = Map(InputFieldForm.value -> testInputEmpty)
        val actual = theForm().bind(testInput)

        actual.errors should contain(FormError(InputFieldForm.value, "error"))
      }
    }
  }
}
