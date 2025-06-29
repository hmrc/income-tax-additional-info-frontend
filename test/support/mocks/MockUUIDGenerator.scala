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

package support.mocks

import org.scalamock.handlers.CallHandler0
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import utils.UUIDGenerator

trait MockUUIDGenerator extends MockFactory { _: TestSuite =>

  protected val mockUUIDGenerator: UUIDGenerator = mock[UUIDGenerator]

  def mockRandomUUID(result: String): CallHandler0[String] =
    (() => mockUUIDGenerator.randomUUID())
      .expects()
      .returning(result)
}
