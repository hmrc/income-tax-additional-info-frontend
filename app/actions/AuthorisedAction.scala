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
import models.requests.AuthorisationRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import services.{AuthorisationService, MissingAgentClientDetails, SessionDetailsService}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait AuthorisedAction extends ActionBuilder[AuthorisationRequest, AnyContent]

class AuthorisedActionImpl @Inject()(authService: AuthorisationService,
                                 sessionDetailsService: SessionDetailsService,
                                 appConfig: AppConfig,
                                 cc: ControllerComponents,
                                 errorHandler: ErrorHandler)
                                (implicit val executionContext: ExecutionContext)
  extends AuthorisedAction with HeaderCarrierHelper with Logging {

  private val minimumConfidenceLevel: Int = ConfidenceLevel.L250.level

  private val agentLogPrefix: String = "[AuthorisedAction][agentAuthentication]"
  private val individualLogPrefix: String = "[AuthorisedAction][individualAuthentication]"

  override def parser: BodyParser[AnyContent] = cc.parsers.default

  override def invokeBlock[A](request: Request[A], block: AuthorisationRequest[A] => Future[Result]): Future[Result] = {
    implicit lazy val headerCarrier: HeaderCarrier = hcWithCorrelationId(request)

    sessionIdFrom(request, headerCarrier).fold({
      logger.info("No session ID was found for the request. Redirecting user to login")
      Future.successful(Redirect(appConfig.signInUrl))
    }) { currentSessionId =>
      authService.authorised().retrieve(affinityGroup) {
        case Some(AffinityGroup.Agent) => agentAuthentication(block, currentSessionId)(request, headerCarrier)
        case Some(affinityGroup) => individualAuthentication(block, affinityGroup)(request, headerCarrier)
        case _ => Future.successful(Redirect(UnauthorisedUserErrorController.show()))
      } recover {
        case _: NoActiveSession => {
          logger.warn(s"[AuthorisedAction][invokeBlock] - No active session. Redirecting to ${appConfig.signInUrl}")
          Redirect(appConfig.signInUrl)
        }
        case _: AuthorisationException => Redirect(UnauthorisedUserErrorController.show())
        case e => logger.error(s"$agentLogPrefix - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
          errorHandler.internalServerError()(request)
      }
    }
  }

  private[actions] def individualAuthentication[A](block: AuthorisationRequest[A] => Future[Result], affinityGroup: AffinityGroup)
                                                  (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    authService.authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>
        handleEnrolmentValidation(enrolments, affinityGroup, request, block)
      case _ =>
        logger.warn(s"$individualLogPrefix User has confidence level below 250, routing user to IV uplift.")
        Future.successful(Redirect(appConfig.incomeTaxSubmissionIvRedirect))
    }
  }

  private def handleEnrolmentValidation[A](enrolments: Enrolments, affinityGroup: AffinityGroup,
                                           request: Request[A], block: AuthorisationRequest[A] => Future[Result])
                                          (implicit hc: HeaderCarrier): Future[Result] = {
    val optionalMtdItId: Option[String] = enrolmentGetIdentifierValue(Individual.key, Individual.value, enrolments)
    val optionalNino: Option[String] = enrolmentGetIdentifierValue(Nino.key, Nino.value, enrolments)

    (optionalMtdItId, optionalNino) match {
      case (Some(mtdItId), Some(nino)) =>
        sessionIdFrom(request, hc) match {
          case Some(sessionId) =>
            block(AuthorisationRequest(models.User(mtdItId, None, nino, affinityGroup.toString, sessionId), request))
          case None =>
            logger.warn(s"[AuthorisedAction] - No session id in request")
            Future.successful(Redirect(appConfig.signInUrl))
        }
      case (_, None) =>
          logger.warn(s"$individualLogPrefix - No active session. Redirecting to sign-in URL.")
          Future.successful(Redirect(appConfig.signInUrl))
      case (None, _) =>
        logger.warn(s"$individualLogPrefix - User has no MTD IT enrolment. Redirecting user to sign up for MTD.")
        Future.successful(Redirect(IndividualAuthErrorController.show()))
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

  private[actions] def agentAuthentication[A](block: AuthorisationRequest[A] => Future[Result],
                                        sessionId: String
                                       )(implicit request: Request[A],
                                         hc: HeaderCarrier): Future[Result] = {
    sessionDetailsService.getSessionData(sessionId).flatMap { sessionData =>
      authService
        .authorised(primaryAgentPredicate(sessionData.mtditid))
        .retrieve(allEnrolments)(
          enrolmentGetIdentifierValue(Agent.key, Agent.value, _) match {
            case Some(arn) =>
              val user = models.User(sessionData.mtditid, Some(arn), sessionData.nino, AffinityGroup.Agent.toString, sessionId)
              val authorisationRequest = AuthorisationRequest(user, request)
              block(authorisationRequest)
            case None =>
              logger.warn(s"$agentLogPrefix - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
              Future.successful(Redirect(YouNeedAgentServicesController.show()))
          }
        )
        .recoverWith(agentRecovery(request, sessionData.mtditid))
    }.recover {
      case _: MissingAgentClientDetails =>
        Redirect(appConfig.viewAndChangeEnterUtrUrl)
    }
  }

  private def agentRecovery[A](request: Request[A],
                               mtdItId: String
                              )(implicit hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: NoActiveSession =>
        logger.warn(s"$agentLogPrefix - No active session. Redirecting to ${appConfig.signInUrl}")
        Future.successful(Redirect(appConfig.signInUrl))
    case _: AuthorisationException =>
      authService.authorised(secondaryAgentPredicate(mtdItId))
        .retrieve(allEnrolments) { enrolments =>
          logger.warn(s"$agentLogPrefix - Secondary agent unauthorised")
          Future.successful(Redirect(controllers.errors.routes.SupportingAgentAuthErrorController.show()))
        }.recoverWith {
          case _: AuthorisationException =>
            logger.warn(s"$agentLogPrefix - Agent does not have delegated authority for Client.")
            Future.successful(Redirect(AgentAuthErrorController.show()))
          case e =>
            logger.error(s"$agentLogPrefix - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
            Future(errorHandler.internalServerError()(request))
        }
    case e =>
      logger.error(s"$agentLogPrefix - Unexpected exception of type '${e.getClass.getSimpleName}' was caught.")
      Future(errorHandler.internalServerError()(request))
  }

  private[actions] def enrolmentGetIdentifierValue(checkedKey: String,
                                                   checkedIdentifier: String,
                                                   enrolments: Enrolments
                                                  ): Option[String] = enrolments.enrolments.collectFirst {
    case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
      case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
    }
  }.flatten

  private def sessionIdFrom(request: Request[_], hc: HeaderCarrier): Option[String] = hc.sessionId match {
    case Some(sessionId) => Some(sessionId.value)
    case _ => request.headers.get(SessionKeys.sessionId)
  }
}
