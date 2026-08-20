package co.edu.uco.xebia.bank

import cats.effect.IO
import cats.effect.IOApp

import co.edu.uco.xebia.bank.accounts.algebras.*
import co.edu.uco.xebia.bank.payments.algebras.*
import co.edu.uco.xebia.bank.persistence.memory.BlockingInMemoryBankPersistence

object Main extends IOApp.Simple:
  override val run: IO[Unit] =
    val resources = for {
      persistence <- BlockingInMemoryBankPersistence.make.toResource
      converter <- CurrencyConverter.make
      calculator = MoneyCalculator.make(converter)
      accounts = Accounts.make(persistence)
      payments = Payments.make(calculator, persistence)
      server <- Server.make(accounts, payments)
    } yield server

    resources.useForever
