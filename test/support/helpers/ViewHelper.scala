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

package support.helpers

import org.jsoup.nodes.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

trait ViewHelper {
  self: AnyWordSpec with Matchers =>

  private val serviceName = "Update and submit an Income Tax Return"
  private val serviceNameWelsh = "Diweddaru a chyflwyno Ffurflen Dreth Incwm"
  private val govUkExtension = "GOV.UK"
  private val ENGLISH = "English"
  private val WELSH = "Welsh"

  protected def welshTest(isWelsh: Boolean): String = if (isWelsh) "Welsh" else "English"

  protected def agentTest(isAgent: Boolean): String = if (isAgent) "Agent" else "Individual"

  def elementText(selector: String)(implicit document: Document): String = {
    document.select(selector).text()
  }

  def elementExist(selector: String)(implicit document: Document): Boolean = {
    !document.select(selector).isEmpty
  }

  def titleCheck(title: String, isWelsh: Boolean)(implicit document: Document): Unit = {
    s"has a title of $title" in {
      document.title() shouldBe s"$title - ${if (isWelsh) serviceNameWelsh else serviceName} - $govUkExtension"
    }
  }

  def hintTextCheck(text: String, selector: String = ".govuk-hint")(implicit document: Document): Unit = {
    s"has the hint text of '$text'" in {
      elementText(selector) shouldBe text
    }
  }

  def h1Check(header: String, size: String = "l")(implicit document: Document): Unit = {
    s"have a page heading of '$header'" in {
      val headingAndCaption = document.select(s"h1.govuk-heading-$size").text()
      val caption = document.select(s"h1 > span.govuk-caption-$size").text()
      headingAndCaption.replace(caption, "").trim shouldBe header
    }
  }

  def captionCheck(caption: String, selector: String = ".govuk-caption-l")(implicit document: Document): Unit = {
    s"have the caption of '$caption'" in {
      document.select(selector).text() shouldBe caption
    }
  }

  def textOnPageCheck(text: String, selector: String, additionalTestText: String = "")(implicit document: Document): Unit = {
    s"have text on the screen of '$text' $additionalTestText" in {
      document.select(selector).text() should include(text)
    }
  }

  def elementNotOnPageCheck(selector: String)(implicit document: Document): Unit = {
    s"not have the page element for selector '$selector'" in {
      document.select(selector).isEmpty shouldBe true
    }
  }

  def changeAmountRowCheck(item: String, value: String, itemSelector: String, valueSelector: String, changeSelector: String,
                           changeHiddenText: String, href: String)
                          (implicit document: Document): Unit = {
    textOnPageCheck(item, itemSelector)
    textOnPageCheck(value, valueSelector, s"for the value of the $item field")
    linkCheck(changeHiddenText, changeSelector, href)
  }

  def formGetLinkCheck(text: String, selector: String)(implicit document: Document): Unit = {
    s"have a form with a GET action of '$text'" in {
      document.select(selector).attr("action") shouldBe text
      document.select(selector).attr("method") shouldBe "GET"
    }
  }

  def formPostLinkCheck(text: String, selector: String)(implicit document: Document): Unit = {
    s"have a form with a POST action of '$text'" in {
      document.select(selector).attr("action") shouldBe text
      document.select(selector).attr("method") shouldBe "POST"
    }
  }

  def buttonCheck(text: String, selector: String = ".govuk-button", href: Option[String] = None)(implicit document: Document): Unit = {
    s"have a $text button" which {
      s"has the text '$text'" in {
        document.select(selector).text() shouldBe text
      }
      s"has a class of govuk-button" in {
        document.select(selector).attr("class") should include("govuk-button")
      }

      if (href.isDefined) {
        s"has a href to '${href.get}'" in {
          document.select(selector).attr("href") shouldBe href.get
        }
      }
    }
  }

  def radioButtonCheck(text: String, radioNumber: Int, checked: Boolean)(implicit document: Document): Unit = {
    s"have a $text radio button" which {
      s"is of type radio button" in {
        val selector = ".govuk-radios__item > input"
        document.select(selector).get(radioNumber - 1).attr("type") shouldBe "radio"
      }
      s"has the text $text" in {
        val selector = ".govuk-radios__item > label"
        document.select(selector).get(radioNumber - 1).text() shouldBe text
      }
      s"has the checked value set to $checked" in {
        val selector = ".govuk-radios__item > input"
        document.select(selector).get(radioNumber - 1).hasAttr("checked") shouldBe checked
      }
    }
  }

  def amountBoxLabelCheck(text: String)(implicit document: Document): Unit = {
    s"has the text $text in the amount box label" in {
      val selector = "#main-content > div > div > form > div > label"
      document.select(selector).text() shouldBe text
    }
  }

  def amountBoxHintCheck(text: String)(implicit document: Document): Unit = {
    s"has the text $text in the amount box hint" in {
      val selector = "#amount-hint"
      document.select(selector).text() shouldBe text
    }
  }

