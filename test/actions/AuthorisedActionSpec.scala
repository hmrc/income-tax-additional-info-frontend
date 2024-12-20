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

package actions

import config.AppConfig
import org.apache.pekko.actor.ActorSystem
import models.authorisation.Enrolment.{Agent, Individual, Nino}
import models.authorisation.SessionValues.{CLIENT_MTDITID, CLIENT_NINO}
import models.requests.AuthorisationRequest
import org.scalamock.scalatest.MockFactory
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SEE_OTHER, UNAUTHORIZED}
import play.api.mvc.Results.{InternalServerError, Ok}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import support.ControllerUnitTest
import support.builders.UserBuilder.{aUser, anAgentUser}
import support.mocks.{MockAuthorisationService, MockErrorHandler}
import support.providers.FakeRequestProvider
import support.stubs.AppConfigStub
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.{ExecutionContext, Future}

class AuthorisedActionSpec extends ControllerUnitTest
  with FakeRequestProvider
  with MockAuthorisationService
  with MockErrorHandler
  with MockFactory {

  private implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(aUser.sessionId)))
  private val executionContext = ExecutionContext.global
  implicit val actorSystem: ActorSystem = ActorSystem()

  private val fakeRequestWithMtditidAndNino: FakeRequest[AnyContentAsEmpty.type] = fakeAgentRequest
    .withHeaders(newHeaders = "X-Session-ID" -> aUser.sessionId)
    .withSession(CLIENT_MTDITID -> "1234567890", CLIENT_NINO -> "AA123456A")

  private val enrolments = Enrolments(Set(
    Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, aUser.mtditid)), "Activated"),
    Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, aUser.nino)), "Activated")
  ))

  private val agentEnrolment = Enrolments(Set(
    Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, aUser.mtditid)), "Activated"),
    Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, anAgentUser.arn.get)), "Activated")
  ))

  private val secondaryAgentEnrolments = Enrolments(Set(
    Enrolment("HMRC-MTD-IT-SUPP", Seq(EnrolmentIdentifier("MTDITID", aUser.mtditid)), "Activated"),
    Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, anAgentUser.arn.get)), "Activated")
  ))
  private val underTest = new AuthorisedAction(authorisationService, appConfig, mcc: ControllerComponents, mockErrorHandler)(executionContext)

  private def configWithSupportingAgentsEnabled(): AppConfig = new AppConfigStub().featureSwitchConfigs(("emaSupportingAgentsEnabled"-> true))

  private def configWithSupportingAgentsDisabled(): AppConfig = new AppConfigStub().featureSwitchConfigs(("emaSupportingAgentsEnabled"-> false))

  private val underTestEnabled = new AuthorisedAction(authorisationService, configWithSupportingAgentsEnabled(), mcc: ControllerComponents, mockErrorHandler)(executionContext)
  private val underTestDisabled = new AuthorisedAction(authorisationService, configWithSupportingAgentsDisabled(), mcc: ControllerComponents, mockErrorHandler)(executionContext)


  ".executionContext" should {
    "return the given execution context" in {
      underTest.executionContext shouldBe executionContext
    }
  }

  ".parser" should {
    "return default parser from the ControllerComponents" in {
      underTest.parser shouldBe a[BodyParser[_]]
    }
  }

  ".enrolmentGetIdentifierValue" should {
    "return the value for the given identifier" in {
      val returnValue = "anIdentifierValue"
      val returnValueAgent = "anAgentIdentifierValue"
      val enrolments = Enrolments(Set(
        Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, returnValue)), "Activated"),
        Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, returnValueAgent)), "Activated")
      ))

      underTest.enrolmentGetIdentifierValue(Individual.key, Individual.value, enrolments) shouldBe Some(returnValue)
      underTest.enrolmentGetIdentifierValue(Agent.key, Agent.value, enrolments) shouldBe Some(returnValueAgent)
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

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))

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

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest.withHeaders(), HeaderCarrier()))

        "returns an SEE_OTHER status" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "the nino enrolment is missing" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set())

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))

        "returns a forbidden" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "the individual enrolment is missing but there is a nino" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set(Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, aUser.nino)), "Activated")))

        lazy val result = {
          mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)
          await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))
        }

        "returns an Unauthorised" in {
          result.header.status shouldBe SEE_OTHER
        }

        "returns a redirect to the correct page" in {
          result.header.headers.getOrElse("Location", "/") shouldBe controllers.errors.routes.IndividualAuthErrorController.show().url
        }
      }
    }

    "return the user to IV Uplift" when {
      "the confidence level is below minimum" which {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L50)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))

        "has a status of 303" in {
          result.header.status shouldBe SEE_OTHER
        }

        "redirects to the iv url" in {
          result.header.headers("Location") shouldBe "/update-and-submit-income-tax-return/iv-uplift"
        }
      }
    }
  }

  ".agentAuthenticated as secondary agent" should {
    val block: AuthorisationRequest[AnyContent] => Future[Result] =
      request => Future.successful(Ok(s"${request.user.mtditid} ${request.user.arn.getOrElse("No ARN")}"))

    "fallback to secondary agent if primary fails" which {
      lazy val result = {
        mockAuthorisePredicates(underTestEnabled.primaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Primary failed")))

        mockAuthorisePredicates(underTestEnabled.secondaryAgentPredicate(aUser.mtditid), Future.successful(secondaryAgentEnrolments))

        await(underTestEnabled.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
      }

      "has a status of OK" in {
        result.header.status shouldBe OK
      }

      "has the correct body for limited access" in {
        await(result.body.consumeData.map(_.utf8String)) shouldBe s"${aUser.mtditid} 0987654321"
      }

      "not fallback to secondary agent if primary fails when supporting agents are disabled" which {
        lazy val result = {

          mockAuthorisePredicates(underTestEnabled.primaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Primary failed")))

          mockAuthorisePredicates(underTestEnabled.secondaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Secondary failed")))


          await(underTestEnabled.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
        }

        "has a status of SEE_OTHER" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "return error if both primary and secondary fails when supporting agents are enabled" which {
        lazy val result = {

          mockAuthorisePredicates(underTestDisabled.primaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Primary failed")))

          await(underTestDisabled.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
        }

        "has a status of SEE_OTHER" in {
          result.header.status shouldBe SEE_OTHER
        }
      }
    }
  }

  ".agentAuthenticated" should {
    val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(s"${request.user.mtditid} ${request.user.arn.get}"))

    "perform the block action" when {
      "the agent is authorised for the given user" which {
        lazy val result = {
          mockAuthorise(Retrievals.allEnrolments, agentEnrolment)
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

    "render ISE page" when {
      "Agent authentication fails with a non-Auth related exception (Primary Agent)" in {

        mockAuthorisePredicates(underTestEnabled.primaryAgentPredicate(aUser.mtditid), Future.failed(new Exception("bang")))
        mockInternalServerError(InternalServerError)

        val result = underTestEnabled.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "Agent authentication fails with a non-Auth related exception (Secondary Agent)" in {

        mockAuthorisePredicates(underTestEnabled.primaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Primary failed")))
        mockAuthorisePredicates(underTestEnabled.secondaryAgentPredicate(aUser.mtditid), Future.failed(new Exception("bang")))
        mockInternalServerError(InternalServerError)

        val result = underTestEnabled.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "return an SEE_OTHER" when {
      "the agent does not have a session id" which {
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.successful(agentEnrolment))
          await(underTest.agentAuthentication(block)(fakeRequestWithMtditidAndNino, HeaderCarrier()))
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
            .returning(Future.failed(AuthException)).anyNumberOfTimes()
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
          Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, "1234567890")), "Activated")
        ))

        mockAuthorise(Retrievals.allEnrolments, enrolments)

        lazy val result = await(underTest.agentAuthentication(block)(fakeRequestWithMtditidAndNino, headerCarrierWithSession))

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
