package co.edu.uco.xebia.bank.accounts.models

import java.time.Instant

import co.edu.uco.xebia.bank.shared.{Money, NonEmptyString}

enum AccountStatus:
  case Active
  case Frozen(reason: NonEmptyString)
  case Closed(closedAt: Instant)

final case class Account(
    id: AccountId,
    owner: CustomerId,
    name: AccountName,
    balance: Money,
    status: AccountStatus
)
