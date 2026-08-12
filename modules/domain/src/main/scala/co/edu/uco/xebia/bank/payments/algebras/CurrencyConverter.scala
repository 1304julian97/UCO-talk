package co.edu.uco.xebia.bank.payments.algebras

import cats.data.OptionT
import cats.effect.IO
import cats.effect.Resource
import cats.syntax.all.*

import co.edu.uco.xebia.bank.gateway.FrankfurterApi
import co.edu.uco.xebia.bank.persistence.memory.InMemoryCache
import co.edu.uco.xebia.bank.shared.*
import co.edu.uco.xebia.bank.payments.models.ConversionError

import scala.concurrent.duration.given

trait CurrencyConverter:
  def convert(amount: Money, to: Currency): IO[Money]

object CurrencyConverter:
  def make: Resource[IO, CurrencyConverter] =
    (
      InMemoryCache.make[(Currency, Currency), Rate],
      FrankfurterApi.make
    ).mapN { case (cache, api) =>
      new CurrencyConverter:
        override def convert(amount: Money, to: Currency): IO[Money] =
          val base = amount.currency
          val quote = to

          val cachedRate = OptionT(cache.get(key = base -> quote))
          val apiRate = OptionT(
            api
              .fetchRate(base, quote)
              // Save the fetched rate into the cache to avoid unnecessary requests for the next hour.
              .flatTap(_.traverse(rate => cache.save(key = base -> quote, value = rate, ttl = 1.hour)))
          )

          (cachedRate orElse apiRate)
            .getOrRaise(ConversionError.RateUnavailable(from = base, to = quote))
            .map { rate =>
              // Multiplying an amount with a rate is guaranteed to return a positive value.
              val convertedAmount = MoneyAmount.applyUnsafe(amount.amount * rate)
              Money(
                amount = convertedAmount,
                currency = to
              )
            }
    }
