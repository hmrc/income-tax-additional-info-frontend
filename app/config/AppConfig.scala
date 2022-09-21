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

package config

import play.api.i18n.Lang
import play.api.mvc.{Call, RequestHeader}
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig) {

  private lazy val incomeTaxSubmissionFrontendUrlKey = "microservice.services.income-tax-submission-frontend.url"
  private lazy val basGatewayFrontendUrlKey = "microservice.services.bas-gateway-frontend.url"
  private lazy val feedbackFrontendUrlKey = "microservice.services.feedback-frontend.url"
  private lazy val contactFormServiceIndividualKey = "update-and-submit-income-tax-return"
  private lazy val contactFormServiceAgentKey = "update-and-submit-income-tax-return-agent"
  private lazy val contactFrontendUrlKey = "microservice.services.contact-frontend.url"
  private lazy val viewAndChangeUrlKey = "microservice.services.view-and-change.url"
  private lazy val signInContinueUrlKey = "microservice.services.sign-in.continueUrl"

  private lazy val applicationUrl: String = servicesConfig.getString("microservice.url")
  private lazy val basGatewayUrl = servicesConfig.getString(basGatewayFrontendUrlKey)
  private lazy val feedbackFrontendUrl = servicesConfig.getString(feedbackFrontendUrlKey)
  private lazy val contactFrontEndUrl = servicesConfig.getString(contactFrontendUrlKey)
  private lazy val vcBaseUrl: String = servicesConfig.getString(viewAndChangeUrlKey)
  private lazy val signInBaseUrl: String = servicesConfig.getString("microservice.services.sign-in.url")
  private lazy val signInContinueBaseUrl: String = servicesConfig.getString(signInContinueUrlKey)
  private lazy val signInContinueUrlRedirect: String = SafeRedirectUrl(signInContinueBaseUrl).encodedUrl
  private lazy val signInOrigin = servicesConfig.getString("appName")

  lazy val signOutUrl: String = s"$basGatewayUrl/bas-gateway/sign-out-without-state"
  lazy val welshLanguageEnabled: Boolean = servicesConfig.getBoolean(key = "feature-switch.welshLanguageEnabled")
  lazy val defaultTaxYear: Int = servicesConfig.getInt(key = "defaultTaxYear")
  lazy val languageMap: Map[String, Lang] = Map("english" -> Lang("en"), "cymraeg" -> Lang("cy"))
  lazy val timeoutDialogTimeout: Int = servicesConfig.getInt("timeoutDialogTimeout")
  lazy val timeoutDialogCountdown: Int = servicesConfig.getInt("timeoutDialogCountdown")
  lazy val signInUrl: String = s"$signInBaseUrl?continue=$signInContinueUrlRedirect&origin=$signInOrigin"

  def incomeTaxSubmissionBaseUrl: String = servicesConfig.getString(incomeTaxSubmissionFrontendUrlKey) +
    servicesConfig.getString(key = "microservice.services.income-tax-submission-frontend.context")

  def incomeTaxSubmissionStartUrl(taxYear: Int): String = incomeTaxSubmissionBaseUrl + "/" + taxYear + "/start"

  def incomeTaxSubmissionIvRedirect: String = incomeTaxSubmissionBaseUrl +
    servicesConfig.getString("microservice.services.income-tax-submission-frontend.iv-redirect")

  def incomeTaxSubmissionOverviewUrl(taxYear: Int): String = incomeTaxSubmissionBaseUrl + "/" + taxYear +
    servicesConfig.getString("microservice.services.income-tax-submission-frontend.overview")

  def contactUrl(isAgent: Boolean): String = s"$contactFrontEndUrl/contact/contact-hmrc?service=${contactFormServiceIdentifier(isAgent)}"

  def contactFormServiceIdentifier(isAgent: Boolean): String = if (isAgent) contactFormServiceAgentKey else contactFormServiceIndividualKey

  def feedbackSurveyUrl(isAgent: Boolean): String = s"$feedbackFrontendUrl/feedback/${contactFormServiceIdentifier(isAgent)}"

  def viewAndChangeEnterUtrUrl: String = s"$vcBaseUrl/report-quarterly/income-and-expenses/view/agents/client-utr"

  def betaFeedbackUrl(request: RequestHeader, isAgent: Boolean): String = {
    val requestUri = SafeRedirectUrl(applicationUrl + request.uri).encodedUrl
    val contactFormService = contactFormServiceIdentifier(isAgent)
    s"$contactFrontEndUrl/contact/beta-feedback?service=$contactFormService&backUrl=$requestUri"
  }

  def taxYearErrorFeature: Boolean = servicesConfig.getBoolean("taxYearErrorFeatureSwitch")

  // TODO: Get rid of this
  def routeToSwitchLanguage: String => Call =
    (lang: String) => controllers.routes.LanguageSwitchController.switchToLanguage(lang)
}
