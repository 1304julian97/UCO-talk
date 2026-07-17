package co.edu.uco.bank.models

enum BankError:
  case UnknownAccount(account: AccountId)
  case AccountAlreadyExists(account: AccountId)
  case CurrencyMismatch(account: AccountId, expected: Currency, actual: Currency)
  case InsufficientFunds(account: AccountId, requested: Money, available: Money)
  case InvalidAmount(amount: Money)
  case SameAccountTransfer(account: AccountId)
