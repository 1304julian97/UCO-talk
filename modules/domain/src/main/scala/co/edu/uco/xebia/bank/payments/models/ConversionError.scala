package co.edu.uco.xebia.bank.payments.models

import scala.util.control.NoStackTrace

import co.edu.uco.xebia.bank.shared.Currency

enum ConversionError(message: String) extends RuntimeException(message), NoStackTrace:
  case RateUnavailable(from: Currency, to: Currency) extends ConversionError(s"no rate available: $from -> $to")
