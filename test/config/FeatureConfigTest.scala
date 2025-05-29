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

case class TestFeatureConfig(
   welshLanguageEnabled: Boolean,
   backendSessionEnabled: Boolean,
   splitGainsEnabled: Boolean,
   sessionCookieServiceEnabled: Boolean
) extends FeatureConfig {
  def enableWelshLanguage: TestFeatureConfig = copy(welshLanguageEnabled = true)
  def disableWelshLanguage: TestFeatureConfig = copy(welshLanguageEnabled = false)
  def enableBackendSession: TestFeatureConfig = copy(backendSessionEnabled = true)
  def disableBackendSession: TestFeatureConfig = copy(backendSessionEnabled = false)
  def enableSplitGains: TestFeatureConfig = copy(splitGainsEnabled = true)
  def disableSplitGains: TestFeatureConfig = copy(splitGainsEnabled = false)
  def enableSessionCookieService: TestFeatureConfig = copy(sessionCookieServiceEnabled = true)
  def disableSessionCookieService: TestFeatureConfig = copy(sessionCookieServiceEnabled = false)
}

object TestFeatureConfig {

  def allEnabled: TestFeatureConfig =
    TestFeatureConfig(
      welshLanguageEnabled = true,
      backendSessionEnabled = true,
      splitGainsEnabled = true,
      sessionCookieServiceEnabled = true
    )

  def allDisabled: TestFeatureConfig =
    TestFeatureConfig(
      welshLanguageEnabled = false,
      backendSessionEnabled = false,
      splitGainsEnabled = false,
      sessionCookieServiceEnabled = false
    )

}
