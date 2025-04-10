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

package forms.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.validation.{Constraints, Invalid, Valid}

class StringConstraintsSpec extends Constraints with AnyWordSpecLike with Matchers {

  private val maxLength = 15
  private val errMsgMaxLength = "Too Long"
  private val errMsgNonEmpty = "it is empty"
  private val errMsgInvalidChar = "there are invalid chars"

  "The StringConstraints.nonEmpty method" when {
    "supplied with empty value" should {
      "return invalid" in {
        StringConstraints.nonEmpty(errMsgNonEmpty)("") shouldBe Invalid(errMsgNonEmpty)
      }
    }

    "supplied with some value" should {
      "return valid" in {
        StringConstraints.nonEmpty(errMsgNonEmpty)("someValue") shouldBe Valid
      }
    }
  }

  "The StringConstraints.validateChar method" when {
    "supplied with a valid string" should {
      "return valid" in {
        val lowerCaseAlphabet = ('a' to 'z').mkString
        val upperCaseAlphabet = lowerCaseAlphabet.toUpperCase()
        val oneToNine = (1 to 9).mkString
        val otherChar = "&@£$€¥#.,:;-"
        val space = ""

        StringConstraints.validateChar("""^([ A-Za-z0-9&@£$€¥#.,:;-])*$""")(errMsgInvalidChar)(lowerCaseAlphabet + upperCaseAlphabet + space + oneToNine + otherChar + space) shouldBe Valid
      }
    }

    "supplied with a string which contains invalid characters" should {
      "return invalid" in {
        StringConstraints.validateChar("""^([ A-Za-z0-9&@£$€¥#.,:;-])*$""")(errMsgInvalidChar)("!()+{}?^~") shouldBe Invalid(errMsgInvalidChar)
      }
    }
  }

  "The StringConstraints.validateSize method" when {
    "supplied with a valid string" should {
      "return valid" in {
        StringConstraints.validateSize(maxLength)(errMsgMaxLength)("someInput") shouldBe Valid
      }
    }

    "supplied with a string that exceeds the maximum length" should {
      "return invalid" in {
        StringConstraints.validateSize(maxLength)(errMsgMaxLength)("stringExceedsMaxLength") shouldBe Invalid(errMsgMaxLength)
      }
    }
  }

  "The StringConstraints.validateMixedAlphaNumeric method" when {
    "supplied with a valid mixed alphanumeric string" should {
      "return valid" in {
        StringConstraints.validatePolicyNumber(errMsgInvalidChar)("P-89879-123") shouldBe Valid
      }
    }

    "supplied with a alphanumeric string containing a /" should {
      "return valid" in {
        StringConstraints.validatePolicyNumber(errMsgInvalidChar)("P/89879/123") shouldBe Valid
      }
    }

    "supplied with invalid alphanumeric string" should {
      "return invalid" in {
        StringConstraints.validatePolicyNumber(errMsgInvalidChar)("P#89879#123") shouldBe Invalid(errMsgInvalidChar)
      }
    }
  }

  "The StringConstraints.validateAlphabetsWithSpace method" when {
    "supplied with a valid alphabet string" should {
      "return valid" in {
        StringConstraints.validateAlphabetsWithSpace(errMsgInvalidChar)("test this input") shouldBe Valid
      }
    }

    "supplied with invalid alphabet string" should {
      "return invalid" in {
        StringConstraints.validateAlphabetsWithSpace(errMsgInvalidChar)("123 test") shouldBe Invalid(errMsgInvalidChar)
      }
    }
  }
}