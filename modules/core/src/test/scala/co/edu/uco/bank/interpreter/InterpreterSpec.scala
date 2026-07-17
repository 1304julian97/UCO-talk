package co.edu.uco.bank.interpreter

import java.util.UUID

import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

import co.edu.uco.bank.dsl.Bank
import co.edu.uco.bank.dsl.syntax.*
import co.edu.uco.bank.models.*

class InterpreterSpec extends munit.ScalaCheckSuite:
  private val alice = AccountId(new UUID(0L, 1L))
  private val bob = AccountId(new UUID(0L, 2L))
  private val aliceOwner = CustomerId(new UUID(1L, 1L))
  private val bobOwner = CustomerId(new UUID(1L, 2L))

  private val genAmount: Gen[BigDecimal] = Gen.choose(1L, 1_000_000L).map(BigDecimal(_))

  private def usd(amount: BigDecimal): Money = Money(amount, Currency.USD)

  test("happy path: transfer moves funds between accounts") {
    val program =
      Bank
        .open(alice, aliceOwner, Currency.USD)
        .open(bob, bobOwner, Currency.USD)
        .deposit(alice, 100.USD)
        .transfer(alice, bob, 30.USD)
        .balance(alice)
        .balance(bob)

    assertEquals(
      Interpreter.run(program).map(_.balances.toList),
      Right(List(alice -> 70.USD, bob -> 30.USD))
    )
  }

  test("deposit to an unknown account is rejected") {
    val program = Bank.open(alice, aliceOwner, Currency.USD).deposit(bob, 10.USD)
    assertEquals(Interpreter.run(program), Left(BankError.UnknownAccount(bob)))
  }

  test("opening the same account twice is rejected") {
    val program = Bank.open(alice, aliceOwner, Currency.USD).open(alice, aliceOwner, Currency.USD)
    assertEquals(Interpreter.run(program), Left(BankError.AccountAlreadyExists(alice)))
  }

  test("depositing a different currency is rejected") {
    val program = Bank.open(alice, aliceOwner, Currency.USD).deposit(alice, 10.EUR)
    assertEquals(
      Interpreter.run(program),
      Left(BankError.CurrencyMismatch(alice, Currency.USD, Currency.EUR))
    )
  }

  test("withdrawing more than the balance is rejected") {
    val program =
      Bank.open(alice, aliceOwner, Currency.USD).deposit(alice, 10.USD).withdraw(alice, 20.USD)
    assertEquals(
      Interpreter.run(program),
      Left(BankError.InsufficientFunds(alice, 20.USD, 10.USD))
    )
  }

  test("a non-positive amount is rejected") {
    val program = Bank.open(alice, aliceOwner, Currency.USD).deposit(alice, 0.USD)
    assertEquals(Interpreter.run(program), Left(BankError.InvalidAmount(0.USD)))
  }

  test("transferring to the same account is rejected") {
    val program =
      Bank.open(alice, aliceOwner, Currency.USD).deposit(alice, 50.USD).transfer(alice, alice, 10.USD)
    assertEquals(Interpreter.run(program), Left(BankError.SameAccountTransfer(alice)))
  }

  test("transferring to an unknown destination is rejected and does not debit the source") {
    val program =
      Bank.open(alice, aliceOwner, Currency.USD).deposit(alice, 50.USD).transfer(alice, bob, 10.USD)
    assertEquals(Interpreter.run(program), Left(BankError.UnknownAccount(bob)))
  }

  test("transferring more than the source balance is rejected") {
    val program =
      Bank
        .open(alice, aliceOwner, Currency.USD)
        .open(bob, bobOwner, Currency.USD)
        .deposit(alice, 10.USD)
        .transfer(alice, bob, 20.USD)
    assertEquals(Interpreter.run(program), Left(BankError.InsufficientFunds(alice, 20.USD, 10.USD)))
  }

  test("a self-transfer on an unknown account reports the unknown account first") {
    val program = Bank.open(alice, aliceOwner, Currency.USD).transfer(bob, bob, 10.USD)
    assertEquals(Interpreter.run(program), Left(BankError.UnknownAccount(bob)))
  }

  test("withdrawing a different currency is rejected") {
    val program =
      Bank.open(alice, aliceOwner, Currency.USD).deposit(alice, 100.USD).withdraw(alice, 10.EUR)
    assertEquals(
      Interpreter.run(program),
      Left(BankError.CurrencyMismatch(alice, Currency.USD, Currency.EUR))
    )
  }

  test("transferring across currencies is rejected") {
    val program =
      Bank
        .open(alice, aliceOwner, Currency.USD)
        .open(bob, bobOwner, Currency.EUR)
        .deposit(alice, 100.USD)
        .transfer(alice, bob, 30.USD)
    assertEquals(
      Interpreter.run(program),
      Left(BankError.CurrencyMismatch(bob, Currency.EUR, Currency.USD))
    )
  }

  test("querying the balance of an unknown account is rejected") {
    val program = Bank.open(alice, aliceOwner, Currency.USD).balance(bob)
    assertEquals(Interpreter.run(program), Left(BankError.UnknownAccount(bob)))
  }

  test("transferring a non-positive amount is rejected") {
    val program =
      Bank
        .open(alice, aliceOwner, Currency.USD)
        .open(bob, bobOwner, Currency.USD)
        .deposit(alice, 50.USD)
        .transfer(alice, bob, 0.USD)
    assertEquals(Interpreter.run(program), Left(BankError.InvalidAmount(0.USD)))
  }

  property("transfer conserves the total balance") {
    forAll(genAmount, genAmount) { (deposit, requested) =>
      val amount = requested.min(deposit)
      val program =
        Bank
          .open(alice, aliceOwner, Currency.USD)
          .open(bob, bobOwner, Currency.USD)
          .deposit(alice, usd(deposit))
          .transfer(alice, bob, usd(amount))
          .balance(alice)
          .balance(bob)

      Interpreter.run(program).map(_.balances.map(_._2.amount).toList.sum) == Right(deposit)
    }
  }

  property("deposit then withdraw of the same amount is identity") {
    forAll(genAmount) { amount =>
      val program =
        Bank
          .open(alice, aliceOwner, Currency.USD)
          .deposit(alice, usd(amount))
          .withdraw(alice, usd(amount))
          .balance(alice)

      Interpreter.run(program).map(_.balances.map(_._2.amount).toList) == Right(List(BigDecimal(0)))
    }
  }

  property("withdrawing more than the balance is always rejected") {
    forAll(genAmount, genAmount) { (deposit, extra) =>
      val requested = deposit + extra
      val program =
        Bank
          .open(alice, aliceOwner, Currency.USD)
          .deposit(alice, usd(deposit))
          .withdraw(alice, usd(requested))

      Interpreter.run(program) == Left(BankError.InsufficientFunds(alice, usd(requested), usd(deposit)))
    }
  }
