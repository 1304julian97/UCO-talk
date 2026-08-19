package co.edu.uco.xebia.bank.payments.algebras

import cats.effect.IO

import co.edu.uco.xebia.bank.shared.Currency
import co.edu.uco.xebia.bank.shared.Money
import co.edu.uco.xebia.bank.shared.MoneyAmount
import co.edu.uco.xebia.bank.payments.models.ConversionPolicy
import co.edu.uco.xebia.bank.payments.models.MoneyError

trait MoneyCalculator:
  def add(x: Money, y: Money, conversion: ConversionPolicy): IO[Money]
  def subtract(x: Money, y: Money, conversion: ConversionPolicy): IO[Money]

object MoneyCalculator:
  def make(converter: CurrencyConverter): MoneyCalculator =
    new MoneyCalculator:
      override def add(x: Money, y: Money, conversion: ConversionPolicy): IO[Money] =
        convertCurrencyOperation(x, y, conversion, sameCurrencyOperation = sameCurrencyAdd)

      override def subtract(x: Money, y: Money, conversion: ConversionPolicy): IO[Money] =
        convertCurrencyOperation(x, y, conversion, sameCurrencyOperation = sameCurrencySubtract)

      private def convertCurrencyOperation(
          x: Money,
          y: Money,
          conversion: ConversionPolicy,
          sameCurrencyOperation: (MoneyAmount, MoneyAmount, Currency) => IO[Money]
      ): IO[Money] =
        if (x.currency == y.currency) then sameCurrencyOperation(x.amount, y.amount, x.currency)
        else
          conversion match
            case ConversionPolicy.Convert =>
              converter.convert(y, to = x.currency).flatMap { convertedY =>
                sameCurrencyOperation(x.amount, convertedY.amount, x.currency)
              }

            case ConversionPolicy.Reject =>
              IO.raiseError(MoneyError.IncompatibleCurrencies(x.currency, y.currency))

      private def sameCurrencyAdd(x: MoneyAmount, y: MoneyAmount, currency: Currency): IO[Money] =
        IO.pure(
          Money(
            // Adding two amounts is guaranteed to return a positive value.
            amount = MoneyAmount.applyUnsafe(x + y),
            currency = currency
          )
        )

      private def sameCurrencySubtract(x: MoneyAmount, y: MoneyAmount, currency: Currency): IO[Money] =
        if (x >= y) then
          IO.pure(
            Money(
              // Subtracting a smaller amount is guaranteed to return a positive value.
              amount = MoneyAmount.applyUnsafe(x - y),
              currency = currency
            )
          )
        else
          IO.raiseError(
            MoneyError.NegativeResult(x, y)
          )
