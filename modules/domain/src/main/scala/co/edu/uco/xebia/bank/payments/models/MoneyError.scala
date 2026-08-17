package co.edu.uco.xebia.bank.payments.models

import scala.util.control.NoStackTrace
import co.edu.uco.xebia.bank.shared.Currency
import co.edu.uco.xebia.bank.shared.MoneyAmount

enum MoneyError(message: String) extends RuntimeException(message), NoStackTrace:
  case NegativeResult(x: MoneyAmount, y: MoneyAmount)
      extends MoneyError(s"cannot subtract $y from $x because the result would be negative")
  case IncompatibleCurrencies(x: Currency, y: Currency)
      extends MoneyError(s"cannot operate on different currencies $x & $y when the `ConversionPolicy` is `Reject`")
