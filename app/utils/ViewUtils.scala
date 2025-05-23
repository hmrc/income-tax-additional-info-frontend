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

import models.requests.JourneyDataRequest
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._

import java.time.LocalDate
import scala.util.Try

object ViewUtils {

  def summaryListRow(key: HtmlContent,
                     value: HtmlContent,
                     keyClasses: String = "",
                     valueClasses: String = "",
                     actionClasses: String = "",
                     actions: Seq[(Call, String, Option[String])]): SummaryListRow = {
    SummaryListRow(
      key = Key(
        content = key,
        classes = keyClasses
      ),
      value = Value(
        content = value,
        classes = valueClasses
      ),
      actions = Some(Actions(
        items = actions.map { case (call, linkText, visuallyHiddenText) => ActionItem(
          href = call.url,
          content = ariaHiddenChangeLink(linkText),
          visuallyHiddenText = visuallyHiddenText
        )
        },
        classes = actionClasses
      ))
    )
  }

  def ariaHiddenChangeLink(linkText: String): HtmlContent = {
    HtmlContent(
      s"""<span aria-hidden="true">$linkText</span>"""
    )
  }
  def bigDecimalCurrency(value: String, currencySymbol: String = "£"): String =
    Try(BigDecimal(value))
      .map(amount => currencySymbol + f"$amount%1.2f".replace(".00", ""))
      .getOrElse(value)
      .replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",")

  def translatedDateFormatter(date: LocalDate)(implicit messages: Messages): String = {
    val translatedMonth = messages("common." + date.getMonth.toString.toLowerCase)
    s"${date.getDayOfMonth} $translatedMonth ${date.getYear}"
  }

  def dynamicMessage(key: String, args: String*)(implicit messages: Messages, request: JourneyDataRequest[_]): String =
    messages(key + (if(request.user.isAgent) ".agent" else ".individual"), args: _*)
}
