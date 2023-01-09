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

package controllers

import com.google.inject.Inject
import config.AppConfig
import models.authorisation.SessionValues
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{SessionHelper, UrlUtils}

class LanguageSwitchController @Inject()(implicit mcc: MessagesControllerComponents, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with SessionHelper {

  def switchToLanguage(language: String): Action[AnyContent] = Action { implicit request =>
    val lang = appConfig.languageMap.getOrElse(language, Lang.defaultLang)
    val redirectURL = request.headers.get(REFERER)
      .flatMap(UrlUtils.asRelativeUrl)
      .getOrElse(fallbackURL)

    Redirect(redirectURL).withLang(Lang.apply(lang.code))
  }

  private def fallbackURL(implicit request: Request[_]): String = getFromSession(SessionValues.TAX_YEAR) match {
    case Some(taxYear) => appConfig.incomeTaxSubmissionStartUrl(taxYear.toInt)
    case None => appConfig.incomeTaxSubmissionStartUrl(appConfig.defaultTaxYear)
  }
}
