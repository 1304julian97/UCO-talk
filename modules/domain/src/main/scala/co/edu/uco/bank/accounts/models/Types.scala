package co.edu.uco.bank.accounts.models

import java.util.UUID

opaque type AccountId = UUID
object AccountId:
  def apply(value: UUID): AccountId = value
  extension (id: AccountId) def value: UUID = id

opaque type CustomerId = UUID
object CustomerId:
  def apply(value: UUID): CustomerId = value
  extension (id: CustomerId) def value: UUID = id
