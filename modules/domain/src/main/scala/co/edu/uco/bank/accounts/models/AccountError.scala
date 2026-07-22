package co.edu.uco.bank.accounts.models

import scala.util.control.NoStackTrace

enum AccountError(message: String) extends RuntimeException(message), NoStackTrace:
  case AccountNotFound(id: AccountId) extends AccountError(s"account not found: ${id.value}")
  case AccountAlreadyFrozen(id: AccountId) extends AccountError(s"account already frozen: ${id.value}")
  case AccountAlreadyClosed(id: AccountId) extends AccountError(s"account already closed: ${id.value}")
