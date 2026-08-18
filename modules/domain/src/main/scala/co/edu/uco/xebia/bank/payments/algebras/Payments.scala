package co.edu.uco.xebia.bank.payments.algebras

import cats.effect.IO
import cats.effect.Outcome

import co.edu.uco.xebia.bank.accounts.models.*
import co.edu.uco.xebia.bank.payments.models.*
import co.edu.uco.xebia.bank.persistence.memory.BlockingInMemoryBankPersistence
import co.edu.uco.xebia.bank.shared.Money

trait Payments:
  def deposit(account: AccountId, amount: Money, conversion: ConversionPolicy): IO[Transaction]
  def withdraw(account: AccountId, amount: Money, conversion: ConversionPolicy): IO[Transaction]
  def transfer(from: AccountId, to: AccountId, amount: Money, conversion: ConversionPolicy): IO[Transaction]

object Payments:
  def make(
      calculator: MoneyCalculator,
      persistence: BlockingInMemoryBankPersistence
  ): Payments =
    new Payments:
      override def deposit(
          account: AccountId,
          amount: Money,
          conversion: ConversionPolicy
      ): IO[Transaction] =
        modifyAccountBalance(accountId = account) { accountBalance =>
          calculator
            .add(accountBalance, amount, conversion)
            .adaptError { case MoneyError.IncompatibleCurrencies(x, y) =>
              PaymentError.CurrencyMismatch(account, expected = x, actual = y)
            }
        }.map { transactionId =>
          Transaction.Deposit(
            id = transactionId,
            account = account,
            amount = amount
          )
        }

      override def withdraw(
          account: AccountId,
          amount: Money,
          conversion: ConversionPolicy
      ): IO[Transaction] =
        modifyAccountBalance(accountId = account) { accountBalance =>
          calculator
            .subtract(accountBalance, amount, conversion)
            .adaptError {
              case MoneyError.IncompatibleCurrencies(x, y) =>
                PaymentError.CurrencyMismatch(account, expected = x, actual = y)

              case MoneyError.NegativeResult(_, _) =>
                PaymentError.InsufficientFunds(account, requested = amount, available = accountBalance)
            }
        }.map { transactionId =>
          Transaction.Withdrawal(
            id = transactionId,
            account = account,
            amount = amount
          )
        }

      private def modifyAccountBalance(
          accountId: AccountId
      )(
          f: Money => IO[Money]
      ): IO[TransactionId] =
        persistence
          .apply(key = accountId)
          .evalModifyValueIfSet { account =>
            f(account.balance).map { newBalance =>
              account.copy(balance = newBalance) -> ()
            }
          }
          .flatMap {
            case Some(_) =>
              IO.randomUUID.map(TransactionId.apply)

            case None =>
              IO.raiseError(PaymentError.AccountDoesNotExist(accountId))
          }

      override def transfer(
          from: AccountId,
          to: AccountId,
          amount: Money,
          conversion: ConversionPolicy
      ): IO[Transaction] =
        if (from == to) then
          IO.raiseError(
            PaymentError.SameAccountTransfer(account = from)
          )
        else
          withdraw(account = from, amount, conversion) >>
            deposit(account = to, amount, conversion).guaranteeCase {
              case Outcome.Succeeded(_) =>
                IO.unit

              case Outcome.Errored(_) | Outcome.Canceled() =>
                deposit(account = from, amount, conversion).void
            } >> IO.randomUUID.map { transactionId =>
              Transaction.Transfer(
                id = TransactionId(transactionId),
                from = from,
                to = to,
                amount = amount
              )
            }
