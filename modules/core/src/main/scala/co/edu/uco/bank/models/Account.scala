package co.edu.uco.bank.models

final case class Account(id: AccountId, owner: CustomerId, balance: Money)
