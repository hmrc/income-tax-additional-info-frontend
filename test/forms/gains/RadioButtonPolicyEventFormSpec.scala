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

import forms.gains.RadioButtonPolicyEventForm.{formatter, radioButtonCustomOptionForm}
import play.api.data.{Form, FormError}
import support.UnitTest

class RadioButtonPolicyEventFormSpec extends UnitTest {

  private val customOptionForm: Form[(String,String)] = RadioButtonPolicyEventForm.radioButtonCustomOptionForm("someError","error","error")

  "RadioButtonCustomOptionForm" should {

    "return Full or part surrender" when {
      "Full or part surrender is selected" in {
        val expectedResult = ("Full or part surrender", "")
        val result = customOptionForm.bind(Map("policy-event" -> "Full or part surrender")).get

        result shouldBe expectedResult
      }
    }

    "Policy matured or a death" when {
      "Policy matured or a death is selected" in {
        val expectedResult = ("Policy matured or a death", "")
        val result = customOptionForm.bind(Map("policy-event" -> "Policy matured or a death")).get

        result shouldBe expectedResult
      }
    }

    "Sale or assignment of a policy" when {
      "Sale or assignment of a policy is selected" in {
        val expectedResult = ("Sale or assignment of a policy", "")
        val result = customOptionForm.bind(Map("policy-event" -> "Sale or assignment of a policy")).get

        result shouldBe expectedResult
      }
    }

    "return Personal Portfolio Bond" when {
      "Personal Portfolio Bond is selected" in {
        val expectedResult = ("Personal Portfolio Bond", "")
        val result = customOptionForm.bind(Map("policy-event" -> "Personal Portfolio Bond")).get

        result shouldBe expectedResult
      }
    }

    "return Other" when {
      "Other is selected" in {
        val expectedResult = ("Other", "test")
        val result = customOptionForm.bind(Map("policy-event" -> "Other", "other-text" -> "test")).get

        result shouldBe expectedResult
      }
    }

    "return an error" when {
      "no option is returned" in {
        val expectedResult = Seq(FormError("policy-event", Seq("error")))
        val result = customOptionForm.bind(Map[String, String]()).errors

        result shouldBe expectedResult
      }

      "return an error" when {
        "other is selected but text is empty" in {
          val expectedResult = Seq(FormError("other-text", Seq("someError")))
          val result = customOptionForm.bind(Map("policy-event" -> "Other")).errors

          result shouldBe expectedResult
        }

      }

      "return an error with arguments" in {
        val yesNoForm: Form[(String, String)] = radioButtonCustomOptionForm("error", "error", "error")
        val expectedResult = Seq(FormError("policy-event", Seq("error")))
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

  }}
