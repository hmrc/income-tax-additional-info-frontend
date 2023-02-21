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

import forms.gains.RadioButtonPolicyTypeForm.{formatter, radioButtonCustomOptionForm}
import play.api.data.{Form, FormError}
import support.UnitTest

class RadioButtonPolicyTypeFormSpec extends UnitTest {

  private val customOptionForm: Form[String] = RadioButtonPolicyTypeForm.radioButtonCustomOptionForm("someError")

  "RadioButtonCustomOptionForm" should {

    "return Life Insurance" when {
      "Life Insurance is selected" in {
        val expectedResult = "Life Insurance"
        val result = customOptionForm.bind(Map("policy-type" -> "lifeInsurance")).get

        result shouldBe expectedResult
      }
    }

    "return Life Annuity" when {
      "Life Annuity is selected" in {
        val expectedResult = "Life Annuity"
        val result = customOptionForm.bind(Map("policy-type" -> "lifeAnnuity")).get

        result shouldBe expectedResult
      }
    }

    "return Capital Redemption" when {
      "Capital Redemption is selected" in {
        val expectedResult = "Capital Redemption"
        val result = customOptionForm.bind(Map("policy-type" -> "capitalRedemption")).get

        result shouldBe expectedResult
      }
    }

    "return Voided ISA" when {
      "Voided ISA is selected" in {
        val expectedResult = "Voided ISA"
        val result = customOptionForm.bind(Map("policy-type" -> "voidedIsa")).get

        result shouldBe expectedResult
      }
    }

    "return A foreign policy" when {
      "A foreign policy is selected" in {
        val expectedResult = "A foreign policy"
        val result = customOptionForm.bind(Map("policy-type" -> "foreignPolicy")).get

        result shouldBe expectedResult
      }
    }

    "return an error" when {
      "no option is returned" in {
        val expectedResult = Seq(FormError("policy-type", Seq("someError")))
        val result = customOptionForm.bind(Map[String, String]()).errors

        result shouldBe expectedResult
      }

    }

    "return an error with arguments" in {
      val yesNoForm: Form[String] = radioButtonCustomOptionForm("someError", Seq("someArgument"))
      val expectedResult = Seq(FormError("policy-type", Seq("someError"), Seq("someArgument")))
      val result = yesNoForm.bind(Map[String, String]("" -> "invalid")).errors

      result shouldBe expectedResult
    }
  }

  ".formatter(...)" should {
    "return an formatter with relevant unbind implementation" in {
      val anyString = ""
      formatter("any-string").unbind("some-key", anyString) shouldBe Map("some-key" -> "")
    }
  }
}
