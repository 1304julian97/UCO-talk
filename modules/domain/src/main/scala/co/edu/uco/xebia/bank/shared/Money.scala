package co.edu.uco.xebia.bank.shared

import cats.Monoid

final case class Money(amount: BigDecimal, currency: Currency)

object Money:
  def monoid(currency: Currency): Monoid[Money] = new Monoid[Money]:
    val empty: Money = Money(BigDecimal(0), currency)
    def combine(x: Money, y: Money): Money = Money(x.amount + y.amount, currency)
