/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.mobilehelptosave.domain

import play.api.libs.json.{Json, Writes}

object UserState extends Enumeration {
  val NotEnrolled, Enrolled = Value
}

case class UserDetails(
  state: UserState.Value,
  account: Option[Account],
  accountError: Option[ErrorInfo]
)

object UserDetails {
  implicit val writes: Writes[UserDetails] = Json.writes[UserDetails]
}
