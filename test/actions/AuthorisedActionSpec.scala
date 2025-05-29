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
import models.session.UserSessionData
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.http.Status.{IM_A_TEAPOT, INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.mvc.Results.{ImATeapot, InternalServerError, Ok}
import play.api.mvc._
import play.api.test.{DefaultAwaitTimeout, FutureAwaits, Helpers}
import play.api.test.Helpers._
import services.{AuthorisationService, MissingAgentClientDetails, SessionDetailsService}
import support.builders.UserBuilder.{aUser, anAgentUser}
import support.mocks.MockErrorHandler
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
import scala.concurrent.ExecutionContext.Implicits.global

class AuthorisedActionSpec
  extends AnyFreeSpec
    with Matchers
    with FutureAwaits
    with DefaultAwaitTimeout
    with OptionValues
    with FakeRequestProvider
    with MockFactory
    with MockErrorHandler {

  protected val mockAuthConnector: AuthConnector = mock[AuthConnector]

  def mockAuthorise[A](retrieval: Retrieval[A], result: A): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, retrieval, *, *)
      .returning(Future.successful(result))
  }

  def mockAuthorisePredicates[A](predicate: Predicate,
                                 returningResult: Future[A]): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(predicate, *, *, *)
      .returning(returningResult)
  }

  protected val authorisationService: AuthorisationService = new AuthorisationService(mockAuthConnector)

  def mockAuthAsAgent(): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] = {
    val agentRetrievals: Some[AffinityGroup] = Some(AffinityGroup.Agent)
    val enrolments: Enrolments = Enrolments(Set(
      Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, aUser.mtditid)), "Activated"),
      Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, anAgentUser.arn.get)), "Activated")
    ))

    mockAuthorise(Retrievals.affinityGroup, agentRetrievals)
    mockAuthorise(Retrievals.allEnrolments, enrolments)
  }

  case class Harness(authorisedAction: AuthorisedAction) {
    def run: Action[AnyContent] = authorisedAction {
      _ => ImATeapot("Result from block")
    }
  }

  val appConfig: AppConfig = new AppConfigStub().config()
  val mcc: MessagesControllerComponents = Helpers.stubMessagesControllerComponents()

  private implicit val headerCarrierWithSession: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(aUser.sessionId)))
  implicit val actorSystem: ActorSystem = ActorSystem()

  private val fakeRequestWithMtditidAndNino = fakeAgentRequest
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

  private val userSessionData = UserSessionData(sessionId = aUser.sessionId, mtditid = aUser.mtditid, nino = aUser.nino, utr = Some("123456"))

  ".enrolmentGetIdentifierValue" - {
    val underTest = new AuthorisedActionImpl(
      authorisationService,
      PassingSessionDetailsService(userSessionData),
      appConfig,
      mcc,
      mockErrorHandler
    )

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

    "return a None" - {
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

  ".individualAuthentication" - {
    val underTest = new AuthorisedActionImpl(
      authorisationService,
      PassingSessionDetailsService(userSessionData),
      appConfig,
      mcc,
      mockErrorHandler
    )

    "perform the block action" - {
      "the correct enrolment exist" - {
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

    "return a redirect" - {
      "the session id does not exist in the headers" - {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest.withHeaders(), HeaderCarrier()))

        "returns an SEE_OTHER status" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "the nino enrolment is missing" - {
        val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set())

        mockAuthorise(allEnrolments and confidenceLevel, enrolments and ConfidenceLevel.L250)

        val result = await(underTest.individualAuthentication[AnyContent](block, AffinityGroup.Individual)(fakeIndividualRequest, headerCarrierWithSession))

        "returns a forbidden" in {
          result.header.status shouldBe SEE_OTHER
        }
      }

      "the individual enrolment is missing but there is a nino" - {
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

    "return the user to IV Uplift" - {
      "the confidence level is below minimum" - {
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

  ".agentAuthenticated as secondary agent" - {
    val underTest = new AuthorisedActionImpl(
      authorisationService,
      PassingSessionDetailsService(userSessionData),
      appConfig,
      mcc,
      mockErrorHandler
    )

    val block: AuthorisationRequest[AnyContent] => Future[Result] =
      request => Future.successful(Ok(s"${request.user.mtditid} ${request.user.arn.getOrElse("No ARN")}"))

    "redirects user to unauthorized page" in {
      mockAuthorisePredicates(underTest.primaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Primary failed")))
      mockAuthorisePredicates(underTest.secondaryAgentPredicate(aUser.mtditid), Future.successful(secondaryAgentEnrolments))

      val result = underTest.agentAuthentication(block, aUser.sessionId)(fakeRequestWithMtditidAndNino, headerCarrierWithSession)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value should endWith("/error/supporting-agent-not-authorised")
    }

    "return error if both primary and secondary fails" in {
      mockAuthorisePredicates(underTest.primaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Primary failed")))
      mockAuthorisePredicates(underTest.secondaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Secondary failed")))

      val result = underTest.agentAuthentication(block, aUser.sessionId)(fakeRequestWithMtditidAndNino, headerCarrierWithSession)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value should endWith("/error/you-need-client-authorisation")
    }
  }

  ".agentAuthenticated" - {
    val underTest = new AuthorisedActionImpl(
      authorisationService,
      PassingSessionDetailsService(userSessionData),
      appConfig,
      mcc,
      mockErrorHandler
    )

    val block: AuthorisationRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(s"${request.user.mtditid} ${request.user.arn.get}"))

    "perform the block action" - {
      "the agent is authorised for the given user" in {
        mockAuthorise(Retrievals.allEnrolments, agentEnrolment)
        val result = underTest.agentAuthentication(block, aUser.sessionId)(fakeRequestWithMtditidAndNino, headerCarrierWithSession)

        status(result) shouldBe OK
        contentAsString(result) shouldBe "1234567890 0987654321"
      }
    }

    "render ISE page" - {
      "Agent authentication fails with a non-Auth related exception (Primary Agent)" in {

        mockAuthorisePredicates(underTest.primaryAgentPredicate(aUser.mtditid), Future.failed(new Exception("bang")))
        mockInternalServerError(InternalServerError)

        val result = underTest.agentAuthentication(block, "???")(fakeRequestWithMtditidAndNino, headerCarrierWithSession)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "Agent authentication fails with a non-Auth related exception (Secondary Agent)" in {

        mockAuthorisePredicates(underTest.primaryAgentPredicate(aUser.mtditid), Future.failed(InsufficientEnrolments("Primary failed")))
        mockAuthorisePredicates(underTest.secondaryAgentPredicate(aUser.mtditid), Future.failed(new Exception("bang")))
        mockInternalServerError(InternalServerError)

        val result = underTest.agentAuthentication(block, "???")(fakeRequestWithMtditidAndNino, headerCarrierWithSession)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "return an SEE_OTHER" - {
      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")
        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(AuthException)).anyNumberOfTimes()
          await(underTest.agentAuthentication(block, "???")(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
        }
        result.header.status shouldBe SEE_OTHER
      }
    }

    "redirect to the sign in page" - {
      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
            .expects(*, *, *, *)
            .returning(Future.failed(NoActiveSession))
          await(underTest.agentAuthentication(block, "???")(fakeRequestWithMtditidAndNino, headerCarrierWithSession))
        }

        result.header.status shouldBe SEE_OTHER
      }
    }

    "return a redirect" - {
      "the user does not have an enrolment for the agent" in {
        val enrolments = Enrolments(Set(
          Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, "1234567890")), "Activated")
        ))

        mockAuthorise(Retrievals.allEnrolments, enrolments)

        lazy val result = await(underTest.agentAuthentication(block, "???")(fakeRequestWithMtditidAndNino, headerCarrierWithSession))

        result.header.status shouldBe SEE_OTHER
      }
    }
  }

  ".invokeBlock" - {
    "should return response from the block" - {
      "when the user is an agent" - {
        "when there is session data" in {
          val underTest = Harness(new AuthorisedActionImpl(
            authorisationService,
            PassingSessionDetailsService(userSessionData),
            appConfig,
            mcc,
            mockErrorHandler
          ))

          mockAuthAsAgent()
          val result = underTest.run(fakeRequestWithMtditidAndNino)

          status(result) shouldBe IM_A_TEAPOT
          contentAsString(result) shouldBe "Result from block"
        }
      }

      "when they are successfully verified as an individual" in {
        val underTest = Harness(new AuthorisedActionImpl(
          authorisationService,
          NoOpSessionDetailsService,
          appConfig,
          mcc,
          mockErrorHandler
        ))

        val enrolments = Enrolments(Set(
          Enrolment(Individual.key, Seq(EnrolmentIdentifier(Individual.value, "1234567890")), "Activated"),
          Enrolment(Agent.key, Seq(EnrolmentIdentifier(Agent.value, "0987654321")), "Activated"),
          Enrolment(Nino.key, Seq(EnrolmentIdentifier(Nino.value, "AA123456A")), "Activated")
        ))

        mockAuthorise(Retrievals.affinityGroup, Some(AffinityGroup.Individual))
        mockAuthorise(Retrievals.allEnrolments and Retrievals.confidenceLevel, enrolments and ConfidenceLevel.L250)

        val result = underTest.run(fakeIndividualRequest)

        status(result) shouldBe IM_A_TEAPOT
        contentAsString(result) shouldBe "Result from block"
      }
    }

    "when the user is an agent and session data is missing" in {
      val underTest = Harness(new AuthorisedActionImpl(
        authorisationService,
        MissingSessionDetailsService,
        appConfig,
        mcc,
        mockErrorHandler
      ))

      mockAuthorise(Retrievals.affinityGroup, Some(AffinityGroup.Agent))
      val result = underTest.run(fakeRequestWithMtditidAndNino)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result).value shouldBe appConfig.viewAndChangeEnterUtrUrl
    }

    "the user is an agent and fails to authorisation checks" - {

      "redirect when the authorisation service returns an AuthorisationException exception" in {
        val underTest = Harness(new AuthorisedActionImpl(
          authorisationService,
          PassingSessionDetailsService(userSessionData),
          appConfig,
          mcc,
          mockErrorHandler
        ))

        object AuthException extends AuthorisationException("Some reason")
        (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *, *)
          .returning(Future.failed(AuthException))

        val result = underTest.run(fakeAgentRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).value should endWith("/error/not-authorised-to-use-service")
      }
    }

    "redirect to the sign in page" - {
      "the authorisation service returns a NoActiveSession exception" in {
        val underTest = Harness(new AuthorisedActionImpl(
          authorisationService,
          NoOpSessionDetailsService,
          appConfig,
          mcc,
          mockErrorHandler
        ))

        object NoActiveSession extends NoActiveSession("Some reason")
        (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *, *)
          .returning(Future.failed(NoActiveSession))

        val result = underTest.run(fakeIndividualRequest)

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).value shouldBe "/sign-in-url"
      }
    }

    "return a status 500" - {
      "an unexpected exception is caught that is not related to Authorisation" in {
        val underTest = Harness(new AuthorisedActionImpl(
          authorisationService,
          NoOpSessionDetailsService,
          appConfig,
          mcc,
          mockErrorHandler
        ))

        (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
          .expects(*, *, *, *)
          .returning(Future.failed(new Exception("bang")))

        mockInternalServerError(InternalServerError("An unexpected error occurred"))

        val result = underTest.run(fakeAgentRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}

case class PassingSessionDetailsService(userSessionData: UserSessionData) extends SessionDetailsService {
  override def getSessionData[A](sessionId: String)(implicit request: Request[A], hc: HeaderCarrier): Future[UserSessionData] =
    Future.successful(userSessionData)
}

object MissingSessionDetailsService extends SessionDetailsService {
  override def getSessionData[A](sessionId: String)(implicit request: Request[A], hc: HeaderCarrier): Future[UserSessionData] =
    Future.failed(MissingAgentClientDetails("Session Data service and Session Cookie both returned empty data"))
}

object NoOpSessionDetailsService extends SessionDetailsService {
  override def getSessionData[A](sessionId: String)(implicit request: Request[A], hc: HeaderCarrier): Future[UserSessionData] =
    ???
}
