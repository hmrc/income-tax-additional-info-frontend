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

package models

import support.UnitTest
import support.builders.UserBuilder.aUser

class UserSpec extends UnitTest {

  ".isAgent" should {
    "return true when user arn non empty" in {
      val underTest = aUser.copy(arn = Some("any-arn"))

      underTest.isAgent shouldBe true
    }

    "return false when user arn is None" in {
      val underTest = aUser.copy(arn = None)

      underTest.isAgent shouldBe false
    }
  }
}
