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

object BusinessReliefsQualifyingLoanMessages {

  sealed trait Messages { _: i18n =>
    val headingAndTitle = "Qualifying loan interest payable in the year"
    val p1 = "You can claim tax relief for interest on a loan or alternative finance arrangement used to buy:"
    val p2 = "The total amount of certain Income Tax reliefs that can be used to reduce your total taxable income is limited to £50,000, or 25% of your adjusted total income, whichever amount is greater."
    val b1 = "shares in, or to fund, a close company"
    val b2 = "an interest, or to fund, a partnership (if the partnership is a property letting partnership, follow the link below for further guidance)"
    val b3 = "plant or machinery for your work (unless you have already deducted this as a business expense)"
    val link = "More information about interest and alternative finance payments eligible for relief (opens in new tab)"
    val label = "How much is the qualifying loan interest payable in the year?"
    val hint: String = "For example, £193.54"

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
    override val headingAndTitle = "Llog ar fenthyciad cymwys yn daladwy yn y flwyddyn"
    override val p1 = "Gallwch hawlio rhyddhad treth ar log ar fenthyciad neu drefniant ariannu amgen a ddefnyddiwyd i brynu’r canlynol:"
    override val p2 = "Mae cyfanswm y rhyddhadau Treth Incwm penodol y gellir eu defnyddio i leihau cyfanswm eich incwm trethadwy wedi’i gyfyngu i £50,000, neu 25% o gyfanswm eich incwm wedi’i addasu – pa un bynnag sydd fwyaf."
    override val b1 = "cyfranddaliadau mewn, neu i ariannu, cwmni caeedig"
    override val b2 = "buddiant mewn, neu i ariannu, partneriaeth (os yw’r bartneriaeth yn bartneriaeth gosod eiddo, dilynwch y cysylltiad isod i gael arweiniad pellach)"
    override val b3 = "offer neu beiriannau ar gyfer eich gwaith (oni bai eich bod eisoes wedi didynnu hwn fel traul busnes)"
    override val link = "Rhagor o wybodaeth ynghylch llog a thaliadau cyllid amgen sy’n gymwys ar gyfer ryddhad (yn agor tab newydd)"
    override val label = "Beth yw swm y llog ar fenthyciad cymwys sy’n daladwy yn y flwyddyn?"
    override val hint: String = "Er enghraifft, £193.54"

    override val amountEmpty = "Nodwch swm y log sy’n daladwy yn y flwyddyn"
    override val amountInvalid = "Mae’n rhaid i swm y llog sy’n daladwy yn y flwyddyn gynnwys y rhifau 0 i 9 a phwynt degol yn unig"
    override val amountTooLarge = "Mae’n rhaid i swm eich llog ar fenthyciad fod yn llai na £100,000,000,000"
    override val amountTooSmall = "Nodwch swm dilys ar gyfer faint o log sy’n daladwy yn y flwyddyn"
  }

  object WelshAgent extends Messages with Cy {
    override val headingAndTitle = "Llog ar fenthyciad cymwys yn daladwy yn y flwyddyn"
    override val p1 = "Gall eich cleient hawlio rhyddhad treth ar log ar fenthyciad neu drefniant ariannu amgen a ddefnyddiwyd i brynu’r canlynol:"
    override val p2 = "Mae cyfanswm y rhyddhadau Treth Incwm penodol y gellir eu defnyddio i leihau cyfanswm incwm trethadwy eich cleient wedi’i gyfyngu i £50,000, neu 25% o gyfanswm incwm eich cleient wedi’i addasu – pa un bynnag sydd fwyaf."
    override val b1 = "cyfranddaliadau mewn, neu i ariannu, cwmni caeedig"
    override val b2 = "buddiant mewn, neu i ariannu, partneriaeth (os yw’r bartneriaeth yn bartneriaeth gosod eiddo, dilynwch y cysylltiad isod i gael arweiniad pellach)"
    override val b3 = "offer neu beiriannau ar gyfer gwaith eich cleient (oni bai eich bod eisoes wedi didynnu hwn fel traul busnes)"
    override val link = "Rhagor o wybodaeth ynghylch llog a thaliadau cyllid amgen sy’n gymwys ar gyfer ryddhad (yn agor tab newydd)"
    override val label = "Beth yw swm y llog ar fenthyciad cymwys sy’n daladwy yn y flwyddyn?"
    override val hint: String = "Er enghraifft, £193.54"

    override val amountEmpty = "Nodwch swm y log sy’n daladwy yn y flwyddyn"
    override val amountInvalid = "Mae’n rhaid i swm y llog sy’n daladwy yn y flwyddyn gynnwys y rhifau 0 i 9 a phwynt degol yn unig"
    override val amountTooLarge = "Mae’n rhaid i swm llog ar fenthyciad eich cleient fod yn llai na £100,000,000,000"
    override val amountTooSmall = "Nodwch swm dilys ar gyfer faint o log sy’n daladwy yn y flwyddyn"
  }
}
