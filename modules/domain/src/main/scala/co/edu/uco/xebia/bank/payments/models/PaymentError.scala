package co.edu.uco.xebia.bank.payments.models

import scala.util.control.NoStackTrace

import co.edu.uco.xebia.bank.accounts.models.AccountId
import co.edu.uco.xebia.bank.shared.{Currency, Money}

enum PaymentError(message: String) extends RuntimeException(message), NoStackTrace:
  case InsufficientFunds(account: AccountId, requested: Money, available: Money)
      extends PaymentError(s"insufficient funds in account $account")
  case CurrencyMismatch(account: AccountId, expected: Currency, actual: Currency)
      extends PaymentError(s"account $account holds $expected, not $actual")
  case SameAccountTransfer(account: AccountId) extends PaymentError(s"cannot transfer to the same account $account")
  case InvalidAmount(amount: Money) extends PaymentError(s"invalid amount ${amount.amount} ${amount.currency}")
