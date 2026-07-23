package co.edu.uco.xebia.bank.shared

opaque type NonEmptyString <: String = String
object NonEmptyString:
  def from(value: String): Either[String, NonEmptyString] =
    val trimmed = value.trim
    if trimmed.isEmpty then Left("value must not be blank") else Right(trimmed)
