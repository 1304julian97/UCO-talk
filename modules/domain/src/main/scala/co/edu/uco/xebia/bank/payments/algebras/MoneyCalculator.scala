package co.edu.uco.xebia.bank.payments.algebras

import cats.effect.IO

import co.edu.uco.xebia.bank.shared.Money
import co.edu.uco.xebia.bank.payments.models.ConversionPolicy

trait MoneyCalculator:
  def add(x: Money, y: Money, conversion: ConversionPolicy): IO[Money]
  def subtract(x: Money, y: Money, conversion: ConversionPolicy): IO[Money]
