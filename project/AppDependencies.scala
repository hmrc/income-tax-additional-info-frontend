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

import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val bootstrapPlay28Version = "7.3.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-28" % bootstrapPlay28Version,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc"         % "3.32.0-play-28",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.13.4"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapPlay28Version  % "test, it",
    "org.scalatest"           %% "scalatest"                  % "3.2.14"                % Test,
    "org.jsoup"               %  "jsoup"                      % "1.15.3"                % Test,
    "com.typesafe.play"       %% "play-test"                  % current                 % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"              % "2.34.0"                % "test, it",
    "org.scalamock"           %% "scalamock"                  % "5.2.0"                 % Test,
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.62.2"                % "test, it"
  )
}
