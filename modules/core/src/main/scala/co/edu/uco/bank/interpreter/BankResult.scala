package co.edu.uco.bank.interpreter

import cats.data.Chain

import co.edu.uco.bank.models.{AccountId, Money}

final case class BankResult(state: BankState, balances: Chain[(AccountId, Money)])

object BankResult:
  val empty: BankResult = BankResult(BankState.empty, Chain.empty)
