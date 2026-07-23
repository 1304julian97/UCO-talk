package co.edu.uco.xebia.bank.shared

import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

class MoneySpec extends munit.ScalaCheckSuite:
  private val genAmount: Gen[BigDecimal] =
    Gen.choose(-100_000_00L, 100_000_00L).map(cents => BigDecimal(cents) / 100)

  private val genCurrency: Gen[Currency] = Gen.oneOf(Currency.values.toIndexedSeq)

  private def money(currency: Currency): Gen[Money] = genAmount.map(amount => Money(amount, currency))

  private val genSingle: Gen[(Currency, Money)] =
    for
      currency <- genCurrency
      a <- money(currency)
    yield (currency, a)

  private val genPair: Gen[(Currency, Money, Money)] =
    for
      currency <- genCurrency
      a <- money(currency)
      b <- money(currency)
    yield (currency, a, b)

  private val genTriple: Gen[(Currency, Money, Money, Money)] =
    for
      currency <- genCurrency
      a <- money(currency)
      b <- money(currency)
      c <- money(currency)
    yield (currency, a, b, c)

  property("combine is associative within a currency") {
    forAll(genTriple) { case (currency, a, b, c) =>
      val monoid = Money.monoid(currency)
      monoid.combine(monoid.combine(a, b), c) == monoid.combine(a, monoid.combine(b, c))
    }
  }

  property("empty is an identity within a currency") {
    forAll(genSingle) { case (currency, a) =>
      val monoid = Money.monoid(currency)
      monoid.combine(monoid.empty, a) == a && monoid.combine(a, monoid.empty) == a
    }
  }

  property("combine is commutative within a currency") {
    forAll(genPair) { case (currency, a, b) =>
      val monoid = Money.monoid(currency)
      monoid.combine(a, b) == monoid.combine(b, a)
    }
  }
