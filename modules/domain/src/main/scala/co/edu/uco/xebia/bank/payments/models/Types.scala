package co.edu.uco.xebia.bank.payments.models

import java.util.UUID

opaque type TransactionId <: UUID = UUID
object TransactionId:
  def apply(value: UUID): TransactionId = value
