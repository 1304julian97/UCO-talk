package co.edu.uco.xebia.bank.accounts.algebras

import cats.effect.IO

import co.edu.uco.xebia.bank.accounts.models.*
import co.edu.uco.xebia.bank.persistence.memory.BlockingInMemoryBankPersistence
import co.edu.uco.xebia.bank.shared.*

trait Accounts:
  def open(owner: CustomerId, currency: Currency, name: AccountName): IO[Account]
  def find(id: AccountId): IO[Option[Account]]
  def freeze(id: AccountId, reason: NonEmptyString): IO[Account]
  def close(id: AccountId): IO[Account]

object Accounts:
  def make(persistence: BlockingInMemoryBankPersistence): Accounts =
    new Accounts:
      override def open(owner: CustomerId, currency: Currency, name: AccountName): IO[Account] =
        IO.randomUUID.map(AccountId.apply).flatMap { id =>
          val account = Account(
            id = id,
            owner = owner,
            name = name,
            balance = Money(
              amount = MoneyAmount.zero,
              currency = currency
            ),
            status = AccountStatus.Active
          )
          persistence.apply(key = id).setValue(account).as(account)
        }

      override def find(id: AccountId): IO[Option[Account]] =
        persistence.apply(key = id).get

      override def freeze(id: AccountId, reason: NonEmptyString): IO[Account] =
        modifyAccountIfExists(id) { account =>
          account.status match
            case AccountStatus.Active =>
              val frozenAccount = account.copy(status = AccountStatus.Frozen(reason))
              Right(frozenAccount)

            case AccountStatus.Frozen(_) =>
              Left(AccountError.AccountAlreadyFrozen(id))

            case AccountStatus.Closed(_) =>
              Left(AccountError.AccountAlreadyClosed(id))
        }

      override def close(id: AccountId): IO[Account] =
        IO.realTimeInstant.flatMap { now =>
          modifyAccountIfExists(id) { account =>
            account.status match
              case AccountStatus.Active | AccountStatus.Frozen(_) =>
                val closedAccount = account.copy(status = AccountStatus.Closed(closedAt = now))
                Right(closedAccount)

              case AccountStatus.Closed(_) =>
                Left(AccountError.AccountAlreadyClosed(id))
          }
        }

      private def modifyAccountIfExists(
          id: AccountId
      )(
          f: Account => Either[AccountError, Account]
      ): IO[Account] =
        persistence
          .apply(key = id)
          .evalModifyValueIfSet { account =>
            f(account) match
              case Left(accountError) =>
                IO.raiseError(accountError)

              case Right(account) =>
                IO.pure(account -> account)
          }
          .flatMap {
            case Some(account) =>
              IO.pure(account)

            case None =>
              IO.raiseError(AccountError.AccountNotFound(id))
          }
