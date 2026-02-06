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

import sbt.*

object AppDependencies {

  private val hmrcMongoPlayVersion = "2.12.0"
  private val bootstrapPlay30Version = "10.5.0"
  private val hmrcPlayFrontend = "12.31.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30" % bootstrapPlay30Version,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30" % hmrcPlayFrontend,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"       % "2.21.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapPlay30Version  % Test,
    "org.jsoup"               %  "jsoup"                      % "1.22.1"                % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "7.0.2"                 % Test,
    "org.scalamock"           %% "scalamock"                  % "7.5.5"                 % Test
  )
  def apply(): Seq[ModuleID] = compile ++ test

}
