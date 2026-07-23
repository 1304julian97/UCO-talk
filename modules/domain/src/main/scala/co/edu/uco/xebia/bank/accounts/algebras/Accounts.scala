package co.edu.uco.xebia.bank.accounts.algebras

import cats.effect.IO

import co.edu.uco.xebia.bank.accounts.models.{Account, AccountId, AccountName, CustomerId}
import co.edu.uco.xebia.bank.shared.{Currency, NonEmptyString}

trait Accounts:
  def open(owner: CustomerId, currency: Currency, name: AccountName): IO[Account]
  def find(id: AccountId): IO[Option[Account]]
  def freeze(id: AccountId, reason: NonEmptyString): IO[Account]
  def close(id: AccountId): IO[Account]
