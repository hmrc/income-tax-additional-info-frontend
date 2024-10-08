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

package models.mongo

import play.api.libs.json.{Json, OFormat}

trait DatabaseError {
  val message: String
}

case object DataNotUpdated extends DatabaseError {
  override val message: String = "User data was not updated due to mongo exception"
}
case object DataNotFound extends DatabaseError {
  override val message: String = "User data could not be found due to mongo exception"
}
case class EncryptionDecryptionError(error: String) extends DatabaseError {
  override val message: String = s"Encryption / Decryption exception occurred. Exception: $error"
}

case class MongoError(error: String) extends DatabaseError {
  override val message: String = s"Mongo exception occurred. Exception: $error"
}

object MongoError {
  implicit val formats: OFormat[MongoError] = Json.format[MongoError]
}
