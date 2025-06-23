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

package config

import play.api.Configuration

import javax.inject.Inject

trait FeatureConfig {
  def welshLanguageEnabled: Boolean
  def backendSessionEnabled: Boolean
  def splitGainsEnabled: Boolean
  def sessionCookieServiceEnabled: Boolean
}

class FeatureConfigImpl @ Inject() (configuration: Configuration) extends FeatureConfig {

  val welshLanguageEnabled: Boolean = configuration.get[Boolean]("feature-switch.welshLanguageEnabled")
  val backendSessionEnabled: Boolean = configuration.get[Boolean]("feature-switch.backendSessionEnabled")
  val splitGainsEnabled: Boolean = configuration.get[Boolean]("feature-switch.split-gains")

  val sessionCookieServiceEnabled: Boolean = configuration.get[Boolean]("feature-switch.sessionCookieService")

}
