package co.edu.uco.bank.interpreter

import cats.syntax.all.*

import co.edu.uco.bank.dsl.{BankProgram, Command}
import co.edu.uco.bank.models.*

object Interpreter:
  def run(program: BankProgram): Either[BankError, BankResult] =
    program.commands.foldM(BankResult.empty)(step)

  private def step(result: BankResult, command: Command): Either[BankError, BankResult] =
    command match
      case Command.Open(account, owner, currency) =>
        ensureAbsent(result.state, account).map { _ =>
          val opened = Account(account, owner, Money(BigDecimal(0), currency))
          result.copy(state = result.state.updated(opened))
        }

      case Command.Deposit(account, amount) =>
        for
          existing <- lookup(result.state, account)
          _ <- ensurePositive(amount)
          _ <- ensureSameCurrency(existing, amount)
        yield result.copy(state = result.state.updated(credit(existing, amount)))

      case Command.Withdraw(account, amount) =>
        for
          existing <- lookup(result.state, account)
          _ <- ensurePositive(amount)
          _ <- ensureSameCurrency(existing, amount)
          _ <- ensureSufficient(existing, amount)
        yield result.copy(state = result.state.updated(debit(existing, amount)))

      case Command.Transfer(from, to, amount) =>
        for
          _ <- lookup(result.state, from)
          _ <- Either.cond(from != to, (), BankError.SameAccountTransfer(from))
          debited <- step(result, Command.Withdraw(from, amount))
          credited <- step(debited, Command.Deposit(to, amount))
        yield credited

      case Command.Balance(account) =>
        lookup(result.state, account).map { existing =>
          result.copy(balances = result.balances :+ (account -> existing.balance))
        }

  private def lookup(state: BankState, account: AccountId): Either[BankError, Account] =
    state.get(account).toRight(BankError.UnknownAccount(account))

  private def ensureAbsent(state: BankState, account: AccountId): Either[BankError, Unit] =
    Either.cond(state.get(account).isEmpty, (), BankError.AccountAlreadyExists(account))

  private def ensurePositive(amount: Money): Either[BankError, Unit] =
    Either.cond(amount.amount > 0, (), BankError.InvalidAmount(amount))

  private def ensureSameCurrency(account: Account, amount: Money): Either[BankError, Unit] =
    Either.cond(
      account.balance.currency == amount.currency,
      (),
      BankError.CurrencyMismatch(account.id, account.balance.currency, amount.currency)
    )

  private def ensureSufficient(account: Account, amount: Money): Either[BankError, Unit] =
    Either.cond(
      account.balance.amount >= amount.amount,
      (),
      BankError.InsufficientFunds(account.id, amount, account.balance)
    )

  private def credit(account: Account, amount: Money): Account =
    account.copy(balance = account.balance.copy(amount = account.balance.amount + amount.amount))

  private def debit(account: Account, amount: Money): Account =
    account.copy(balance = account.balance.copy(amount = account.balance.amount - amount.amount))
