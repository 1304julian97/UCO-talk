package co.edu.uco.bank

import java.util.UUID

import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all.*

import co.edu.uco.bank.dsl.Bank
import co.edu.uco.bank.dsl.syntax.*
import co.edu.uco.bank.interpreter.Interpreter
import co.edu.uco.bank.models.{AccountId, Currency, CustomerId}

object Main extends IOApp.Simple:
  private val alice = AccountId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
  private val bob = AccountId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
  private val aliceOwner = CustomerId(UUID.fromString("00000000-0000-0000-0000-0000000000a1"))
  private val bobOwner = CustomerId(UUID.fromString("00000000-0000-0000-0000-0000000000b1"))

  private val program =
    Bank
      .open(alice, aliceOwner, Currency.USD)
      .open(bob, bobOwner, Currency.USD)
      .deposit(alice, 100.USD)
      .transfer(alice, bob, 30.USD)
      .balance(alice)
      .balance(bob)

  override val run: IO[Unit] =
    Interpreter.run(program) match
      case Right(result) =>
        result.balances.traverse_ { case (account, money) =>
          IO.println(s"${account.value}: ${money.amount} ${money.currency}")
        }
      case Left(error) =>
        IO.println(s"Rejected: $error")
