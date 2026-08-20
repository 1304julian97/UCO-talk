package co.edu.uco.xebia.bank.routes

import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import co.edu.uco.xebia.bank.accounts.algebras.Accounts
import co.edu.uco.xebia.bank.accounts.models.*
import co.edu.uco.xebia.bank.accounts.models.AccountStatus.Active
import co.edu.uco.xebia.bank.shared.Currency.COP
import co.edu.uco.xebia.bank.shared.{Currency, Money, MoneyAmount, NonEmptyString}

import java.util.UUID

class DummyAccountImpl extends Accounts {

  private def accountGenerator: IO[Account] = IO.fromOption(
    AccountName
      .from("My First Car")
      .toOption
      .map(name =>
        Account(
          AccountId(UUID.randomUUID()),
          CustomerId(UUID.randomUUID()),
          name,
          Money(MoneyAmount(BigDecimal(100)), COP),
          Active
        )
      )
  )(new Exception("Account no valid"))

  override def open(owner: CustomerId, currency: Currency, name: AccountName): IO[Account] = accountGenerator

  override def find(id: AccountId): IO[Option[Account]] = accountGenerator.map(_.some)

  override def freeze(id: AccountId, reason: NonEmptyString): IO[Account] = accountGenerator

  override def close(id: AccountId): IO[Account] = accountGenerator
}
