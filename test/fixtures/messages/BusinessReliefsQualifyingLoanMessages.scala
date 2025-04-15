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

package fixtures.messages

object BusinessReliefsQualifyingLoanMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "Qualifying loan interest payable in the year"
    val p1 = "You can claim tax relief for interest on a loan or alternative finance arrangement used to buy:"
    val p2 = "The total amount of certain Income Tax reliefs that can be used to reduce your total taxable income is limited to £50,000, or 25% of your adjusted total income, whichever amount is greater."
    val b1 = "shares in, or to fund, a close company"
    val b2 = "an interest, or to fund, a partnership (if the partnership is a property letting partnership, follow the link below for further guidance)"
    val b3 = "plant or machinery for your work (unless you have already deducted this as a business expense)"
    val link = "More information about interest and alternative finance payments eligible for relief"
    val label = "How much is the qualifying loan interest payable in the year?"

    val amountEmpty = "Enter how much interest is payable in the year"
    val amountInvalid = "Interest payable in the year must only include the numbers 0-9 and a decimal point"
    val amountTooLarge = "The amount of your loan interest must be less than £100,000,000,000"
    val amountTooSmall = "Enter a valid amount for how much interest is payable in the year"
  }

  object English extends Messages with En

  object EnglishAgent extends Messages with En {
    override val p1 = "Your client can claim tax relief for interest on a loan or alternative finance arrangement used to buy:"
    override val p2 = "The total amount of certain Income Tax reliefs that can be used to reduce your client's total taxable income is limited to £50,000, or 25% of your client's adjusted total income, whichever amount is greater."
    override val b3 = "plant or machinery for your client's work (unless you have already deducted this as a business expense)"
    override val amountTooLarge = "The amount of your client's loan interest must be less than £100,000,000,000"
  }

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Qualifying loan interest payable in the year"
    override val p1 = "You can claim tax relief for interest on a loan or alternative finance arrangement used to buy:"
    override val p2 = "The total amount of certain Income Tax reliefs that can be used to reduce your total taxable income is limited to £50,000, or 25% of your adjusted total income, whichever amount is greater."
    override val b1 = "shares in, or to fund, a close company"
    override val b2 = "an interest, or to fund, a partnership (if the partnership is a property letting partnership, follow the link below for further guidance)"
    override val b3 = "plant or machinery for your work (unless you have already deducted this as a business expense)"
    override val link = "More information about interest and alternative finance payments eligible for relief"
    override val label = "How much is the qualifying loan interest payable in the year?"

    override val amountEmpty = "Enter how much interest is payable in the year"
    override val amountInvalid = "Interest payable in the year must only include the numbers 0-9 and a decimal point"
    override val amountTooLarge = "The amount of your loan interest must be less than £100,000,000,000"
    override val amountTooSmall = "Enter a valid amount for how much interest is payable in the year"
  }

  object WelshAgent extends Messages with Cy {
    override val headingAndTitle = "Qualifying loan interest payable in the year"
    override val p1 = "Your client can claim tax relief for interest on a loan or alternative finance arrangement used to buy:"
    override val p2 = "The total amount of certain Income Tax reliefs that can be used to reduce your client's total taxable income is limited to £50,000, or 25% of your client's adjusted total income, whichever amount is greater."
    override val b1 = "shares in, or to fund, a close company"
    override val b2 = "an interest, or to fund, a partnership (if the partnership is a property letting partnership, follow the link below for further guidance)"
    override val b3 = "plant or machinery for your client's work (unless you have already deducted this as a business expense)"
    override val link = "More information about interest and alternative finance payments eligible for relief"
    override val label = "How much is the qualifying loan interest payable in the year?"

    override val amountEmpty = "Enter how much interest is payable in the year"
    override val amountInvalid = "Interest payable in the year must only include the numbers 0-9 and a decimal point"
    override val amountTooLarge = "The amount of your client's loan interest must be less than £100,000,000,000"
    override val amountTooSmall = "Enter a valid amount for how much interest is payable in the year"
  }
}
