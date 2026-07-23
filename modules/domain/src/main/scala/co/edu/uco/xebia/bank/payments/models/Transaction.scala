package co.edu.uco.xebia.bank.payments.models

import co.edu.uco.xebia.bank.accounts.models.AccountId
import co.edu.uco.xebia.bank.shared.Money

enum Transaction:
  case Deposit(id: TransactionId, account: AccountId, amount: Money)
  case Withdrawal(id: TransactionId, account: AccountId, amount: Money)
  case Transfer(id: TransactionId, from: AccountId, to: AccountId, amount: Money)
