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

package fixtures.messages

import play.api.i18n.Lang
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En, Language}

sealed trait i18n {
  val language: Language
  lazy val lang: Lang = Lang(language.code)
  val continue = "Continue"
}

trait En extends i18n {
  override val language: Language = En
}

trait Cy extends i18n {
  override val language: Language = Cy
  override val continue = "Yn eich blaen"
}
