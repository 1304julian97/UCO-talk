package co.edu.uco.xebia.bank.payments.models

import co.edu.uco.xebia.bank.accounts.models.AccountId
import co.edu.uco.xebia.bank.shared.Money

import java.util.UUID

opaque type TransactionId <: UUID = UUID
object TransactionId:
  def apply(value: UUID): TransactionId = value

case class DepositRequest(account: AccountId, amount: Money, conversion: ConversionPolicy)

case class WithdrawRequest(account: AccountId, amount: Money, conversion: ConversionPolicy)

case class TransferRequest(from: AccountId, to: AccountId, amount: Money, conversion: ConversionPolicy)
