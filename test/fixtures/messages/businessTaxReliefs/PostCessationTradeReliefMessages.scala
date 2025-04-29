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

object PostCessationTradeReliefMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "Post-cessation trade relief and certain other losses"
    val p1 = "If you’ve paid expenses in connection with a business that has ceased, you may be able to get tax relief on those payments. This relief can only be claimed if you didn’t claim those expenses at the time the business ceased, or anywhere else in this or an earlier tax return. Post-cessation expenses are always set against post-cessation receipts first."
    val p2 = "The expenses must be paid within 7 years of your cessation, and should be claimed against the tax return for the year the expense was paid. The claim must also be made by the anniversary of the filing deadline for the tax year in question. For example, a claim for an expense paid in the tax year 6 April 2026 to 5 April 2027 must be made by 31 January 2029."
    val expensesSummaryHeading = "Post-cessation expenses"
    val expensesSummaryP1 = "Post-cessation expenses are expenses that would have been taken from the business’s profits had it not ceased trading. These expenses are:"
    val expensesSummaryBullet1 = "fixing defective work done, or replacing defective services or goods supplied"
    val expensesSummaryBullet2 = "any legal or professional costs for any claim against you for defective work"
    val expensesSummaryBullet3 = "insurance against such expenses"
    val expensesSummaryBullet4 = "recovering debts that were taken into account in calculating your pre-cessation profits. This can include debts that have gone bad, or have been voluntarily released, within 7 years of cessation"
    val liabilitiesSummaryHeading = "Relief for former employee’s liabilities and costs"
    val liabilitiesSummaryP1 = "If the amount of liabilities or costs to be entered relating to your actual or alleged wrongful acts in a former employment, exceed your total income in the year you may be able to claim the excess against capital gains. There are special rules limiting the relief if you did not pay for these costs, for example, yourself."
    val lossSummaryHeading = "Employment loss relief"
    val lossSummaryP1 = "Relief for losses arising from an employment or office is only available in exceptional circumstances. This is because, as a general rule, employment expenses cannot exceed the earnings from which they are deductible. A loss can sometimes arise where capital allowances due cannot be deducted from the earnings from the employment. Additionally, a loss may also come directly from the conditions of the employment. Otherwise, relief cannot usually be claimed for these losses."
    val label = "How much are you claiming for post-cessation trade relief and certain other losses?"

    val amountEmpty = "Enter how much relief you are claiming"
    val amountInvalid = "The amount of relief must only include the numbers 0-9 and a decimal point"
    val amountTooLarge = "The amount of your relief must be less than £100,000,000,000"
    val amountTooSmall = "Enter a valid amount for how much relief you are claiming"
  }

  object English extends Messages with En

  object EnglishAgent extends Messages with En {
    override val p1 = "If your client has paid expenses in connection with a business that has ceased, they may be able to get tax relief on those payments. This relief can only be claimed if they didn’t claim those expenses at the time the business ceased, or anywhere else in this or an earlier tax return. Post-cessation expenses are always set against post-cessation receipts first."
    override val liabilitiesSummaryP1 = "If the amount of liabilities or costs to be entered, relating to your client’s actual or alleged wrongful acts in a former employment, exceed their total income in the year, they may be able to claim the excess against capital gains. There are special rules limiting the relief if they did not pay for these costs, for example, themselves."
    override val label = "How much is your client claiming for post-cessation trade relief and certain other losses?"

    override val amountEmpty = "Enter how much relief your client is claiming"
    override val amountTooLarge = "The amount of your client’s relief must be less than £100,000,000,000"
    override val amountTooSmall = "Enter a valid amount for how much relief your client is claiming"
  }

  object Welsh extends Messages with Cy {
    override val headingAndTitle = "Post-cessation trade relief and certain other losses (Welsh)"
    override val p1 = "If you’ve paid expenses in connection with a business that has ceased, you may be able to get tax relief on those payments. This relief can only be claimed if you didn’t claim those expenses at the time the business ceased, or anywhere else in this or an earlier tax return. Post-cessation expenses are always set against post-cessation receipts first. (Welsh)"
    override val p2 = "The expenses must be paid within 7 years of your cessation, and should be claimed against the tax return for the year the expense was paid. The claim must also be made by the anniversary of the filing deadline for the tax year in question. For example, a claim for an expense paid in the tax year 6 April 2026 to 5 April 2027 must be made by 31 January 2029. (Welsh)"
    override val expensesSummaryHeading = "Post-cessation expenses (Welsh)"
    override val expensesSummaryP1 = "Post-cessation expenses are expenses that would have been taken from the business’s profits had it not ceased trading. These expenses are: (Welsh)"
    override val expensesSummaryBullet1 = "fixing defective work done, or replacing defective services or goods supplied (Welsh)"
    override val expensesSummaryBullet2 = "any legal or professional costs for any claim against you for defective work (Welsh)"
    override val expensesSummaryBullet3 = "insurance against such expenses (Welsh)"
    override val expensesSummaryBullet4 = "recovering debts that were taken into account in calculating your pre-cessation profits. This can include debts that have gone bad, or have been voluntarily released, within 7 years of cessation (Welsh)"
    override val liabilitiesSummaryHeading = "Relief for former employee’s liabilities and costs (Welsh)"
    override val liabilitiesSummaryP1 = "If the amount of liabilities or costs to be entered relating to your actual or alleged wrongful acts in a former employment, exceed your total income in the year you may be able to claim the excess against capital gains. There are special rules limiting the relief if you did not pay for these costs, for example, yourself. (Welsh)"
    override val lossSummaryHeading = "Employment loss relief (Welsh)"
    override val lossSummaryP1 = "Relief for losses arising from an employment or office is only available in exceptional circumstances. This is because, as a general rule, employment expenses cannot exceed the earnings from which they are deductible. A loss can sometimes arise where capital allowances due cannot be deducted from the earnings from the employment. Additionally, a loss may also come directly from the conditions of the employment. Otherwise, relief cannot usually be claimed for these losses. (Welsh)"
    override val label = "How much are you claiming for post-cessation trade relief and certain other losses? (Welsh)"

    override val amountEmpty = "Enter how much relief you are claiming (Welsh)"
    override val amountInvalid = "The amount of relief must only include the numbers 0-9 and a decimal point (Welsh)"
    override val amountTooLarge = "The amount of your relief must be less than £100,000,000,000 (Welsh)"
    override val amountTooSmall = "Enter a valid amount for how much relief you are claiming (Welsh)"
  }

  object WelshAgent extends Messages with Cy {
    override val headingAndTitle = Welsh.headingAndTitle
    override val p1 = "If your client has paid expenses in connection with a business that has ceased, they may be able to get tax relief on those payments. This relief can only be claimed if they didn’t claim those expenses at the time the business ceased, or anywhere else in this or an earlier tax return. Post-cessation expenses are always set against post-cessation receipts first. (Welsh)"
    override val p2 = Welsh.p2
    override val expensesSummaryHeading = Welsh.expensesSummaryHeading
    override val expensesSummaryP1 = Welsh.expensesSummaryP1
    override val expensesSummaryBullet1 = Welsh.expensesSummaryBullet1
    override val expensesSummaryBullet2 = Welsh.expensesSummaryBullet2
    override val expensesSummaryBullet3 = Welsh.expensesSummaryBullet3
    override val expensesSummaryBullet4 = Welsh.expensesSummaryBullet4
    override val liabilitiesSummaryHeading = Welsh.liabilitiesSummaryHeading
    override val liabilitiesSummaryP1 = "If the amount of liabilities or costs to be entered, relating to your client’s actual or alleged wrongful acts in a former employment, exceed their total income in the year, they may be able to claim the excess against capital gains. There are special rules limiting the relief if they did not pay for these costs, for example, themselves. (Welsh)"
    override val lossSummaryHeading = Welsh.lossSummaryHeading
    override val lossSummaryP1 = Welsh.lossSummaryP1
    override val label = "How much is your client claiming for post-cessation trade relief and certain other losses? (Welsh)"

    override val amountEmpty = "Enter how much relief your client is claiming (Welsh)"
    override val amountInvalid = Welsh.amountInvalid
    override val amountTooLarge = "The amount of your client’s relief must be less than £100,000,000,000 (Welsh)"
    override val amountTooSmall = "Enter a valid amount for how much relief your client is claiming (Welsh)"
  }
}
