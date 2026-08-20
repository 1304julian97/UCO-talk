package co.edu.uco.xebia.bank.routes

import org.http4s.HttpRoutes
import co.edu.uco.xebia.bank.accounts.algebras.Accounts
import co.edu.uco.xebia.bank.accounts.models.{AccountId, OpenAccountRequest}
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec.*
import co.edu.uco.xebia.bank.codecs.JsonCodecs.given
import cats.effect.IO

import java.util.UUID

object AccountsRoutes {

  private object UUIDVar:
    def unapply(s: String): Option[UUID] =
      try Some(UUID.fromString(s))
      catch case _: IllegalArgumentException => None

  def routes(algebra: Accounts): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "account" / UUIDVar(uuidVar) =>
      algebra.find(AccountId(uuidVar)).flatMap {
        case Some(account) => Ok(account)
        case None => NotFound("Account not found")
      }
    case req @ POST -> Root / "account" =>
      for {
        request <- req.as[OpenAccountRequest]
        account <- algebra.open(request.owner, request.currency, request.name)
        response <- Ok(account.id.toString)
      } yield response

  }

}
