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

package utils

import support.UnitTest

class UrlUtilsSpec extends UnitTest {

  ".asRelativeUrl" should {
    "return None when no url passed" in {
      UrlUtils.asRelativeUrl("some:wrong:url") shouldBe None
    }

    "return relative url" in {
      val testUrl = "https://www.domain.com/resource?queryParameter=test#here"

      UrlUtils.asRelativeUrl(testUrl) shouldBe Some("/resource?queryParameter=test#here")
    }
  }
}
