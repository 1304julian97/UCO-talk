package co.edu.uco.xebia.bank.payments.algebras

import cats.effect.IO

import co.edu.uco.xebia.bank.shared.Money

trait MoneyAdder:
  def add(x: Money, y: Money): IO[Money]
