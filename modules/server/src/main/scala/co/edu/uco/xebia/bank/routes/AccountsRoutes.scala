package co.edu.uco.xebia.bank.routes

import org.http4s.HttpRoutes
import co.edu.uco.xebia.bank.accounts.algebras.Accounts
import co.edu.uco.xebia.bank.accounts.models.{AccountError, AccountId, FreezeAccountRequest, OpenAccountRequest}
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

  private def handleAccountError(response: IO[Response[IO]]): IO[Response[IO]] =
    response.recoverWith {
      case AccountError.AccountNotFound(id) => NotFound(s"Account not found: $id")
      case AccountError.AccountAlreadyFrozen(id) => Conflict(s"Account already frozen: $id")
      case AccountError.AccountAlreadyClosed(id) => Conflict(s"Account already closed: $id")
    }

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

    case req @ POST -> Root / "account" / UUIDVar(uuidVar) / "freeze" =>
      handleAccountError {
        for {
          request <- req.as[FreezeAccountRequest]
          account <- algebra.freeze(AccountId(uuidVar), request.reason)
          response <- Ok(account)
        } yield response
      }

    case POST -> Root / "account" / UUIDVar(uuidVar) / "close" =>
      handleAccountError {
        algebra.close(AccountId(uuidVar)).flatMap(account => Ok(account))
      }

  }

}
