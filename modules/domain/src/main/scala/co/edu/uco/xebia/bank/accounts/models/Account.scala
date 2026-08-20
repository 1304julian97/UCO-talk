package co.edu.uco.xebia.bank.accounts.models

import java.time.Instant
import co.edu.uco.xebia.bank.shared.{Currency, Money, NonEmptyString}

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

case class OpenAccountRequest(owner: CustomerId, name: AccountName, currency: Currency)
