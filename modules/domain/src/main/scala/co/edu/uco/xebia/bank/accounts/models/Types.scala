package co.edu.uco.xebia.bank.accounts.models

import java.util.UUID

opaque type AccountId <: UUID = UUID
object AccountId:
  def apply(value: UUID): AccountId = value

opaque type CustomerId <: UUID = UUID
object CustomerId:
  def apply(value: UUID): CustomerId = value
