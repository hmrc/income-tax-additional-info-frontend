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
    val hint: String = "For example, £193.54"

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
    override val headingAndTitle = "hyddhad masnach ôl-derfynu a rhai colledion penodol eraill"
    override val p1 = "Os ydych wedi talu treuliau mewn perthynas â busnes ers i’r busnes hwnnw ddod i ben, efallai y gallwch gael rhyddhad treth ar y taliadau hynny. Gall y rhyddhad dim ond cael ei hawlio os na wnaethoch hawlio’r treuliau hynny ar yr adeg y gwnaeth y busnes ddod i ben, neu unrhyw fan arall yn y Ffurflen Dreth hon neu Ffurflen Dreth gynharach. Mae treuliau ôl-derfynu pob tro yn cael eu gosod yn erbyn derbyniadau ôl-derfynu yn gyntaf."
    override val p2 = "Mae’n rhaid i’r treuliau gael eu talu cyn pen 7 mlynedd i’r dyddiad y gwnaeth y busnes ddod i ben, a dylent gael eu hawlio yn erbyn y Ffurflen Dreth ar gyfer y flwyddyn y talwyd y treuliau. Mae’n rhaid i’r hawliad hefyd gael ei wneud erbyn pen-blwydd y dyddiad cau ar gyfer cyflwyno ar gyfer y flwyddyn dreth dan sylw. Er enghraifft, mae’n rhaid i gais am draul a delir yn y flwyddyn dreth 6 Ebrill 2026 i 5 Ebrill 2027 gael ei wneud erbyn 31 Ionawr 2029."
    override val expensesSummaryHeading = "Treuliau ôl-derfynu"
    override val expensesSummaryP1 = "Ystyr treuliau ôl-derfynu yw treuliau y byddai wedi cael eu cymryd o elw’r busnes os nad oedd y busnes wedi rhoi’r gorau i fasnachu. Mae’r treuliau hyn yn cynnwys y canlynol:"
    override val expensesSummaryBullet1 = "trwsio gwaith diffygiol wedi’i wneud, neu amnewid gwasanaethau neu nwyddau diffygiol a gyflenwyd"
    override val expensesSummaryBullet2 = "unrhyw gostau cyfreithiol neu broffesiynol ar gyfer unrhyw hawliadau yn eich erbyn am waith diffygiol"
    override val expensesSummaryBullet3 = "yswiriant yn erbyn treuliau o’r fath"
    override val expensesSummaryBullet4 = "adennill dyledion a gymerwyd i ystyriaeth wrth gyfrifo eich elw cyn-derfynu. Gall hyn gynnwys dyledion sydd wedi’u troi’n ddrwgddyled, neu sydd wedi’u rhyddhau’n wirfoddol, o fewn 7 mlynedd o’r flwyddyn darfod"
    override val liabilitiesSummaryHeading = "Rhyddhad ar gyfer rhwymedigaethau a chostau cyn gyflogai"
    override val liabilitiesSummaryP1 = "Os yw swm y rhwymedigaethau neu gostau sydd i’w nodi ac sy’n ymwneud â gweithredoedd anghyfreithlon gwirioneddol neu honedig a gyflawnwyd gennych mewn cyflogaeth flaenorol, yn fwy na chyfanswm eich incwm yn y flwyddyn, efallai y gallwch hawlio’r swm gormodol drwy ei osod yn erbyn enillion cyfalaf. Mae rheolau arbennig sy’n cyfyngu ar y rhyddhad os na wnaethoch, er enghraifft, dalu am y costau hyn eich hun."
    override val lossSummaryHeading = "Rhyddhad colledion ar gyfer cyflogaeth"
    override val lossSummaryP1 = "Mae rhyddhad ar gyfer colledion sy’n deillio o gyflogaeth neu swydd ar gael mewn amgylchiadau eithriadol yn unig. Mae hyn oherwydd, fel rheol gyffredinol, ni all treuliau cyflogaeth fod yn fwy na’r enillion y maent yn cael ei ddidynnu ohono. Gall colled weithiau digwydd pan na ellir didynnu’r lwfansau cyfalaf sy’n ddyledus o’r enillion o gyflogaeth. Ar ben hynny, gall colled hefyd ddigwydd yn uniongyrchol oherwydd amodau’r gyflogaeth. Fel arall, ni all rhyddhad gael ei hawlio ar gyfer y colledion hyn fel arfer."
    override val label = "Faint ydych chi’n ei hawlio ar gyfer rhyddhad masnach ôl-derfynu a rhai colledion penodol eraill?"
    override val hint: String = "Er enghraifft, £193.54"

    override val amountEmpty = "Nodwch faint o ryddhad rydych yn ei hawlio"
    override val amountInvalid = "Mae’n rhaid i swm y rhyddhad gynnwys y rhifau 0-9 a phwynt degol yn unig"
    override val amountTooLarge = "Mae’n rhaid i swm eich rhyddhad fod yn llai na £100,000,000,000"
    override val amountTooSmall = "Nodwch rif dilys am faint o ryddhad rydych yn ei hawlio"
  }

  object WelshAgent extends Messages with Cy {
    override val headingAndTitle = Welsh.headingAndTitle
    override val p1 = "Os yw’ch cleient wedi talu treuliau mewn perthynas â busnes ers i’r busnes hwnnw ddod i ben, efallai gall eich cleient gael rhyddhad treth ar y taliadau hynny. Gall y rhyddhad dim ond cael ei hawlio os na wnaeth eich cleient hawlio’r treuliau hynny ar yr adeg y gwnaeth y busnes ddod i ben, neu unrhyw fan arall yn y Ffurflen Dreth hon neu Ffurflen Dreth gynharach. Mae treuliau ôl-derfynu pob tro yn cael eu gosod yn erbyn derbyniadau ôl-derfynu yn gyntaf."
    override val p2 = Welsh.p2
    override val expensesSummaryHeading = Welsh.expensesSummaryHeading
    override val expensesSummaryP1 = Welsh.expensesSummaryP1
    override val expensesSummaryBullet1 = Welsh.expensesSummaryBullet1
    override val expensesSummaryBullet2 = Welsh.expensesSummaryBullet2
    override val expensesSummaryBullet3 = Welsh.expensesSummaryBullet3
    override val expensesSummaryBullet4 = Welsh.expensesSummaryBullet4
    override val liabilitiesSummaryHeading = Welsh.liabilitiesSummaryHeading
    override val liabilitiesSummaryP1 = "Os yw swm y rhwymedigaethau neu gostau sydd i’w nodi ac sy’n ymwneud â gweithredoedd anghyfreithlon gwirioneddol neu honedig a gyflawnwyd gan eich cleient mewn cyflogaeth flaenorol, yn fwy na chyfanswm ei incwm yn y flwyddyn, efallai y gall eich cleient hawlio’r swm gormodol drwy ei osod yn erbyn enillion cyfalaf. Mae rheolau arbennig sy’n cyfyngu ar y rhyddhad os na wnaeth eich cleient, er enghraifft, dalu am y costau hyn ei hun."
    override val lossSummaryHeading = Welsh.lossSummaryHeading
    override val lossSummaryP1 = Welsh.lossSummaryP1
    override val label = "Faint mae’ch cleient yn ei hawlio ar gyfer rhyddhad masnach ôl-derfynu a rhai colledion penodol eraill?"
    override val hint: String = "Er enghraifft, £193.54"

    override val amountEmpty = "Nodwch faint o ryddhad y mae’ch cleient yn ei hawlio"
    override val amountInvalid = Welsh.amountInvalid
    override val amountTooLarge = "Mae’n rhaid i swm rhyddhad eich cleient fod yn llai na £100,000,000,000"
    override val amountTooSmall = "Nodwch rif dilys am faint o ryddhad y mae’ch cleient yn ei hawlio"
  }
}
