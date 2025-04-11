/*
 * Copyright 2025 HM Revenue & Customs
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

package forms.businessTaxReliefs

import fixtures.messages.businessTaxReliefs.PostCessationTradeReliefMessages
import forms.businessTaxReliefs.PostCessationTradeReliefForm.{key => formKey}
import models.BusinessTaxReliefs
import models.requests.{AuthorisationRequest, JourneyDataRequest}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import play.api.i18n.{Messages, MessagesApi}
import support.UnitTest
import support.utils.TaxYearUtils.taxYear

class PostCessationTradeReliefFormSpec extends UnitTest with GuiceOneAppPerSuite {

  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(
    false -> PostCessationTradeReliefMessages.English,
    true -> PostCessationTradeReliefMessages.EnglishAgent,
    false -> PostCessationTradeReliefMessages.Welsh,
    true -> PostCessationTradeReliefMessages.WelshAgent
  ).foreach { case (isAgent, messagesForLanguage) =>

    s"when the form is rendered with language of '${messagesForLanguage.language.name}'" when {

      implicit val msgs: Messages = messagesApi.preferred(Seq(messagesForLanguage.lang))

      s"the user is an ${if(isAgent) "Agent" else "Individual"}" when {

        val authRequest: AuthorisationRequest[_] = if (isAgent) agentRequest else individualRequest
        implicit val request: JourneyDataRequest[_] = JourneyDataRequest(authRequest.user, authRequest, emptyUserAnswers(taxYear, BusinessTaxReliefs))

        "bind valid values" when {

          "amount is greater than 0 (0dp)" in {
            PostCessationTradeReliefForm().bind(Map(formKey -> "1")).value shouldBe Some(1)
          }

          "amount is greater than 0 (1dp)" in {
            PostCessationTradeReliefForm().bind(Map(formKey -> "1.1")).value shouldBe Some(1.1)
          }

          "amount is greater than 0 (2dp)" in {
            PostCessationTradeReliefForm().bind(Map(formKey -> "1.12")).value shouldBe Some(1.12)
          }

          "amount contains commas (2dp)" in {
            PostCessationTradeReliefForm().bind(Map(formKey -> "1,000,569.12")).value shouldBe Some(1000569.12)
          }
        }

        "return correct error message for invalid values" when {

          "amount is empty" in {
            val boundForm = PostCessationTradeReliefForm().bind(Map(formKey -> ""))
            boundForm.errors should contain(FormError(formKey, messagesForLanguage.amountEmpty))
          }

          "amount contains a non numeric values" in {
            val boundForm = PostCessationTradeReliefForm().bind(Map(formKey -> "number"))
            boundForm.errors should contain(FormError(formKey, messagesForLanguage.amountInvalid))
          }

          "amount is zero" in {
            val boundForm = PostCessationTradeReliefForm().bind(Map(formKey -> "0"))
            boundForm.errors should contain(FormError(formKey, messagesForLanguage.amountTooSmall))
          }

          "amount too big" in {
            val boundForm = PostCessationTradeReliefForm().bind(Map(formKey -> "10000000000000000"))
            boundForm.errors should contain(FormError(formKey, messagesForLanguage.amountTooLarge))
          }
        }
      }
    }
  }
}
