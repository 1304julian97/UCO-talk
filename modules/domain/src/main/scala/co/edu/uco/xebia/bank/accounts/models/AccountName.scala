package co.edu.uco.xebia.bank.accounts.models

import co.edu.uco.xebia.bank.shared.NonEmptyString

opaque type AccountName <: String = String
object AccountName:
  def from(value: String): Either[String, AccountName] =
    NonEmptyString.from(value)
