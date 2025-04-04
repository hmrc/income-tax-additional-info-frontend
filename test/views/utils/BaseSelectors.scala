/*
 * Copyright 2025 HM Revenue & Customs
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

package views.utils

trait BaseSelectors {

  def concat(selectors: String*): String = selectors.mkString(" ")

  val h1 = "h1"
  val h2: Int => String = i => s"h2:nth-of-type($i)"
  val p: Int => String = i => s"p:nth-of-type($i)"
  val bullet: Int => String = i => s"ul li:nth-of-type($i)"
  val summary = "summary"

}
