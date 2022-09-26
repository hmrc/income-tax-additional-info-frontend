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

package actions

import akka.actor.ActorSystem
import config.AppConfig
import models.authorisation.SessionValues.{CLIENT_MTDITID, CLIENT_NINO}
import models.authorisation.{AgentEnrolment, IndividualEnrolment, NinoEnrolment}
import models.requests.AuthorisationRequest
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.Results.Ok
import play.api.mvc._
import play.api.test.Helpers.status
import play.api.test.{FakeRequest, Helpers}
import services.AuthorisationService
import support.UnitTest
import support.builders.models.UserBuilder.aUser
import support.helpers.FakeRequestHelper
import support.stubs.AppConfigStub
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedActionSpec extends UnitTest
  with FakeRequestHelper
  with MockFactory {

  private implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(aUser.sessionId)))
  private implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()
  private implicit val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private implicit val authorisationService: AuthorisationService = new AuthorisationService(mockAuthConnector)
  private val appConfig: AppConfig = new AppConfigStub().config()
  private val cc: ControllerComponents = Helpers.stubControllerComponents()
  private val executionContext = ExecutionContext.global
  implicit val actorSystem: ActorSystem = ActorSystem()
  private val fakeRequestWithMtditidAndNino: FakeRequest[AnyContentAsEmpty.type] = fakeAgentRequest
    .withHeaders(newHeaders = "X-Session-ID" -> aUser.sessionId)
    .withSession(CLIENT_MTDITID -> "1234567890", CLIENT_NINO -> "AA123456A")

  private def mockAuthAsAgent(): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    val enrolments: Enrolments = Enrolments(Set(
      Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, "1234567890")), "Activated"),
      Enrolment(AgentEnrolment.key, Seq(EnrolmentIdentifier(AgentEnrolment.value, "0987654321")), "Activated")
    ))
    val agentRetrievals: Some[AffinityGroup] = Some(AffinityGroup.Agent)

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(agentRetrievals))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments, *, *)
      .returning(Future.successful(enrolments))
  }

  private def mockAuth(nino: Option[String]): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    val enrolments = Enrolments(Set(
      Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, "1234567890")), "Activated"),
      Enrolment(AgentEnrolment.key, Seq(EnrolmentIdentifier(AgentEnrolment.value, "0987654321")), "Activated")
    ) ++ nino.fold(Seq.empty[Enrolment])(unwrappedNino =>
      Seq(Enrolment(NinoEnrolment.key, Seq(EnrolmentIdentifier(NinoEnrolment.value, unwrappedNino)), "Activated"))
    ))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Individual)))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
      .returning(Future.successful(enrolments and ConfidenceLevel.L200))
  }


  private val underTest = new AuthorisedAction(authorisationService, appConfig, cc: ControllerComponents)(executionContext)

  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".parser" should {
    "return default parser from the ControllerComponents" in {
      underTest.parser shouldBe a[BodyParser[AnyContent]]
    }
  }

  ".enrolmentGetIdentifierValue" should {
    "return the value for the given identifier" in {
      val returnValue = "anIdentifierValue"
      val returnValueAgent = "anAgentIdentifierValue"
      val enrolments = Enrolments(Set(
        Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, returnValue)), "Activated"),
        Enrolment(AgentEnrolment.key, Seq(EnrolmentIdentifier(AgentEnrolment.value, returnValueAgent)), "Activated")
      ))

      underTest.enrolmentGetIdentifierValue(IndividualEnrolment.key, IndividualEnrolment.value, enrolments) shouldBe Some(returnValue)
      underTest.enrolmentGetIdentifierValue(AgentEnrolment.key, AgentEnrolment.value, enrolments) shouldBe Some(returnValueAgent)
    }

    "return a None" when {
      val key = "someKey"
      val identifierKey = "anIdentifier"
      val returnValue = "anIdentifierValue"
      val enrolments = Enrolments(Set(Enrolment(key, Seq(EnrolmentIdentifier(identifierKey, returnValue)), "someState")))

      "the given identifier cannot be found" in {
        underTest.enrolmentGetIdentifierValue(key, "someOtherIdentifier", enrolments) shouldBe None
      }

      "the given key cannot be found" in {
        underTest.enrolmentGetIdentifierValue("someOtherKey", identifierKey, enrolments) shouldBe None
      }
    }
  }

  ".individualAuthentication" should {
    "perform the block action" when {
      "the correct enrolment exist" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set(
          Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, aUser.mtditid)), "Activated"),
          Enrolment(NinoEnrolment.key, Seq(EnrolmentIdentifier(NinoEnrolment.value, aUser.nino)), "Activated")
        ))
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L200))
          await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))
        }

        "returns an OK status" in {
          result.header.status shouldBe OK
        }

        "returns a body of the mtditid" in {
          await(result.body.consumeData.map(_.utf8String)) shouldBe aUser.mtditid
        }
      }
    }

    "return a redirect" when {
      "the session id does not exist in the headers" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set(
          Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, aUser.mtditid)), "Activated"),
          Enrolment(NinoEnrolment.key, Seq(EnrolmentIdentifier(NinoEnrolment.value, aUser.nino)), "Activated")
        ))

        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L200))
          await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest.withHeaders(), emptyHeaderCarrier))
        }

        "returns an SEE_OTHER status" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "the nino enrolment is missing" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set())
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L200))
          await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))
        }

        "returns a forbidden" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "the individual enrolment is missing but there is a nino" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set(Enrolment(NinoEnrolment.key, Seq(EnrolmentIdentifier(NinoEnrolment.value, aUser.nino)), "Activated")))
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L200))
          await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))
        }

        "returns an Unauthorised" in {
          result.header.status shouldBe SEE_OTHER
        }

        "returns a redirect to the correct page" in {
          result.header.headers.getOrElse("Location", "/") shouldBe controllers.errors.routes.IndividualAuthErrorController.show.url
        }
      }
    }

    "return the user to IV Uplift" when {
      "the confidence level is below minimum" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val mtditid = "1234567890"
        val enrolments = Enrolments(Set(
          Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, mtditid)), "Activated"),
          Enrolment(NinoEnrolment.key, Seq(EnrolmentIdentifier(NinoEnrolment.value, "AA123456A")), "Activated")
        ))
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, allEnrolments and confidenceLevel, *, *)
            .returning(Future.successful(enrolments and ConfidenceLevel.L50))
          await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))
        }

        "has a status of 303" in {
          result.header.status shouldBe SEE_OTHER
        }

        "redirects to the iv url" in {
          result.header.headers("Location") shouldBe "/update-and-submit-income-tax-return/iv-uplift"
        }
      }
    }
  }

  ".agentAuthenticated" should {
    val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(s"${request.user.mtditid} ${request.user.arn.get}"))

    "perform the block action" when {
      "the agent is authorised for the given user" which {
        val enrolments = Enrolments(Set(
          Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, "1234567890")), "Activated"),
          Enrolment(AgentEnrolment.key, Seq(EnrolmentIdentifier(AgentEnrolment.value, "0987654321")), "Activated")
        ))
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.successful(enrolments))
          await(underTest.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
        }

        "has a status of OK" in {
          result.header.status shouldBe OK
        }

        "has the correct body" in {
          await(result.body.consumeData.map(_.utf8String)) shouldBe "1234567890 0987654321"
        }
      }
    }

    "return an SEE_OTHER" when {
      "the agent does not have a session id" which {
        val enrolments = Enrolments(Set(
          Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, "1234567890")), "Activated"),
          Enrolment(AgentEnrolment.key, Seq(EnrolmentIdentifier(AgentEnrolment.value, "0987654321")), "Activated")
        ))
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.successful(enrolments))
          await(underTest.agentAuthentication(block)(fakeRequestWithMtditidAndNino, emptyHeaderCarrier))
        }

        "has a status of SEE_OTHER" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(AuthException))
          await(underTest.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
        }
        result.header.status shouldBe SEE_OTHER
      }
    }

    "redirect to the sign in page" when {
      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(NoActiveSession))
          await(underTest.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
        }

        result.header.status shouldBe SEE_OTHER
      }
    }

    "return a redirect" when {
      "the user does not have an enrolment for the agent" in {
        val enrolments = Enrolments(Set(
          Enrolment(IndividualEnrolment.key, Seq(EnrolmentIdentifier(IndividualEnrolment.value, "1234567890")), "Activated")
        ))
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.successful(enrolments))
          await(underTest.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
        }

        result.header.status shouldBe SEE_OTHER
      }
    }
  }

  ".invokeBlock" should {
    lazy val block: AuthorisationRequest[AnyContent] => Future[Result] = request =>
      Future.successful(Ok(s"mtditid: ${request.user.mtditid}${request.user.arn.fold("")(arn => " arn: " + arn)}"))

    "perform the block action" when {
      "the user is successfully verified as an agent" which {
        lazy val result = {
          mockAuthAsAgent()
          await(underTest.invokeBlock(fakeRequestWithMtditidAndNino, block))
        }

        "should return an OK(200) status" in {
          result.header.status shouldBe OK
          await(result.body.consumeData.map(_.utf8String)) shouldBe "mtditid: 1234567890 arn: 0987654321"
        }
      }

      "the user is successfully verified as an individual" in {
        lazy val result = {
          mockAuth(Some("AA123456A"))
          await(underTest.invokeBlock(fakeIndividualRequest, block))
        }

        result.header.status shouldBe OK
        await(result.body.consumeData.map(_.utf8String)) shouldBe "mtditid: 1234567890"
      }
    }

    "return a redirect" when {
      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(AuthException))

          underTest.invokeBlock(fakeAgentRequest, block)
        }

        status(result) shouldBe SEE_OTHER
      }

      "there is no MTDITID value in session" in {
        val fakeRequestWithNino = fakeIndividualRequest.withSession(CLIENT_NINO -> "AA123456A")
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, Retrievals.affinityGroup, *, *)
            .returning(Future.successful(Some(AffinityGroup.Agent)))

          underTest.invokeBlock(fakeRequestWithNino, block)
        }

        status(result) shouldBe SEE_OTHER
        await(result).header.headers.getOrElse("Location", "/") shouldBe "/report-quarterly/income-and-expenses/view/agents/client-utr"
      }
    }

    "redirect to the sign in page" when {
      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(NoActiveSession))
          underTest.invokeBlock(fakeIndividualRequest, block)
        }

        status(result) shouldBe SEE_OTHER
      }
    }
  }
}
