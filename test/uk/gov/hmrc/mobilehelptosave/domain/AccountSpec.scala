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

import org.joda.time.{LocalDate, YearMonth}
import org.scalatest.{Matchers, WordSpec}
import uk.gov.hmrc.mobilehelptosave.AccountTestData
import uk.gov.hmrc.mobilehelptosave.connectors.HelpToSaveBonusTerm

class AccountSpec extends WordSpec with Matchers
  with AccountTestData {

  private val accountOpenedInJan2018 = helpToSaveAccount.copy(
    openedYearMonth = new YearMonth(2018, 1),
    bonusTerms = Seq(
      HelpToSaveBonusTerm(
        bonusEstimate = BigDecimal("90.99"),
        bonusPaid = BigDecimal("90.99"),
        endDate = new LocalDate(2019, 12, 31),
        bonusPaidOnOrAfterDate = new LocalDate(2020, 1, 1)
      ),
      HelpToSaveBonusTerm(
        bonusEstimate = 12,
        bonusPaid = 0,
        endDate = new LocalDate(2021, 12, 31),
        bonusPaidOnOrAfterDate = new LocalDate(2022, 1, 1)
      )
    )
  )

  "apply" should {

    "include nextPaymentMonthStartDate when next month will start before the end of the the last bonus term" in {
      val penultimateMonthHelpToSaveAccount = accountOpenedInJan2018.copy(
        thisMonthEndDate = new LocalDate(2021, 11, 30)
      )

      val account = Account(penultimateMonthHelpToSaveAccount)
      account.nextPaymentMonthStartDate shouldBe Some(new LocalDate(2021, 12, 1))
    }

    "omit nextPaymentMonthStartDate when payments will not be possible next month because it will be after the last bonus term" in {
      val lastMonthHelpToSaveAccount = accountOpenedInJan2018.copy(
        thisMonthEndDate = new LocalDate(2021, 12, 31)
      )

      val account = Account(lastMonthHelpToSaveAccount)
      account.nextPaymentMonthStartDate shouldBe None
    }

    "return currentBonusTerm = *first* when current month is first month of *first* term" in {
      val firstMonthOfFirstTermHtSAccount = accountOpenedInJan2018.copy(
        thisMonthEndDate = new LocalDate(2018, 1, 31)
      )

      val account = Account(firstMonthOfFirstTermHtSAccount)
      account.currentBonusTerm shouldBe CurrentBonusTerm.First
    }

    "return currentBonusTerm = *first* when current month is last month of *first* term" in {
      val firstMonthOfFirstTermHtSAccount = accountOpenedInJan2018.copy(
        thisMonthEndDate = new LocalDate(2019, 12, 31)
      )

      val account = Account(firstMonthOfFirstTermHtSAccount)
      account.currentBonusTerm shouldBe CurrentBonusTerm.First
    }

    "return currentBonusTerm = *second* when current month is first month of *second* term" in {
      val firstMonthOfFirstTermHtSAccount = accountOpenedInJan2018.copy(
        thisMonthEndDate = new LocalDate(2020, 1, 31)
      )

      val account = Account(firstMonthOfFirstTermHtSAccount)
      account.currentBonusTerm shouldBe CurrentBonusTerm.Second
    }

    "return currentBonusTerm = *second* when current month is last month of *second* term" in {
      val firstMonthOfFirstTermHtSAccount = accountOpenedInJan2018.copy(
        thisMonthEndDate = new LocalDate(2021, 12, 31)
      )

      val account = Account(firstMonthOfFirstTermHtSAccount)
      account.currentBonusTerm shouldBe CurrentBonusTerm.Second
    }

    "return currentBonusTerm = *afterFinalTerm* when current month is after end of second term" in {
      val firstMonthOfFirstTermHtSAccount = accountOpenedInJan2018.copy(
        thisMonthEndDate = new LocalDate(2022, 1, 31)
      )

      val account = Account(firstMonthOfFirstTermHtSAccount)
      account.currentBonusTerm shouldBe CurrentBonusTerm.AfterFinalTerm
    }

    // balanceMustBeMoreThanForBonus is always 0 for the first term, we only include it for consistency with the second term
    "return balanceMustBeMoreThanForBonus = 0 for the first bonus term" in {
      val account = Account(accountOpenedInJan2018)
      account.bonusTerms.head.balanceMustBeMoreThanForBonus shouldBe 0
    }

    "calculate the second bonus term's balanceMustBeMoreThanForBonus from the first term's bonusEstimate" in {
      val account = Account(accountOpenedInJan2018)
      account.bonusTerms(1).balanceMustBeMoreThanForBonus shouldBe BigDecimal("181.98")
    }
  }
}
