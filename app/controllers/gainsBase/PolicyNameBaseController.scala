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

package controllers.gainsBase

import com.google.inject.Inject
import config._
import controllers.gains.PolicyNameController
import controllers.gainsSplit.PolicyNameSplitController
import play.api.mvc.{Action, AnyContent}

class PolicyNameBaseController @Inject()(linearController: PolicyNameController,
                                         splitController: PolicyNameSplitController,
                                         appConfig: AppConfig) {

  def show(taxYear: Int, sessionId: String, policyType: Option[String]): Action[AnyContent] = {
    if (appConfig.isSplitGains) {
      splitController.show(taxYear, sessionId, policyType)
    } else {
      linearController.show(taxYear, sessionId)
    }
  }

  def submit(taxYear: Int, sessionId: String): Action[AnyContent] = {
    if (appConfig.isSplitGains) {
      splitController.submit(taxYear, sessionId)
    } else {
      linearController.submit(taxYear, sessionId)
    }
  }
}
