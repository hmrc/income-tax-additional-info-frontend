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

package test.support.stubs

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.authorisation.Enrolment.Agent
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.libs.json.{JsObject, Json}
import support.builders.UserBuilder.aUser
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel}


trait WireMockStubs {

  protected def stubGetWithHeadersCheck(url: String,
                                        status: Int,
                                        responseBody: String,
                                        sessionHeader: (String, String) = "X-Session-ID" -> aUser.sessionId,
                                        mtditidHeader: (String, String) = "mtditid" -> aUser.mtditid): StubMapping =
    stubFor(get(urlMatching(url))
      .withHeader(sessionHeader._1, equalTo(sessionHeader._2))
      .withHeader(mtditidHeader._1, equalTo(mtditidHeader._2))
      .willReturn(aResponse().withStatus(status).withBody(responseBody))
    )

  protected def authoriseAgentOrIndividual(isAgent: Boolean, nino: Boolean = true): StubMapping = if (isAgent) authoriseAgent() else authoriseIndividual(nino)

  protected def authoriseIndividualUnauthorized(): StubMapping = {
    stubPost(authoriseUri, UNAUTHORIZED, Json.prettyPrint(
      successfulAuthResponse(Some(AffinityGroup.Individual), ConfidenceLevel.L250, Seq(mtditEnrolment, ninoEnrolment): _*)
    ))
  }

  protected def authoriseIndividual(withNino: Boolean = true): StubMapping = {
    stubPost(authoriseUri, OK, Json.prettyPrint(successfulAuthResponse(Some(AffinityGroup.Individual), ConfidenceLevel.L250,
      enrolments = Seq(mtditEnrolment) ++ (if (withNino) Seq(ninoEnrolment) else Seq.empty[JsObject]): _*)))
  }

  protected def authoriseAgent(): StubMapping = {
    stubPost(authoriseUri, OK, Json.prettyPrint(
      successfulAuthResponse(Some(AffinityGroup.Agent), ConfidenceLevel.L250, Seq(asAgentEnrolment, mtditEnrolment): _*)
    ))
  }

  protected def authoriseAgentUnauthorized(): StubMapping = {
    stubPost(authoriseUri, UNAUTHORIZED, Json.prettyPrint(
      successfulAuthResponse(Some(AffinityGroup.Agent), ConfidenceLevel.L250, Seq(asAgentEnrolment, mtditEnrolment): _*)
    ))
  }

  protected def stubPost(url: String, status: Int, responseBody: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(post(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders.willReturn(aResponse().withStatus(status).withBody(responseBody)))
  }

  protected def stubPut(url: String, status: Int, responseBody: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(put(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }

    stubFor(mappingWithHeaders.willReturn(aResponse().withStatus(status).withBody(responseBody)))
  }

  def stubDeleteWithoutResponseBody(url: String, status: Int, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(delete(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }
    stubFor(mappingWithHeaders
      .willReturn(
        aResponse()
          .withStatus(status)))
  }

  def stubDeleteWithResponseBody(url: String, status: Int, response: String, requestHeaders: Seq[HttpHeader] = Seq.empty): StubMapping = {
    val mappingWithHeaders: MappingBuilder = requestHeaders.foldLeft(delete(urlMatching(url))) { (result, nxt) =>
      result.withHeader(nxt.key(), equalTo(nxt.firstValue()))
    }
    stubFor(mappingWithHeaders
      .willReturn(
        aResponse()
          .withStatus(status)
          .withBody(response)
      ))
  }

  private def successfulAuthResponse(affinityGroup: Option[AffinityGroup], confidenceLevel: ConfidenceLevel, enrolments: JsObject*): JsObject = {
    affinityGroup match {
      case Some(group) => Json.obj(
        "affinityGroup" -> group,
        "allEnrolments" -> enrolments,
        "confidenceLevel" -> confidenceLevel
      )
      case _ => Json.obj(
        "allEnrolments" -> enrolments,
        "confidenceLevel" -> confidenceLevel
      )
    }
  }


  private val authoriseUri = "/auth/authorise"

  private val mtditEnrolment = Json.obj(
    "key" -> "HMRC-MTD-IT",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "MTDITID",
        "value" -> "1234567890"
      )
    )
  )

  private val ninoEnrolment = Json.obj(
    "key" -> "HMRC-NI",
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> "NINO",
        "value" -> aUser.nino
      )
    )
  )

  private val asAgentEnrolment = Json.obj(
    "key" -> Agent.key,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> Agent.value,
        "value" -> "XARN1234567"
      )
    )
  )

  protected def createUserSessionDataStub(url: String,
                                          status: Int,
                                          responseBody: String,
                                          sessionHeader: (String, String) = "X-Session-ID" -> aUser.sessionId,
                                          mtditidHeader: (String, String) = "mtditid" -> aUser.mtditid
                                         ): StubMapping = {
    stubFor(post(urlMatching(url))
      .withHeader(sessionHeader._1, equalTo(sessionHeader._2))
      .withHeader(mtditidHeader._1, equalTo(mtditidHeader._2))
      .willReturn(aResponse().withStatus(status).withBody(responseBody))
    )
  }

  protected def updateUserSessionDataStub(url: String,
                                          status: Int,
                                          responseBody: String,
                                          sessionHeader: (String, String) = "X-Session-ID" -> aUser.sessionId,
                                          mtditidHeader: (String, String) = "mtditid" -> aUser.mtditid
                                         ): StubMapping = {
    stubFor(put(urlMatching(url))
      .withHeader(sessionHeader._1, equalTo(sessionHeader._2))
      .withHeader(mtditidHeader._1, equalTo(mtditidHeader._2))
      .willReturn(aResponse().withStatus(status).withBody(responseBody))
    )
  }

}
