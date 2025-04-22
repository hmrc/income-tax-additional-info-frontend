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

import play.api.libs.json.{Format, JsResult, JsString, JsValue}

sealed abstract class Journey(val name: String) {
  override def toString: String = name
}

object BusinessTaxReliefs extends Journey("businessTaxReliefs")

object Journey {

  def apply(name: String): Journey = name match {
    case BusinessTaxReliefs.name => BusinessTaxReliefs
    case invalid                 => throw new IllegalArgumentException("Invalid journey name supplied: " + invalid)
  }

  implicit val format: Format[Journey] = new Format[Journey] {
    override def writes(model: Journey): JsValue = JsString(model.name)
    override def reads(json: JsValue): JsResult[Journey] = json.validate[String].map(apply)
  }
}
