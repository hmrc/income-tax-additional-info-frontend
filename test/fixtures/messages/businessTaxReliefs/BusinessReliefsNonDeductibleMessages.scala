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
    val hint: String = "For example, £193.54"

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
    override val headingAndTitle = "Llog ar fenthyciad na ellir ei ddidynnu o fuddsoddiadau mewn partneriaethau gosod eiddo"
    override val p1 = "Gallwch hawlio rhyddhad treth ar y gost o gael benthyciad i ariannu partneriaeth, neu’r llog ar fenthyciad o’r fath. Hefyd, mae modd hawlio rhyddhad ar y gost o gael trefniadau ariannol amgen yn eu lle, a’r llog ar daliadau am drefniadau o’r fath. Mae’r rhyddhad hwn wedi’i gyfyngu i ddefnydd yr arian hwnnw ar gyfer unrhyw ran o’r busnes sy’n ymwneud ag eiddo preswyl."
    override val p2 = "Mae’r rhyddhad treth y gallwch ei hawlio wedi’i gyfyngu i’r gyfradd sylfaenol o Dreth Incwm."
    override val p3 = "Am ragor o wybodaeth, ewch i Daflen Gymorth 340 (yn agor tab newydd) a darllen yr adran, ‘Cyfyngiad ar gostau ariannol eiddo preswyl’."
    override val label = "Faint yw’r llog ar fenthyciad na ellir ei ddidynnu o fuddsoddiadau mewn partneriaethau gosod eiddo?"
    override val hint: String = "Er enghraifft, £193.54"

    override val amountEmpty = "Nodwch swm y llog ar fenthyciad na ellir ei ddidynnu"
    override val amountInvalid = "Dim ond y rhifau 0-9 ac un pwynt degol y mae’n rhaid i’r llog ar fenthyciad na ellir ei ddidynnu gynnwys."
    override val amountTooLarge = "Mae’n rhaid i swm eich llog ar fenthyciad na ellir ei ddidynnu fod yn llai na £100,000,000,000"
    override val amountTooSmall = "Nodwch swm dilys ar gyfer swm y llog ar fenthyciad na ellir ei ddidynnu"
  }

  object WelshAgent extends Messages with Cy {
    override val headingAndTitle = Welsh.headingAndTitle
    override val p1 = "Gall eich cleient hawlio rhyddhad treth ar y gost o gael benthyciad i ariannu partneriaeth, neu’r llog ar fenthyciad o’r fath. Hefyd, mae modd hawlio rhyddhad ar y gost o gael trefniadau ariannol amgen yn eu lle, a’r llog ar daliadau am drefniadau o’r fath. Mae’r rhyddhad hwn wedi’i gyfyngu i ddefnydd yr arian hwnnw ar gyfer unrhyw ran o’r busnes sy’n ymwneud ag eiddo preswyl."
    override val p2 = "Mae’r rhyddhad treth y gall eich cleient ei hawlio wedi’i gyfyngu i’r gyfradd sylfaenol o Dreth Incwm."
    override val p3 = Welsh.p3
    override val label = Welsh.label
    override val hint: String = "Er enghraifft, £193.54"

    override val amountEmpty = Welsh.amountEmpty
    override val amountInvalid = Welsh.amountInvalid
    override val amountTooLarge = "Mae’n rhaid i swm llog eich cleient ar fenthyciad na ellir ei ddidynnu fod yn llai na £100,000,000,000"
    override val amountTooSmall = Welsh.amountTooSmall
  }
}
