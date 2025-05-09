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

import config.{AppConfig, ErrorHandler}
import controllers.errors.routes.{AgentAuthErrorController, IndividualAuthErrorController, UnauthorisedUserErrorController, YouNeedAgentServicesController}
import models.authorisation.Enrolment.{Agent, Individual, Nino}
import models.authorisation.SessionValues
import models.requests.AuthorisationRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import services.AuthorisationService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject()(authService: AuthorisationService,
                                 appConfig: AppConfig,
                                 cc: ControllerComponents,
                                 errorHandler: ErrorHandler)
                                (implicit ec: ExecutionContext)
  extends ActionBuilder[AuthorisationRequest, AnyContent] with HeaderCarrierHelper with Logging {

  private val minimumConfidenceLevel: Int = ConfidenceLevel.L250.level

  private val agentAuthLogString: String = "[AuthorisedAction][agentAuthentication]"
  private val individualAuthLogString: String = "[AuthorisedAction][individualAuthentication]"

  override protected[actions] def executionContext: ExecutionContext = ec

  override def parser: BodyParser[AnyContent] = cc.parsers.default

  override def invokeBlock[A](request: Request[A], block: AuthorisationRequest[A] => Future[Result]): Future[Result] = {

    implicit lazy val headerCarrier: HeaderCarrier = hcWithCorrelationId(request)

    authService.authorised().retrieve(affinityGroup) {
      case Some(AffinityGroup.Agent) => agentAuthentication(block)(request, headerCarrier)
      case Some(affinityGroup) => individualAuthentication(block, affinityGroup)(request, headerCarrier)
      case _ => Future.successful(redirectToUnauthorisedUserErrorPage())
    } recover {
      case _: NoActiveSession => redirectToSignInPage()
      case _: AuthorisationException => redirectToUnauthorisedUserErrorPage()
      case e => logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
        errorHandler.internalServerError()(request)
    }
  }

  private[actions] def individualAuthentication[A](block: AuthorisationRequest[A] => Future[Result], affinityGroup: AffinityGroup)
                                                  (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    authService.authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>
        handleEnrolmentValidation(enrolments, affinityGroup, request, block)
      case _ =>
        logAndRedirectFromUrl(
          s"$individualAuthLogString User has confidence level below 250, routing user to IV uplift.",
          appConfig.incomeTaxSubmissionIvRedirect)
    }
  }

  private def handleEnrolmentValidation[A](enrolments: Enrolments, affinityGroup: AffinityGroup,
                                           request: Request[A], block: AuthorisationRequest[A] => Future[Result])
                                          (implicit hc: HeaderCarrier): Future[Result] = {
    val optionalMtdItId: Option[String] = enrolmentGetIdentifierValue(Individual.key, Individual.value, enrolments)
    val optionalNino: Option[String] = enrolmentGetIdentifierValue(Nino.key, Nino.value, enrolments)

    (optionalMtdItId, optionalNino) match {
      case (Some(mtdItId), Some(nino)) =>
        createAuthorisationRequest(mtdItId, None, nino, affinityGroup, request, block)
      case (_, None) =>
        logAndRedirectFromUrl(
          s"$individualAuthLogString - No active session. Redirecting to sign-in URL.",
          appConfig.signInUrl)
      case (None, _) =>
        logAndRedirect(
          s"$individualAuthLogString - User has no MTD IT enrolment. Redirecting user to sign up for MTD.",
          IndividualAuthErrorController.show())
    }
  }

  private[actions] def primaryAgentPredicate(mtdId: String) =
    Enrolment("HMRC-MTD-IT")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth")

  private[actions] def secondaryAgentPredicate(mtdId: String): Predicate =
    Enrolment("HMRC-MTD-IT-SUPP")
      .withIdentifier("MTDITID", mtdId)
      .withDelegatedAuthRule("mtd-it-auth-supp")

  private[actions] def agentAuthentication[A](block: AuthorisationRequest[A] => Future[Result])
                                             (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    val optionalNino = request.session.get(SessionValues.CLIENT_NINO)
    val optionalMtdItId = request.session.get(SessionValues.CLIENT_MTDITID)

    (optionalMtdItId, optionalNino) match {
      case (Some(mtdItId), Some(nino)) =>
        authService.authorised(primaryAgentPredicate(mtdItId))
          .retrieve(allEnrolments) { enrolments =>
            populateAgent(block, request, hc, mtdItId, nino, enrolments, isSupportingAgent = false)
          }.recoverWith {
            agentRecovery(block, request, mtdItId, nino)
          }

      case (mtditid, nino) =>
        logAndRedirectFromUrl(
          s"$agentAuthLogString - Agent does not have session key values. " +
            s"Redirecting to view & change. MTDITID missing:${mtditid.isEmpty}, NINO missing:${nino.isEmpty}",
          appConfig.viewAndChangeEnterUtrUrl)
    }
  }

  private def agentRecovery[A](block: AuthorisationRequest[A] => Future[Result], request: Request[A],
                               mtdItId: String, nino: String)(implicit hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
      logAndRedirectFromUrl(
        s"$agentAuthLogString - No active session. Redirecting to ${appConfig.signInUrl}",
        appConfig.signInUrl)
    case _: AuthorisationException =>
      authService.authorised(secondaryAgentPredicate(mtdItId))
        .retrieve(allEnrolments) { enrolments =>
          populateAgent(block, request, hc, mtdItId, nino, enrolments, isSupportingAgent = true)
        }.recoverWith {
          case _: AuthorisationException =>
            logAndRedirect(
              s"$agentAuthLogString - Agent does not have delegated authority for Client.",
              AgentAuthErrorController.show()
            )
          case e =>
            logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
            Future(errorHandler.internalServerError()(request))
        }
    case e =>
      logger.error(s"$agentAuthLogString - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
      Future(errorHandler.internalServerError()(request))
  }

  private def populateAgent[A](block: AuthorisationRequest[A] =>
    Future[Result], request: Request[A], hc: HeaderCarrier, mtdItId: String, nino: String, enrolments: Enrolments, isSupportingAgent: Boolean): Future[Result] = {
    if (isSupportingAgent) {
      logger.warn(s"$agentAuthLogString - Secondary agent unauthorised")
      Future.successful(Redirect(controllers.errors.routes.SupportingAgentAuthErrorController.show()))
    } else {
      enrolmentGetIdentifierValue(Agent.key, Agent.value, enrolments) match {
        case Some(arn) =>
          sessionIdFrom(request, hc).fold {
            logAndRedirectFromUrl(s"$agentAuthLogString - No session id in request", appConfig.signInUrl)
          } { sessionId =>
            block(AuthorisationRequest(models.User(mtdItId, Some(arn), nino, AffinityGroup.Agent.toString, sessionId), request))
          }
        case None =>
          logAndRedirect(s"$agentAuthLogString - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.", YouNeedAgentServicesController.show())
      }
    }
  }

  private[actions] def enrolmentGetIdentifierValue(checkedKey: String,
                                                   checkedIdentifier: String,
                                                   enrolments: Enrolments
                                                  ): Option[String] = enrolments.enrolments.collectFirst {
    case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
      case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
    }
  }.flatten

  private def logAndRedirect(logMessage: String, redirectUrl: => Call): Future[Result] = {
    logger.warn(logMessage)
    Future.successful(Redirect(redirectUrl))
  }
  private def logAndRedirectFromUrl(logMessage: String, redirectUrl: String): Future[Result] = {
    logger.warn(logMessage)
    Future.successful(Redirect(redirectUrl))
  }

  private def createAuthorisationRequest[A](mtdItId: String, arn: Option[String], nino: String,
                                            affinityGroup: AffinityGroup, request: Request[A],
                                            block: AuthorisationRequest[A] => Future[Result])
                                           (implicit hc: HeaderCarrier): Future[Result] = {
    sessionIdFrom(request, hc) match {
      case Some(sessionId) =>
        block(AuthorisationRequest(models.User(mtdItId, arn, nino, affinityGroup.toString, sessionId), request))
      case None =>
        logAndRedirectFromUrl(s"[AuthorisedAction] - No session id in request", appConfig.signInUrl)
    }
  }


  private def redirectToUnauthorisedUserErrorPage(): Result = {
    val logMessage = s"[AuthorisedAction][invokeBlock] - User failed to authenticate"
    logger.warn(logMessage)
    Redirect(UnauthorisedUserErrorController.show())
  }

  private def redirectToSignInPage(): Result = {
    val logMessage = s"[AuthorisedAction][invokeBlock] - No active session. Redirecting to ${appConfig.signInUrl}"
    logger.warn(logMessage)
    Redirect(appConfig.signInUrl)
  }

  private def sessionIdFrom(request: Request[_], hc: HeaderCarrier): Option[String] = hc.sessionId match {
    case Some(sessionId) => Some(sessionId.value)
    case _ => request.headers.get(SessionKeys.sessionId)
  }
}
