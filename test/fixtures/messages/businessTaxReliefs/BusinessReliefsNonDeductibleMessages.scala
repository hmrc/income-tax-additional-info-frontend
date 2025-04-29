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

package fixtures.messages.businessTaxReliefs

import fixtures.messages.{Cy, En, i18n}

object BusinessReliefsNonDeductibleMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "Non-deductible loan interest from investments into property letting partnerships"
    val p1 = "You can claim tax relief on the cost of getting a loan to fund a partnership, or the interest on such a loan. Relief can also be claimed on the cost of getting alternative finance arrangements, and the interest on payments for such arrangements. This relief is limited to the extent that those funds are used for any part of the business that consists of residential properties."
    val p2 = "The tax relief you can claim is restricted to the basic rate of Income Tax."
    val p3 = "For more information, visit Helpsheet 340 (opens in new tab) and read the section titled Residential property finance cost restriction."
    val label = "How much is the non-deductible loan interest from investments into property letting partnerships?"

    val amountEmpty = "Enter the value of the non-deductible loan interest"
    val amountInvalid = "Non-deductible loan interest must only include the numbers 0-9 and a decimal point"
    val amountTooLarge = "The amount of your non-deductible loan interest must be less than £100,000,000,000"
    val amountTooSmall = "Enter a valid amount for the value of the non-deductible loan interest"
  }

  object English extends Messages with En

  object EnglishAgent extends Messages with En {
    override val p1 = "Your client can claim tax relief on the cost of getting a loan to fund a partnership, or the interest on such a loan. Relief can also be claimed on the cost of getting alternative finance arrangements, and the interest on payments for such arrangements. This relief is limited to the extent that those funds are used for any part of the business that consists of residential properties."
    override val p2 = "The tax relief your client can claim is restricted to the basic rate of Income Tax."
    override val amountTooLarge = "The amount of your client's non-deductible loan interest must be less than £100,000,000,000"
  }

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Non-deductible loan interest from investments into property letting partnerships (Welsh)"
    override val p1 = "You can claim tax relief on the cost of getting a loan to fund a partnership, or the interest on such a loan. Relief can also be claimed on the cost of getting alternative finance arrangements, and the interest on payments for such arrangements. This relief is limited to the extent that those funds are used for any part of the business that consists of residential properties. (Welsh)"
    override val p2 = "The tax relief you can claim is restricted to the basic rate of Income Tax. (Welsh)"
    override val p3 = "For more information, visit Helpsheet 340 (opens in new tab) and read the section titled Residential property finance cost restriction. (Welsh)"
    override val label = "How much is the non-deductible loan interest from investments into property letting partnerships? (Welsh)"

    override val amountEmpty = "Enter the value of the non-deductible loan interest (Welsh)"
    override val amountInvalid = "Non-deductible loan interest must only include the numbers 0-9 and a decimal point (Welsh)"
    override val amountTooLarge = "The amount of your non-deductible loan interest must be less than £100,000,000,000 (Welsh)"
    override val amountTooSmall = "Enter a valid amount for the value of the non-deductible loan interest (Welsh)"
  }

  object WelshAgent extends Messages with Cy {
    override val headingAndTitle = "Non-deductible loan interest from investments into property letting partnerships (Welsh)"
    override val p1 = "Your client can claim tax relief on the cost of getting a loan to fund a partnership, or the interest on such a loan. Relief can also be claimed on the cost of getting alternative finance arrangements, and the interest on payments for such arrangements. This relief is limited to the extent that those funds are used for any part of the business that consists of residential properties. (Welsh)"
    override val p2 = "The tax relief your client can claim is restricted to the basic rate of Income Tax. (Welsh)"
    override val p3 = "For more information, visit Helpsheet 340 (opens in new tab) and read the section titled Residential property finance cost restriction. (Welsh)"
    override val label = "How much is the non-deductible loan interest from investments into property letting partnerships? (Welsh)"

    override val amountEmpty = "Enter the value of the non-deductible loan interest (Welsh)"
    override val amountInvalid = "Non-deductible loan interest must only include the numbers 0-9 and a decimal point (Welsh)"
    override val amountTooLarge = "The amount of your client's non-deductible loan interest must be less than £100,000,000,000 (Welsh)"
    override val amountTooSmall = "Enter a valid amount for the value of the non-deductible loan interest (Welsh)"
  }
}
