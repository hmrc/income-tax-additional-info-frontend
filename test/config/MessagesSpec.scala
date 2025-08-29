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

package config

import play.api.i18n.MessagesApi
import support.ViewUnitTest

import scala.collection.immutable.HashSet

class MessagesSpec extends ViewUnitTest {

  private val exclusionKeys: Set[String] = Set("global.error.fallbackClientError4xx.heading", "global.error.fallbackClientError4xx.message",
    "internal-server-error-template.heading", "back.text", "global.error.pageNotFound404.message", "internal-server-error-template.paragraph.1",
    "radios.yesnoitems.no", "phase.banner.before", "betaBar.banner.message.3", "radios.yesnoitems.yes", "global.error.badRequest400.message",
    "phase.banner.after", "betaBar.banner.message.2", "common.yes", "common.back", "phase.banner.link", "betaBar.banner.message.1", "common.no",
    "global.error.fallbackClientError4xx.title", "language.day.plural", "language.day.singular")

  private lazy val allLanguages: Map[String, Map[String, String]] = app.injector.instanceOf[MessagesApi].messages

  private val defaults = allLanguages("default")
  private val welsh = allLanguages("cy")


  "the messages file must have welsh translations" should {
    "check all keys in the default file other than those in the exclusion list has a corresponding translation" in {
      defaults.keys.foreach(
        key =>
          if (!exclusionKeys.contains(key)) {
            welsh.keys should contain(key)
          }
      )
    }
  }

  "the english messages file" should {
    "have no duplicate messages(values)" in {
      val messages: List[(String, String)] = defaults.filter(entry => !exclusionKeys.contains(entry._1)).toList

      val result = checkMessagesAreUnique(messages, messages, Set())

      result shouldBe HashSet("global.error.badRequest400.title", "global.error.badRequest400.heading", "global.error.InternalServerError500.title", "global.error.InternalServerError500.heading", "not-found-template.heading", "global.error.pageNotFound404.title")
    }
  }

  "the welsh messages file" should {
    "have no duplicate messages(values)" in {
      val messages: List[(String, String)] = welsh.filter(entry => !exclusionKeys.contains(entry._1)).toList

      val result = checkMessagesAreUnique(messages, messages, Set())

      result shouldBe HashSet("global.error.badRequest400.title", "global.error.badRequest400.heading", "global.error.InternalServerError500.title", "global.error.InternalServerError500.heading", "not-found-template.heading", "global.error.pageNotFound404.title")
    }
  }
  override protected val userScenarios: Seq[UserScenario[_, _]] = Seq.empty
}
