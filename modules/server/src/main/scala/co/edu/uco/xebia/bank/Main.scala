package co.edu.uco.xebia.bank

import cats.effect.IO
import cats.effect.IOApp
import co.edu.uco.xebia.bank.accounts.algebras.Accounts
import co.edu.uco.xebia.bank.accounts.models.*
import co.edu.uco.xebia.bank.payments.algebras.*
import co.edu.uco.xebia.bank.payments.models.ConversionPolicy
import co.edu.uco.xebia.bank.persistence.memory.BlockingInMemoryBankPersistence
import co.edu.uco.xebia.bank.shared.*
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import co.edu.uco.xebia.bank.routes.{AccountsRoutes, DummyAccountImpl}

object Main extends IOApp.Simple:

  def runServer(accounts: Accounts, payments: Payments): IO[Server] =
    val accountsAlgebra = new DummyAccountImpl()

    val routes = AccountsRoutes.routes(accounts)

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
      .allocated
      .map(_._1)

  override val run: IO[Unit] = {

    val resources = for {
      persistence <- BlockingInMemoryBankPersistence.make.toResource
      converter <- CurrencyConverter.make
      calculator = MoneyCalculator.make(converter)
      accounts = Accounts.make(persistence)
      accountsDummy = new DummyAccountImpl()
      payments = Payments.make(calculator, persistence)

    } yield (accounts, payments)

    resources.use { case (accounts, payments) =>
      for {
        luisId <- IO.randomUUID
        luisAccount <- accounts.open(
          owner = CustomerId(luisId),
          currency = Currency.COP,
          name = AccountName.from("BalmungSan").toOption.get
        )

        jonathanId <- IO.randomUUID
        jonathanAccount <- accounts.open(
          owner = CustomerId(jonathanId),
          currency = Currency.USD,
          name = AccountName.from("jdeyrson").toOption.get
        )

        _ <- payments.deposit(
          account = luisAccount.id,
          amount = Money(
            amount = MoneyAmount(BigDecimal(100000)),
            currency = Currency.COP
          ),
          conversion = ConversionPolicy.Convert
        )

        _ <- payments.deposit(
          account = jonathanAccount.id,
          amount = Money(
            amount = MoneyAmount(BigDecimal(100)),
            currency = Currency.USD
          ),
          conversion = ConversionPolicy.Convert
        )

        _ <- payments.transfer(
          from = luisAccount.id,
          to = jonathanAccount.id,
          amount = Money(
            amount = MoneyAmount(BigDecimal(5)),
            currency = Currency.EUR
          ),
          conversion = ConversionPolicy.Convert
        )

        _ <- accounts.find(luisAccount.id).flatTap(IO.println)
        _ <- accounts.find(jonathanAccount.id).flatTap(IO.println)
        serve <- runServer(accounts, payments).flatMap(srv =>
          IO.println(s"Server started at ${srv.address}")
        ) >> IO.never
      } yield serve
    }
  }