  def linkCheck(text: String, selector: String, href: String, hiddenTextSelector: Option[String] = None,
                isExactUrlMatch: Boolean = true, additionalTestText: String = "")(implicit document: Document): Unit = {
    s"have a $text link $additionalTestText" which {
      s"has the text '$text' and a href to '$href'" in {
        if (hiddenTextSelector.isDefined) {
          document.select(hiddenTextSelector.get).text() shouldBe text.split(" ").drop(1).mkString(" ")
        }

        document.select(selector).text() shouldBe text
        if (isExactUrlMatch) {
          document.select(selector).attr("href") shouldBe href
        } else {
          val str = document.select(selector).attr("href")
          str.contains(href) shouldBe true
        }
      }
    }
  }

  def inputFieldValueCheck(name: String, selector: String, value: String)(implicit document: Document): Unit = {
    s"'$selector' has a name of '$name'" in {
      document.select(selector).attr("name") shouldBe name
    }
    s"'$selector' has a value of '$value'" in {
      document.select(selector).attr("value") shouldBe value
    }
  }

  def errorSummaryCheck(text: String, href: String)(implicit document: Document): Unit = {
    "contains an error summary" in {
      elementExist(".govuk-error-summary")
    }
    "contains the text 'There is a problem'" in {
      document.select(".govuk-error-summary__title").text() should (be("There is a problem") or be("Mae problem wedi codi"))
    }
    s"has a $text error in the error summary" which {
      s"has the text '$text'" in {
        document.select(".govuk-error-summary__body").text() shouldBe text
      }
      s"has a href to '$href'" in {
        document.select(".govuk-error-summary__body > ul > li > a").attr("href") shouldBe href
      }
    }
  }

  def multipleErrorCheck(errors: List[(String, String)], isWelsh: Boolean)(implicit document: Document): Unit = {

    "contains an error summary" in {
      elementExist(".govuk-error-summary")
    }
    "contains the text 'There is a problem'" in {
      if (isWelsh) {
        document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
      } else {
        document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
      }
    }

    for (error <- errors) {
      val index = errors.indexOf(error) + 1
      val selector = s".govuk-error-summary__body > ul > li:nth-child($index) > a"

      s"has a ${error._1} error in the error summary" which {
        s"has the text '${error._1}'" in {
          document.select(selector).text() shouldBe error._1
        }
        s"has a href to '${error._2}'" in {
          document.select(selector).attr("href") shouldBe error._2
        }
      }
    }
  }

  def errorAboveElementCheck(text: String, isWelsh: Boolean, id: Option[String] = None)(implicit document: Document): Unit = {
    s"has a $text error above the element" which {
      s"has the text '$text'" in {
        val selector = if (id.isDefined) s"#${id.get}-error" else ".govuk-error-message"
        if (isWelsh) {
          document.select(selector).text() shouldBe s"Gwall: $text"
        } else {
          document.select(selector).text() shouldBe s"Error: $text"
        }
      }
    }
  }

  def checkMessagesAreUnique(initial: List[(String, String)], keysToExplore: List[(String, String)], result: Set[String]): Set[String] = {
    keysToExplore match {
      case Nil => result
      case head :: tail =>
        val (currentMessageKey, currentMessage) = (head._1, head._2)
        val duplicate = initial.collect {
          case (messageKey, message) if currentMessageKey != messageKey && currentMessage == message => currentMessageKey
        }.toSet

        checkMessagesAreUnique(initial, tail, duplicate ++ result)
    }
  }

  def welshToggleCheck(isWelsh: Boolean)(implicit document: Document): Unit = {
    welshToggleCheck(if (isWelsh) WELSH else ENGLISH)
  }

  def welshToggleCheck(activeLanguage: String)(implicit document: Document): Unit = {
    val otherLanguage = if (activeLanguage == "English") "Welsh" else "English"

    def selector = Map("English" -> 0, "Welsh" -> 1)

    def linkLanguage = Map("English" -> "English", "Welsh" -> "Cymraeg")

    def linkText = Map("English" -> "Change the language to English English",
      "Welsh" -> "Newid yr iaith ir Gymraeg Cymraeg")

    s"have the language toggle already set to $activeLanguage" which {
      s"has the text '$activeLanguage" in {
        document.select(".hmrc-language-select__list-item").get(selector(activeLanguage)).text() shouldBe linkLanguage(activeLanguage)
      }
    }
    s"has a link to change the language to $otherLanguage" which {
      s"has the text '${linkText(otherLanguage)}" in {
        document.select(".hmrc-language-select__list-item").get(selector(otherLanguage)).text() shouldBe linkText(otherLanguage)
      }
      s"has a link to change the language" in {
        document.select(".hmrc-language-select__list-item > a").attr("href") shouldBe
          s"/update-and-submit-income-tax-return/additional-information/language/${linkLanguage(otherLanguage).toLowerCase}"
      }
    }
  }
}
