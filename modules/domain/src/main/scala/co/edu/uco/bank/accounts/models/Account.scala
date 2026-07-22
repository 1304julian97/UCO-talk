package co.edu.uco.bank.accounts.models

import java.time.Instant

import co.edu.uco.bank.shared.{Money, NonEmptyString}

enum AccountStatus:
  case Active
  case Frozen(reason: NonEmptyString)
  case Closed(closedAt: Instant)

final case class Account(
    id: AccountId,
    owner: CustomerId,
    name: Option[NonEmptyString],
    balance: Money,
    status: AccountStatus
)
