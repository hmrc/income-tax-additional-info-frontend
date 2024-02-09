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

resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

// To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("uk.gov.hmrc"          % "sbt-auto-build"         % "3.20.0")
addSbtPlugin("uk.gov.hmrc"          % "sbt-distributables"     % "2.5.0")
addSbtPlugin("com.typesafe.play"    % "sbt-plugin"             % "2.8.21")
addSbtPlugin("org.scoverage"        % "sbt-scoverage"          % "2.0.9")
addSbtPlugin("com.typesafe.sbt"     % "sbt-gzip"               % "1.0.2")
addSbtPlugin("io.github.irundaia"   % "sbt-sassify"            % "1.5.2")
addSbtPlugin("org.scalastyle"       %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("net.ground5hark.sbt"  % "sbt-concat"             % "0.2.0")
addSbtPlugin("com.typesafe.sbt"     % "sbt-uglify"             % "2.0.0")
addSbtPlugin("com.typesafe.sbt"     % "sbt-digest"             % "1.1.4")