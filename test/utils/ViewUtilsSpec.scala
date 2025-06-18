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

package utils

import models.BusinessTaxReliefs
import models.requests.JourneyDataRequest
import org.scalamock.scalatest.MockFactory
import play.api.i18n.{Lang, Messages}
import play.i18n
import support.UnitTest
import support.utils.TaxYearUtils.taxYear

import java.time.LocalDate

class ViewUtilsSpec extends UnitTest
  with MockFactory {

  private val messages = new Messages {
    override def lang: Lang = throw new NotImplementedError

    override def apply(key: String, args: Any*): String = key.replace("common.", "").capitalize

    override def apply(keys: Seq[String], args: Any*): String = throw new NotImplementedError

    override def translate(key: String, args: Seq[Any]): Option[String] = throw new NotImplementedError

    override def isDefinedAt(key: String): Boolean = throw new NotImplementedError

    override def asJava: i18n.Messages = throw new NotImplementedError
  }

  ".bigDecimalCurrency" should {
    "Place comma in appropriate place when given amount over 999" in {
      ViewUtils.bigDecimalCurrency("45000.10") shouldBe "£45,000.10"
    }
  }

  ".translatedDateFormatter" should {
    "translate date" in {
      ViewUtils.translatedDateFormatter(LocalDate.parse("2002-01-01"))(messages = messages) shouldBe "1 January 2002"
    }
  }

  ".dynamicMessage" when {
    "request is from an Agent" should {
      "output the Agent message" in {

        implicit val request: JourneyDataRequest[_] = JourneyDataRequest(agentRequest.user, agentRequest, emptyUserAnswers(taxYear, BusinessTaxReliefs))
        implicit val msgs: Messages = messages

        ViewUtils.dynamicMessage("common.test") shouldBe "Test.agent"
      }
    }

    "request is from an Individual" should {
      "output the Individual message" in {

        implicit val request: JourneyDataRequest[_] = JourneyDataRequest(individualRequest.user, individualRequest, emptyUserAnswers(taxYear, BusinessTaxReliefs))
        implicit val msgs: Messages = messages

        ViewUtils.dynamicMessage("common.test") shouldBe "Test.individual"
      }
    }
  }
}
