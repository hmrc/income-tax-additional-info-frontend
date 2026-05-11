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

package audit

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import play.api.Configuration
import play.api.libs.json.Json
import support.UnitTest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends UnitTest {

  private trait Test {
    val mockedAppName = "some-app-name"
    val mockAuditConnector: AuditConnector = mock[AuditConnector]
    val mockConfig: Configuration = mock[Configuration]

    when(mockConfig.get[String](any[String]())(any[play.api.ConfigLoader[String]]()))
      .thenReturn(mockedAppName)

    lazy val target = new AuditService(mockAuditConnector, mockConfig)
    val ec: ExecutionContext = ExecutionContext.global
  }

  "AuditService" when {
    "auditing an event" should {
      val auditType = "Type"
      val transactionName = "Name"
      val eventDetails = "Details"
      val expected: Future[AuditResult] = Future.successful(Success)

      "return a successful audit result" in new Test {
        when(mockAuditConnector.sendExtendedEvent(any[ExtendedDataEvent]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(expected)

        val event = AuditModel(auditType, transactionName, eventDetails)
        target.auditModel(event) shouldBe expected
      }

      "generates an event with the correct auditSource" in new Test {
        val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

        when(mockAuditConnector.sendExtendedEvent(any[ExtendedDataEvent]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(expected)

        val event = AuditModel(auditType, transactionName, eventDetails)
        target.auditModel(event)

        verify(mockAuditConnector).sendExtendedEvent(captor.capture())(any[HeaderCarrier](), any[ExecutionContext]())
        captor.getValue.auditSource shouldBe mockedAppName
      }

      "generates an event with the correct auditType" in new Test {
        val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

        when(mockAuditConnector.sendExtendedEvent(any[ExtendedDataEvent]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(expected)

        val event = AuditModel(auditType, transactionName, eventDetails)
        target.auditModel(event)

        verify(mockAuditConnector).sendExtendedEvent(captor.capture())(any[HeaderCarrier](), any[ExecutionContext]())
        captor.getValue.auditType shouldBe auditType
      }

      "generates an event with the correct details" in new Test {
        val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

        when(mockAuditConnector.sendExtendedEvent(any[ExtendedDataEvent]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(expected)

        val event = AuditModel(auditType, transactionName, eventDetails)
        target.auditModel(event)

        verify(mockAuditConnector).sendExtendedEvent(captor.capture())(any[HeaderCarrier](), any[ExecutionContext]())
        captor.getValue.detail shouldBe Json.toJson(eventDetails)
      }

      "generates an event with the correct transactionName" in new Test {
        val captor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

        when(mockAuditConnector.sendExtendedEvent(any[ExtendedDataEvent]())(any[HeaderCarrier](), any[ExecutionContext]()))
          .thenReturn(expected)

        val event = AuditModel(auditType, transactionName, eventDetails)
        target.auditModel(event)

        verify(mockAuditConnector).sendExtendedEvent(captor.capture())(any[HeaderCarrier](), any[ExecutionContext]())
        captor.getValue.tags.exists(tag => tag == "transactionName" -> transactionName) shouldBe true
      }
    }
  }
}

