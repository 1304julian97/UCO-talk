package co.edu.uco.xebia.bank.routes

import cats.effect.IO
import co.edu.uco.xebia.bank.payments.algebras.Payments
import co.edu.uco.xebia.bank.payments.models.{DepositRequest, PaymentError, TransferRequest, WithdrawRequest}
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec.*
import co.edu.uco.xebia.bank.codecs.JsonCodecs.given


object PaymentsRoutes {

  private def handlePaymentError(response: IO[Response[IO]]): IO[Response[IO]] =
    response.recoverWith {
      case PaymentError.AccountDoesNotExist(account) =>
        NotFound(s"Account $account does not exist")

      case PaymentError.InsufficientFunds(account, _, _) =>
        Conflict(s"Insufficient funds in account $account")

      case PaymentError.CurrencyMismatch(account, expected, actual) =>
        BadRequest(s"Account $account holds $expected, not $actual")

      case PaymentError.SameAccountTransfer(account) =>
        BadRequest(s"Cannot transfer to the same account $account")
    }

  def routes(algebra: Payments):HttpRoutes[IO] = HttpRoutes.of[IO]{
    case req@POST -> Root / "deposit" => handlePaymentError {
      for {
        request <- req.as[DepositRequest]
        transaction <- algebra.deposit(request.account, request.amount, request.conversion)
        response <- Ok(transaction)
      } yield response
    }

    case req@POST -> Root / "withdraw" => handlePaymentError {
      for {
        request <- req.as[WithdrawRequest]
        transaction <- algebra.withdraw(request.account, request.amount, request.conversion)
        response <- Ok(transaction)
      } yield response
    }

    case req@POST -> Root / "transfer" => handlePaymentError {
      for {
        request <- req.as[TransferRequest]
        transaction <- algebra.transfer(request.from, request.to, request.amount, request.conversion)
        response <- Ok(transaction)
      } yield response
    }
  }



}
