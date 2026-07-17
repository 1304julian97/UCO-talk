package co.edu.uco.bank.interpreter

import co.edu.uco.bank.models.{Account, AccountId}

final case class BankState(accounts: Map[AccountId, Account]):
  def get(account: AccountId): Option[Account] = accounts.get(account)
  def updated(account: Account): BankState = BankState(accounts.updated(account.id, account))

object BankState:
  val empty: BankState = BankState(Map.empty)
