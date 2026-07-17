package co.edu.uco.bank.dsl

import cats.data.Chain

import co.edu.uco.bank.models.{AccountId, Currency, CustomerId}

object Bank:
  def open(account: AccountId, owner: CustomerId, currency: Currency): BankProgram =
    BankProgram(Chain.one(Command.Open(account, owner, currency)))
