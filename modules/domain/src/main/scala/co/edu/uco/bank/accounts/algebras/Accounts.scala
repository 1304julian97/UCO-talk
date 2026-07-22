package co.edu.uco.bank.accounts.algebras

import cats.effect.IO

import co.edu.uco.bank.accounts.models.{Account, AccountId, CustomerId}
import co.edu.uco.bank.shared.{Currency, NonEmptyString}

trait Accounts:
  def open(owner: CustomerId, currency: Currency, name: Option[NonEmptyString]): IO[Account]
  def find(id: AccountId): IO[Option[Account]]
  def freeze(id: AccountId, reason: NonEmptyString): IO[Account]
  def close(id: AccountId): IO[Account]
