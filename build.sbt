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

import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "income-tax-additional-info-frontend"

val silencerVersion = "1.7.9"

lazy val coverageSettings: Seq[Setting[_]] = {
    import scoverage.ScoverageKeys

    val excludedPackages = Seq(
        "<empty>",
        ".*Reverse.*",
        ".*standardError*.*",
        ".*govuk_wrapper*.*",
        ".*main_template*.*",
        "uk.gov.hmrc.BuildInfo",
        "app.*",
        "prod.*",
        "config.*",
        "testOnly.*",
        "testOnlyDoNotUseInAppConf.*",
        ".*feedback*.*",
        "partials.*",
        "controllers.testOnly.*",
        "forms.validation.mappings",
        "views.html.*[Tt]emplate.*",
        "views.html.views.templates.helpers*",
        "views.html.views.templates.inputs*",
        "views.headerFooterTemplate"
    )

    Seq(
        ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
        ScoverageKeys.coverageMinimumStmtTotal := 95,
        ScoverageKeys.coverageFailOnMinimum := true,
        ScoverageKeys.coverageHighlighting := true
    )
}

lazy val twirlImports: Seq[String] = Seq(
    "config.AppConfig",
    "uk.gov.hmrc.govukfrontend.views.html.components._",
    "uk.gov.hmrc.hmrcfrontend.views.html.components._",
    "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(PlayKeys.playDefaultPort := 9376)
  .settings(
      majorVersion := 0,
      scalaVersion := "2.12.13",
      libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
      TwirlKeys.templateImports ++= twirlImports,
      Assets / pipelineStages := Seq(gzip),
      // ***************
      // Use the silencer plugin to suppress warnings
      // You may turn it on for `views` too to suppress warnings from unused imports in compiled twirl templates, but this will hide other warnings.
      scalacOptions += "-P:silencer:pathFilters=routes",
      scalacOptions += "-P:silencer:lineContentFilters=^\\w",
      libraryDependencies ++= Seq(
          compilerPlugin(dependency = "com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
          "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
      )
      // ***************
  )
  .settings(Test / fork := false)
  .settings(publishingSettings: _*)
  .configs(IntegrationTest extend Test)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(IntegrationTest / unmanagedResourceDirectories += baseDirectory.value / "it" / "resources")
  .settings(coverageSettings: _*)
  .settings(
      // concatenate js
      Concat.groups := Seq("javascripts/application.js" -> group(Seq(
          "lib/govuk-frontend/govuk/all.js",
          "javascripts/jquery.min.js",
          "javascripts/app.js",
      ))),
      // prevent removal of unused code which generates warning errors due to use of third-party libs
      uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
      pipelineStages := Seq(digest),
      // below line required to force asset pipeline to operate in dev rather than only prod
      Assets / pipelineStages := Seq(concat, uglify),
      // only compress files generated by concat
      uglify / includeFilter := GlobFilter("application.js")
  )
