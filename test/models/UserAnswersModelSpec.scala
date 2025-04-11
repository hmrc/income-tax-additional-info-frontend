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

package models

import org.scalamock.scalatest.MockFactory
import pages.QuestionPage
import play.api.libs.json._
import support.UnitTest
import support.utils.TaxYearUtils.taxYear


class UserAnswersModelSpec extends UnitTest with MockFactory {

  case class TestPage(jsPath: JsPath = JsPath) extends QuestionPage[String] {
    override val toString: String = "TestPage"
    override val path: JsPath = jsPath \ toString
  }

  val userAnswers: UserAnswersModel = emptyUserAnswers(taxYear, BusinessTaxReliefs)

  "UserAnswers" when {

    "calling .set(page)" when {

      "no data exists for that page" should {

        "set the answer" in {
          userAnswers.set(TestPage(), "foo") shouldBe userAnswers.copy(data = Json.obj(
            "TestPage" -> "foo"
          ))
        }
      }

      "data exists for that page" should {

        "change the answer" in {
          val withData = userAnswers.copy(data = Json.obj(
            "TestPage" -> "foo"
          ))
          withData.set(TestPage(), "bar") shouldBe userAnswers.copy(data = Json.obj(
            "TestPage" -> "bar"
          ))
        }
      }

      "setting at a subPath with indexes" should {

        "store the answer at the subPath" in {
          val result =
            userAnswers
              .set(TestPage(__ \ "items" \ 0), "foo")
              .set(TestPage(__ \ "items" \ 1), "bar")
              .set(TestPage(__ \ "items" \ 2), "wizz")


          result.data shouldBe Json.obj(
            "items" -> Json.arr(
              Json.obj("TestPage" -> "foo"),
              Json.obj("TestPage" -> "bar"),
              Json.obj("TestPage" -> "wizz")
            )
          )
        }
      }

      "setting at a subPath which contains nested indexes" should {

        "store the answer at the subPath" in {
          val result =
            userAnswers
              .set(TestPage(__ \ "items" \ 0 \ "subItems" \ 0), "foo")
              .set(TestPage(__ \ "items" \ 0 \ "subItems" \ 1), "bar")
              .set(TestPage(__ \ "items" \ 0 \ "subItems" \ 2), "wizz")

          result.data shouldBe Json.obj(
            "items" -> Json.arr(
              Json.obj(
                "subItems" -> Json.arr(
                  Json.obj("TestPage" -> "foo"),
                  Json.obj("TestPage" -> "bar"),
                  Json.obj("TestPage" -> "wizz")
                )
              )
            )
          )
        }
      }
    }

    "calling .get(page)" when {

      "no data exists for that page" should {

        "return None" in {
          userAnswers.get(TestPage()) shouldBe None
        }
      }

      "data exists for that page" should {

        "return Some(data)" in {
          val withData = userAnswers.copy(data = Json.obj(
            "TestPage" -> "foo"
          ))
          withData.get(TestPage()) shouldBe Some("foo")
        }
      }

      "getting data at a subPath with indexes" should {

        "return the answer at the subPath" in {

          val withData = userAnswers.copy(data = Json.obj(
            "items" -> Json.arr(
              Json.obj("TestPage" -> "foo"),
              Json.obj("TestPage" -> "bar"),
              Json.obj("TestPage" -> "wizz")
            )
          ))
          withData.get(TestPage(__ \ "items" \ 0)) shouldBe Some("foo")
        }
      }

      "getting data at a subPath which contains nested indexes" should {

        "must get the answer at the subPath" in {
          val withData = userAnswers.copy(data = Json.obj(
            "items" -> Json.arr(
              Json.obj(
                "subItems" -> Json.arr(
                  Json.obj("TestPage" -> "foo"),
                  Json.obj("TestPage" -> "bar"),
                  Json.obj("TestPage" -> "wizz")
                )
              )
            )
          ))
          withData.get(TestPage(__ \ "items" \ 0 \ "subItems" \ 0)) shouldBe Some("foo")
        }
      }
    }

    "calling .remove(page)" when {

      "no data exists for that page" should {

        "return the answers unchanged" in {
          val withData = userAnswers.copy(data = Json.obj(
            "AnotherPage" -> "foo"
          ))
          withData.remove(TestPage()) shouldBe withData
        }
      }

      "data exists for that page" should {

        "remove the answer" in {
          val withData = userAnswers.copy(data = Json.obj(
            "TestPage" -> "foo"
          ))
          withData.remove(TestPage()) shouldBe userAnswers
        }
      }

      "removing data at a subPath with indexes" should {

        "the page is the last page in the subObject" should {

          "remove the entire object from the array at the subPath" in {

            val withData = userAnswers.copy(data = Json.obj(
              "items" -> Json.arr(
                Json.obj("TestPage" -> "foo"),
                Json.obj("TestPage" -> "bar"),
                Json.obj("TestPage" -> "wizz")
              )
            ))
            val result = withData.remove(TestPage(__ \ "items" \ 1))
            result.data shouldBe Json.obj(
              "items" -> Json.arr(
                Json.obj("TestPage" -> "foo"),
                Json.obj("TestPage" -> "wizz")
              )
            )
          }
        }

        "the page is NOT the last page in the subObject" should {

          "remove just that page object key from the object in the array" in {

            val withData = userAnswers.copy(data = Json.obj(
              "items" -> Json.arr(
                Json.obj("TestPage" -> "foo"),
                Json.obj(
                  "TestPage" -> "bar",
                  "TestPage2" -> "bar2"
                ),
                Json.obj("TestPage" -> "wizz")
              )
            ))
            val result = withData.remove(TestPage(__ \ "items" \ 1))
            result.data shouldBe Json.obj(
              "items" -> Json.arr(
                Json.obj("TestPage" -> "foo"),
                Json.obj("TestPage2" -> "bar2"),
                Json.obj("TestPage" -> "wizz")
              )
            )
          }
        }
      }

      "removing at a subPath which contains nested indexes" when {

        "the page is that last item in the arrays object" should {

          "remove the object containing the answer from the array at the subPath" in {
            val withData = userAnswers.copy(data = Json.obj(
              "items" -> Json.arr(
                Json.obj(
                  "subItems" -> Json.arr(
                    Json.obj("TestPage" -> "foo"),
                    Json.obj("TestPage" -> "bar"),
                    Json.obj("TestPage" -> "wizz")
                  )
                )
              )
            ))
            val result = withData.remove(TestPage(__ \ "items" \ 0 \ "subItems" \ 1))
            result.data shouldBe Json.obj(
              "items" -> Json.arr(
                Json.obj(
                  "subItems" -> Json.arr(
                    Json.obj("TestPage" -> "foo"),
                    Json.obj("TestPage" -> "wizz")
                  )
                )
              )
            )
          }
        }

        "the page is NOT the last item in the arrays object" should {

          "remove just that key from the object at the subPath" in {
            val withData = userAnswers.copy(data = Json.obj(
              "items" -> Json.arr(
                Json.obj(
                  "subItems" -> Json.arr(
                    Json.obj("TestPage" -> "foo"),
                    Json.obj(
                      "TestPage" -> "bar",
                      "TestPage2" -> "bar2"
                    ),
                    Json.obj("TestPage" -> "wizz")
                  )
                )
              )
            ))
            val result = withData.remove(TestPage(__ \ "items" \ 0 \ "subItems" \ 1))
            result.data shouldBe Json.obj(
              "items" -> Json.arr(
                Json.obj(
                  "subItems" -> Json.arr(
                    Json.obj("TestPage" -> "foo"),
                    Json.obj(
                      "TestPage2" -> "bar2"
                    ),
                    Json.obj("TestPage" -> "wizz")
                  )
                )
              )
            )
          }
        }
      }
    }

    "when calling .handleResult" should {

      "when failed to update the UserAnswers" should {

        "must throw the exception" in {
          intercept[JsResultException](userAnswers.handleResult(JsError("OhNo")))
        }
      }

      "when updated UserAnswers successfully" should {

        "must return the user answers" in {
          userAnswers.handleResult(JsSuccess(userAnswers.data)) shouldBe userAnswers
        }
      }
    }
  }
}
