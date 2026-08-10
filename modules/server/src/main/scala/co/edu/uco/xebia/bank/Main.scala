package co.edu.uco.xebia.bank

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import co.edu.uco.xebia.bank.routes.{AccountsRoutes, DummyAccountImpl}

object Main extends IOApp.Simple:

  private def server: IO[Server] =
    val accountsAlgebra = new DummyAccountImpl()
    val routes = AccountsRoutes.routes(accountsAlgebra)

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
      .allocated
      .map(_._1)

  override val run: IO[Unit] =
    server.flatMap { srv =>
      IO.println(s"Server started at ${srv.address}") >>
        IO.never
    }
