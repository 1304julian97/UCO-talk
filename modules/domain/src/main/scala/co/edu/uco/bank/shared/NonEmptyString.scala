package co.edu.uco.bank.shared

opaque type NonEmptyString = String
object NonEmptyString:
  def from(value: String): Either[String, NonEmptyString] =
    val trimmed = value.trim
    if trimmed.isEmpty then Left("value must not be blank") else Right(trimmed)
  extension (string: NonEmptyString) def value: String = string
