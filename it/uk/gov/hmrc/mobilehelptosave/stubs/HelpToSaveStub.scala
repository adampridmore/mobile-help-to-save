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

package uk.gov.hmrc.mobilehelptosave.stubs

import java.time.LocalDate

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{status, _}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.Status
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mobilehelptosave.{AccountTestData, TransactionTestData}

object HelpToSaveStub extends AccountTestData with TransactionTestData {
  def currentUserIsEnrolled()(implicit wireMockServer:    WireMockServer): StubMapping = enrolmentStatusIs(status       = true)
  def currentUserIsNotEnrolled()(implicit wireMockServer: WireMockServer): StubMapping = enrolmentStatusIs(status       = false)
  def currentUserIsEligible()(implicit wireMockServer:    WireMockServer): StubMapping = eligibilityStatusIs(isEligible = true)
  def currentUserIsNotEligible()(implicit wireMockServer: WireMockServer): StubMapping = eligibilityStatusIs(isEligible = false)

  def enrolmentStatusShouldNotHaveBeenCalled()(implicit wireMockServer: WireMockServer): Unit =
    wireMockServer.verify(0, getRequestedFor(urlPathEqualTo("/help-to-save/enrolment-status")))

  def enrolmentStatusReturnsInternalServerError()(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(urlPathEqualTo("/help-to-save/enrolment-status"))
        .willReturn(aResponse()
          .withStatus(Status.INTERNAL_SERVER_ERROR)))

  private def enrolmentStatusIs(status: Boolean)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(urlPathEqualTo("/help-to-save/enrolment-status"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(
              s"""{"enrolled":$status}"""
            )))

  private def eligibilityStatusIs(isEligible: Boolean)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(urlPathEqualTo("/help-to-save/eligibility-check"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(s"""{
                         |"eligibilityCheckResult": {
                         |"result": "",
                         |"resultCode": ${if (isEligible) 1 else 2},
                         |"reason": "",
                         |"reasonCode": ${if (isEligible) 6 else 10}
                         |}
                 }""".stripMargin)))

  def transactionsExistForUser(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(urlPathEqualTo(s"/help-to-save/$nino/account/transactions"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(transactionsReturnedByHelpToSaveJsonString)))

  def zeroTransactionsExistForUser(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(urlPathEqualTo(s"/help-to-save/$nino/account/transactions"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(zeroTransactionsReturnedByHelpToSaveJsonString)))

  def transactionsWithOver50PoundDebit(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(urlPathEqualTo(s"/help-to-save/$nino/account/transactions"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(transactionsWithOver50PoundDebitReturnedByHelpToSaveJsonString)))

  def multipleTransactionsWithinSameMonthAndDay(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(urlPathEqualTo(s"/help-to-save/$nino/account/transactions"))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withBody(multipleTransactionsWithinSameMonthAndDayReturnedByHelpToSaveJsonString)))

  def userDoesNotHaveAnHtsAccount(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(urlPathEqualTo(s"/help-to-save/$nino/account/transactions"))
        .willReturn(aResponse()
          .withStatus(Status.NOT_FOUND)))

  private def getAccountUrlPathPattern(nino: Nino) =
    urlPathEqualTo(s"/help-to-save/$nino/account")

  def accountShouldNotHaveBeenCalled(nino: Nino)(implicit wireMockServer: WireMockServer): Unit =
    wireMockServer.verify(0, getRequestedFor(getAccountUrlPathPattern(nino)))

  def accountExists(balance: BigDecimal, nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(accountReturnedByHelpToSaveJsonString(balance))))

  def accountExistsSpecifyBonusTerms(
    balance:                   BigDecimal,
    nino:                      Nino,
    firstPeriodBonusEstimate:  BigDecimal,
    firstPeriodBonusPaid:      BigDecimal,
    firstPeriodEndDate:        LocalDate,
    secondPeriodBonusEstimate: BigDecimal,
    secondPeriodEndDate:       LocalDate,
    isClosed:                  Boolean = false)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(accountReturnedByHelpToSaveJsonString(
              balance,
              firstPeriodBonusEstimate,
              firstPeriodBonusPaid,
              firstPeriodEndDate,
              firstPeriodEndDate.plusDays(1),
              secondPeriodBonusEstimate,
              secondPeriodEndDate,
              secondPeriodEndDate.plusDays(1),
              isClosed
            ))))

  def accountExistsWithNoEmail(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(accountWithNoEmailReturnedByHelpToSaveJsonString)))

  def closedAccountExists(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(closedAccountReturnedByHelpToSaveJsonString)))

  def unspecifiedBlockedAccountExists(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(enrolledButUnspecifiedBlockedReturnedByHelpToSaveJsonString)))

  def paymentsBlockedAccountExists(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(enrolledButPaymentsBlockedReturnedByHelpToSaveJsonString)))

  def withdrawalsBlockedAccountExists(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(enrolledButWithdrawalsBlockedReturnedByHelpToSaveJsonString)))

  def bonusesBlockedAccountExists(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(enrolledButBonusesBlockedReturnedByHelpToSaveJsonString)))

  def accountReturnsInvalidJson(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(200)
          .withBody(accountReturnedByHelpToSaveInvalidJsonString)))

  def accountReturnsBadlyFormedJson(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody(
              """not JSON""".stripMargin
            )))

  def accountReturnsInternalServerError(nino: Nino)(implicit wireMockServer: WireMockServer): StubMapping =
    wireMockServer.stubFor(
      get(getAccountUrlPathPattern(nino))
        .withQueryParam("systemId", equalTo("MDTP-MOBILE"))
        .willReturn(aResponse()
          .withStatus(500)))
}
