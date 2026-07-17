package co.edu.uco.bank.dsl

import co.edu.uco.bank.models.{AccountId, Currency, CustomerId, Money}

enum Command:
  case Open(account: AccountId, owner: CustomerId, currency: Currency)
  case Deposit(account: AccountId, amount: Money)
  case Withdraw(account: AccountId, amount: Money)
  case Transfer(from: AccountId, to: AccountId, amount: Money)
  case Balance(account: AccountId)
