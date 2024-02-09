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
import controllers.errors.routes.{AgentAuthErrorController, IndividualAuthErrorController, UnauthorisedUserErrorController, YouNeedAgentServicesController}
import models.authorisation.Enrolment.{Agent, Individual, Nino}
import models.authorisation.SessionValues
import models.requests.AuthorisationRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import services.AuthorisationService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisedAction @Inject()(authService: AuthorisationService,
                                 appConfig: AppConfig,
                                 cc: ControllerComponents)
                                (implicit ec: ExecutionContext)
  extends ActionBuilder[AuthorisationRequest, AnyContent] with Logging {

  private val minimumConfidenceLevel: Int = ConfidenceLevel.L250.level

  override protected[actions] def executionContext: ExecutionContext = ec

  override def parser: BodyParser[AnyContent] = cc.parsers.default

  override def invokeBlock[A](request: Request[A], block: AuthorisationRequest[A] => Future[Result]): Future[Result] = {
    implicit lazy val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      .withExtraHeaders("X-CorrelationId"->correlationId(request.headers.get("CorrelationId")))

    authService.authorised().retrieve(affinityGroup) {
      case Some(AffinityGroup.Agent) => agentAuthentication(block)(request, headerCarrier)
      case Some(affinityGroup) => individualAuthentication(block, affinityGroup)(request, headerCarrier)
      case _ => Future.successful(redirectToUnauthorisedUserErrorPage())
    } recover {
      case _: NoActiveSession => redirectToSignInPage()
      case _: AuthorisationException => redirectToUnauthorisedUserErrorPage()
    }
  }

  private def correlationId(correlationIdHeader: Option[String]): String = {

    if (correlationIdHeader.isDefined) {
      logger.info("[AuthorisedAction]Valid CorrelationId header found.")
      correlationIdHeader.get
    } else {
      lazy val id = UUID.randomUUID().toString
      logger.info(s"[AuthorisedAction]No valid CorrelationId found in headers. Defaulting Correlation Id. $id")
      id
    }
  }

  private[actions] def individualAuthentication[A](block: AuthorisationRequest[A] => Future[Result], affinityGroup: AffinityGroup)
                                                  (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    authService.authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>
        val optionalMtdItId: Option[String] = enrolmentGetIdentifierValue(Individual.key, Individual.value, enrolments)
        val optionalNino: Option[String] = enrolmentGetIdentifierValue(Nino.key, Nino.value, enrolments)

        (optionalMtdItId, optionalNino) match {
          case (Some(mtdItId), Some(nino)) =>
            sessionIdFrom(request, hc).fold {
              val logMessage = s"[AuthorisedAction][individualAuthentication] - No session id in request"
              logger.info(logMessage)
              Future.successful(Redirect(appConfig.signInUrl))
            } { sessionId =>
              block(AuthorisationRequest(models.User(mtdItId, None, nino, affinityGroup.toString, sessionId), request))
            }

          case (_, None) =>
            val logMessage = s"[AuthorisedAction][individualAuthentication] - No active session. Redirecting to ${appConfig.signInUrl}"
            logger.info(logMessage)
            Future.successful(Redirect(appConfig.signInUrl))
          case (None, _) =>
            val logMessage = s"[AuthorisedAction][individualAuthentication] - User has no MTD IT enrolment. Redirecting user to sign up for MTD."
            logger.info(logMessage)
            Future.successful(Redirect(IndividualAuthErrorController.show))
        }
      case _ =>
        val logMessage = "[AuthorisedAction][individualAuthentication] User has confidence level below 250, routing user to IV uplift."
        logger.info(logMessage)
        Future(Redirect(appConfig.incomeTaxSubmissionIvRedirect))
    }
  }

  private[actions] def agentAuthentication[A](block: AuthorisationRequest[A] => Future[Result])
                                             (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {
    lazy val agentDelegatedAuthRuleKey = "mtd-it-auth"
    lazy val agentAuthPredicate: String => Enrolment = identifierId =>
      Enrolment(Individual.key)
        .withIdentifier(Individual.value, identifierId)
        .withDelegatedAuthRule(agentDelegatedAuthRuleKey)
    val optionalNino = request.session.get(SessionValues.CLIENT_NINO)
    val optionalMtdItId = request.session.get(SessionValues.CLIENT_MTDITID)

    (optionalMtdItId, optionalNino) match {
      case (Some(mtdItId), Some(nino)) =>
        authService
          .authorised(agentAuthPredicate(mtdItId))
          .retrieve(allEnrolments) { enrolments =>

            enrolmentGetIdentifierValue(Agent.key, Agent.value, enrolments) match {
              case Some(arn) =>
                sessionIdFrom(request, hc).fold {
                  val logMessage = s"[AuthorisedAction][agentAuthentication] - No session id in request"
                  logger.info(logMessage)
                  Future(Redirect(appConfig.signInUrl))
                } { sessionId =>
                  block(AuthorisationRequest(models.User(mtdItId, Some(arn), nino, AffinityGroup.Agent.toString, sessionId), request))
                }

              case None =>
                val logMessage = "[AuthorisedAction][agentAuthentication] Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view."
                logger.info(logMessage)
                Future.successful(Redirect(YouNeedAgentServicesController.show))
            }
          } recover {
          case _: NoActiveSession =>
            val logMessage = s"[AuthorisedAction][agentAuthentication] - No active session. Redirecting to ${appConfig.signInUrl}"
            logger.info(logMessage)
            Redirect(appConfig.signInUrl)
          case _: AuthorisationException =>
            val logMessage = s"[AuthorisedAction][agentAuthentication] - Agent does not have delegated authority for Client."
            logger.info(logMessage)
            Redirect(AgentAuthErrorController.show)
        }
      case (mtditid, nino) =>
        val logMessage = s"[AuthorisedAction][agentAuthentication] - Agent does not have session key values. " +
          s"Redirecting to view & change. MTDITID missing:${mtditid.isEmpty}, NINO missing:${nino.isEmpty}"
        logger.info(logMessage)
        Future.successful(Redirect(appConfig.viewAndChangeEnterUtrUrl))
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

  private def redirectToUnauthorisedUserErrorPage(): Result = {
    val logMessage = s"[AuthorisedAction][invokeBlock] - User failed to authenticate"
    logger.info(logMessage)
    Redirect(UnauthorisedUserErrorController.show)
  }

  private def redirectToSignInPage(): Result = {
    val logMessage = s"[AuthorisedAction][invokeBlock] - No active session. Redirecting to ${appConfig.signInUrl}"
    logger.info(logMessage)
    Redirect(appConfig.signInUrl)
  }

  private def sessionIdFrom(request: Request[_], hc: HeaderCarrier): Option[String] = hc.sessionId match {
    case Some(sessionId) => Some(sessionId.value)
    case _ => request.headers.get(SessionKeys.sessionId)
  }
}
