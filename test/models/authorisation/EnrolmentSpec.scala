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

class EnrolmentSpec extends UnitTest with Matchers {

  "Enrolment.Individual" should {
    "have correct key value pair" in {
      Enrolment.Individual.key shouldBe "HMRC-MTD-IT"
      Enrolment.Individual.value shouldBe "MTDITID"
    }
  }

  "Enrolment.Agent" should {
    "have correct key value pair" in {
      Enrolment.Agent.key shouldBe "HMRC-AS-AGENT"
      Enrolment.Agent.value shouldBe "AgentReferenceNumber"
    }
  }

  "Enrolment.Nino" should {
    "have correct key value pair" in {
      Enrolment.Nino.key shouldBe "HMRC-NI"
      Enrolment.Nino.value shouldBe "NINO"
    }
  }
}
