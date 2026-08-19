package co.edu.uco.xebia.bank.persistence.memory

import cats.effect.std.AtomicMap
import co.edu.uco.xebia.bank.accounts.models.AccountId
import co.edu.uco.xebia.bank.accounts.models.Account
import cats.effect.IO

type BlockingInMemoryBankPersistence = AtomicMap[IO, AccountId, Option[Account]]
object BlockingInMemoryBankPersistence:
  def make: IO[BlockingInMemoryBankPersistence] =
    AtomicMap.apply
