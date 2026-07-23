package co.edu.uco.xebia.bank.payments.algebras

import cats.effect.IO

import co.edu.uco.xebia.bank.accounts.models.AccountId
import co.edu.uco.xebia.bank.payments.models.Transaction
import co.edu.uco.xebia.bank.shared.Money

trait Payments:
  def deposit(account: AccountId, amount: Money): IO[Transaction]
  def withdraw(account: AccountId, amount: Money): IO[Transaction]
  def transfer(from: AccountId, to: AccountId, amount: Money): IO[Transaction]
