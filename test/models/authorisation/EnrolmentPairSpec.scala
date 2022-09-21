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

package models.authorisation

import org.scalatest.matchers.should.Matchers
import support.UnitTest

class EnrolmentPairSpec extends UnitTest with Matchers {

  "IndividualEnrolment" should {
    "have correct key value pair" in {
      IndividualEnrolment.key shouldBe "HMRC-MTD-IT"
      IndividualEnrolment.value shouldBe "MTDITID"
    }
  }

  "AgentEnrolment" should {
    "have correct key value pair" in {
      AgentEnrolment.key shouldBe "HMRC-AS-AGENT"
      AgentEnrolment.value shouldBe "AgentReferenceNumber"
    }
  }

  "NinoEnrolment" should {
    "have correct key value pair" in {
      NinoEnrolment.key shouldBe "HMRC-NI"
      NinoEnrolment.value shouldBe "NINO"
    }
  }
}
