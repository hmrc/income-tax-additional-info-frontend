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

import models.mongo.UserDataTemplate
import play.api.inject.Binding
import play.api.{Configuration, Environment}
import repositories.{GainsUserDataRepository, UserDataRepository}
import services._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.bootstrap.config.DeprecatedConfigChecker
import uk.gov.hmrc.play.bootstrap.filters.{AuditFilter, MDCFilter}
import uk.gov.hmrc.play.bootstrap.frontend.deprecatedClasses
import uk.gov.hmrc.play.bootstrap.frontend.filters.{DefaultFrontendAuditFilter, FiltersVerifier, FrontendMdcFilter, SessionTimeoutFilterConfig}
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.{ApplicationCryptoProvider, DefaultSessionCookieCryptoFilter, SessionCookieCrypto, SessionCookieCryptoFilter, SessionCookieCryptoProvider}
import uk.gov.hmrc.play.bootstrap.frontend.filters.deviceid.{DefaultDeviceIdFilter, DeviceIdFilter}

import java.time.Clock

class Module extends play.api.inject.Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {

    val sessionBinding: Binding[_] =
      if (configuration.get[Boolean]("feature-switch.newGainsServiceEnabled")) {
        bind[GainsSessionServiceProvider].to[NewGainsSessionServiceImpl].eagerly()
      } else {
        bind[GainsSessionServiceProvider].to[GainsSessionServiceImpl].eagerly()
      }


    Seq(
      sessionBinding,
      bind[Clock].toInstance(Clock.systemUTC()),
      bind[AppConfig].toSelf
    )
  }

}
