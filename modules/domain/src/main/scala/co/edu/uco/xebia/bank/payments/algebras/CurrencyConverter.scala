package co.edu.uco.xebia.bank.payments.algebras

import cats.effect.IO

import co.edu.uco.xebia.bank.shared.{Currency, Money}

trait CurrencyConverter:
  def convert(amount: Money, to: Currency): IO[Money]
