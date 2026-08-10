package co.edu.uco.xebia.bank.routes

import org.http4s.HttpRoutes
import co.edu.uco.xebia.bank.accounts.algebras.Accounts
import co.edu.uco.xebia.bank.accounts.models.AccountId
import co.edu.uco.xebia.bank.codecs.JsonCodecs.given
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec.*
import cats.effect.IO
import java.util.UUID

object AccountsRoutes {

  private object UUIDVar:
    def unapply(s: String): Option[UUID] =
      try Some(UUID.fromString(s))
      catch case _: IllegalArgumentException => None

  def routes(algebra: Accounts): HttpRoutes[IO] = HttpRoutes.of[IO] { case GET -> Root / "account" / UUIDVar(uuidVar) =>
    algebra.find(AccountId(uuidVar)).flatMap {
      case Some(account) => Ok(account)
      case None => NotFound("Account not found")
    }

  }

}
