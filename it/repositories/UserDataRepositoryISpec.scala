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

package repositories

import models.User
import models.gains.GainsCyaModel
import models.mongo._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.InsertOneResult
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.AnyContent
import play.api.test.{DefaultAwaitTimeout, FakeRequest, FutureAwaits}
import support.IntegrationTest
import uk.gov.hmrc.mongo.play.json.Codecs.toBson

class UserDataRepositoryISpec extends IntegrationTest with FutureAwaits with DefaultAwaitTimeout {

  val gainsRepo: GainsUserDataRepository = app.injector.instanceOf[GainsUserDataRepository]

  val gainsInvalidRepo: GainsUserDataRepository = appWithInvalidEncryptionKey.injector.instanceOf[GainsUserDataRepository]

  private def count: Long = await(gainsRepo.collection.countDocuments().toFuture())

  class EmptyDatabase {
    await(gainsRepo.collection.drop().toFuture())
    await(gainsRepo.ensureIndexes)
  }

  val gainsUserData: GainsUserDataModel = GainsUserDataModel(
    sessionId,
    mtditid,
    nino,
    taxYear,
    Some(completeGainsCyaModel)
  )

  implicit val request: FakeRequest[AnyContent] = FakeRequest()

  "create" should {
    "add a document to the collection" in new EmptyDatabase {
      count mustBe 0
      val result: Either[DatabaseError, Boolean] = await(gainsRepo.create(gainsUserData))
      result mustBe Right(true)
      count mustBe 1
    }
    "fail to add a document to the collection when it already exists" in new EmptyDatabase {
      count mustBe 0
      await(gainsRepo.create(gainsUserData))
      val result: Either[DatabaseError, Boolean] = await(gainsRepo.create(gainsUserData))
      result mustBe Left(DataNotUpdated)
      count mustBe 1
    }
  }

  "update" should {

    "update a document in the collection" in new EmptyDatabase {
      val testUser: User = User(
        mtditid, None, nino, "individual", sessionId
      )

      val initialData: GainsUserDataModel = GainsUserDataModel(
        testUser.sessionId, testUser.mtditid, testUser.nino, taxYear,
        Some(completeGainsCyaModel)
      )

      val newGainsCyaModel: GainsCyaModel = GainsCyaModel(
        Some(true), Some("123"), Some("cause"), Some(true), Some("5"), Some(321.11), Some("5"), Some(true), Some(321.11), Some(true), Some(321.11)
      )

      val newUserData: GainsUserDataModel = initialData.copy(
        gains = Some(newGainsCyaModel)
      )

      await(gainsRepo.create(initialData))
      count mustBe 1

      val res: Boolean = await(gainsRepo.update(newUserData).map {
        case Right(value) => value
        case Left(value) => false
      })
      res mustBe true
      count mustBe 1

      val data: Option[GainsUserDataModel] = await(gainsRepo.find(taxYear)(testUser).map {
        case Right(value) => value
        case Left(value) => None
      })

      data.get.gains.get.howMuchGain.get shouldBe 321.11
      data.get.gains.get.taxPaid.get shouldBe 321.11
    }

    "return a leftDataNotUpdated if the document cannot be found" in {
      val newUserData = gainsUserData.copy(sessionId = "sessionId-000001")
      count mustBe 1
      val res = await(gainsRepo.update(newUserData))
      res mustBe Left(DataNotUpdated)
      count mustBe 1
    }
  }

  "find" should {
    def filter(sessionId: String, mtdItId: String, nino: String, taxYear: Int): Bson = Filters.and(
      Filters.equal("sessionId", toBson(sessionId)),
      Filters.equal("mtdItId", toBson(mtdItId)),
      Filters.equal("nino", toBson(nino)),
      Filters.equal("taxYear", toBson(taxYear))
    )

    val testUser = User(
      mtditid, None, nino, "individual", sessionId
    )

    val newGainsCyaModel: GainsCyaModel = GainsCyaModel(
      Some(true), Some("123"), Some("cause"), Some(true), Some("5"), Some(321.11), Some("5"), Some(true), Some(321.11), Some(true), Some(321.11)
    )

    "get a document" in {
      count mustBe 1
      val dataAfter: Option[GainsUserDataModel] = await(gainsRepo.find(taxYear)(testUser).map {
        case Right(value) => value
        case Left(value) => None
      })

      dataAfter.get.gains mustBe Some(newGainsCyaModel)
    }

    "return a EncryptionDecryptionError" in {
      await(gainsInvalidRepo.find(taxYear)(testUser)) mustBe
        Left(EncryptionDecryptionError("Key being used is not valid. It could be due to invalid encoding, wrong length or uninitialized for decrypt Invalid AES key length: 2 bytes"))
    }

    "return a No CYA data found" in {
      await(gainsRepo.find(taxYear)(testUser.copy(sessionId = "invalid"))) mustBe Right(None)
    }
  }

  "the set indexes" should {

    "enforce uniqueness" in {
      val result: Either[Exception, InsertOneResult] = try {
        Right(await(gainsRepo.collection.insertOne(EncryptedGainsUserDataModel(
          sessionId, mtditid, nino, taxYear
        )).toFuture()))
      } catch {
        case e: Exception => Left(e)
      }
      result.isLeft mustBe true
      result.left.get.getMessage must include(
        "E11000 duplicate key error collection: income-tax-additional-info-frontend.gainsUserData")
    }
  }

  "clear" should {

    "clear the document for the current user" in {
      count shouldBe 1
      await(gainsRepo.create(GainsUserDataModel(sessionId, "7788990066", nino, taxYear)))
      count shouldBe 2
      await(gainsRepo.clear(taxYear))
      count shouldBe 1
    }
  }
}
