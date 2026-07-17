package co.edu.uco.bank.dsl

import co.edu.uco.bank.models.{Currency, Money}

object syntax:
  extension (amount: Int)
    def USD: Money = Money(BigDecimal(amount), Currency.USD)
    def EUR: Money = Money(BigDecimal(amount), Currency.EUR)
    def COP: Money = Money(BigDecimal(amount), Currency.COP)
