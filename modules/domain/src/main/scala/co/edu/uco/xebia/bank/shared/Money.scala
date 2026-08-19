package co.edu.uco.xebia.bank.shared

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.numeric.*

type MoneyAmount = MoneyAmount.T
object MoneyAmount extends RefinedSubtype[BigDecimal, Positive0]:
  val zero: MoneyAmount = apply(BigDecimal(0))

final case class Money(amount: MoneyAmount, currency: Currency)
