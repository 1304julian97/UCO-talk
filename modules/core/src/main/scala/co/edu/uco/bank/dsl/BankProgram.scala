package co.edu.uco.bank.dsl

import cats.data.Chain

import co.edu.uco.bank.models.{AccountId, Currency, CustomerId, Money}

final case class BankProgram(commands: Chain[Command]):
  def open(account: AccountId, owner: CustomerId, currency: Currency): BankProgram =
    add(Command.Open(account, owner, currency))

  def deposit(account: AccountId, amount: Money): BankProgram =
    add(Command.Deposit(account, amount))

  def withdraw(account: AccountId, amount: Money): BankProgram =
    add(Command.Withdraw(account, amount))

  def transfer(from: AccountId, to: AccountId, amount: Money): BankProgram =
    add(Command.Transfer(from, to, amount))

  def balance(account: AccountId): BankProgram =
    add(Command.Balance(account))

  private def add(command: Command): BankProgram =
    BankProgram(commands :+ command)
