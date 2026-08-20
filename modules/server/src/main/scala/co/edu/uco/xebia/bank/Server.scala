package co.edu.uco.xebia.bank

import cats.effect.IO
import cats.effect.Resource
import com.comcast.ip4s.*
import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder

import co.edu.uco.xebia.bank.accounts.algebras.Accounts
import co.edu.uco.xebia.bank.payments.algebras.Payments
import co.edu.uco.xebia.bank.routes.*

import scala.annotation.unused

object Server:
  def make(accounts: Accounts, @unused payments: Payments): Resource[IO, Server] =
    val routes = AccountsRoutes.routes(accounts)

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
      .evalTap { server =>
        IO.println(s"Server started at ${server.address}")
      }
