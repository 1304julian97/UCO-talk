package co.edu.uco.xebia.bank.codecs

import cats.syntax.either.catsSyntaxEitherId
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import co.edu.uco.xebia.bank.accounts.models.*
import co.edu.uco.xebia.bank.payments.models.*
import co.edu.uco.xebia.bank.shared.*

import java.util.UUID
import java.time.Instant

object JsonCodecs {

  // UUID codecs for opaque types
  given Encoder[AccountId] = Encoder[UUID].contramap(identity)
  given Decoder[AccountId] = Decoder[UUID].map(AccountId(_))

  given Encoder[CustomerId] = Encoder[UUID].contramap(identity)
  given Decoder[CustomerId] = Decoder[UUID].map(CustomerId(_))

  given Encoder[TransactionId] = Encoder[UUID].contramap(identity)
  given Decoder[TransactionId] = Decoder[UUID].map(TransactionId(_))

  // String codecs for opaque types
  given Encoder[NonEmptyString] = Encoder[String].contramap(identity)
  given Decoder[NonEmptyString] = Decoder[String].emap(NonEmptyString.from)

  given Encoder[AccountName] = Encoder[String].contramap(identity)
  given Decoder[AccountName] = Decoder[String].emap(AccountName.from)

  // Refined numeric codecs for opaque types
  given Encoder[MoneyAmount] = Encoder[BigDecimal].contramap(identity)
  given Decoder[MoneyAmount] = Decoder[BigDecimal].emap(MoneyAmount.either(_))

  // Enum codecs
  given Encoder[Currency] = Encoder[String].contramap(_.toString)
  given Decoder[Currency] = Decoder[String].emap {
    case "USD" => Right(Currency.USD)
    case "EUR" => Right(Currency.EUR)
    case "COP" => Right(Currency.COP)
    case other => Left(s"Unknown currency: $other")
  }

  given Encoder[ConversionPolicy] = Encoder[String].contramap(_.toString)
  given Decoder[ConversionPolicy] = Decoder[String].emap {
    case "Reject" => ConversionPolicy.Reject.asRight[String]
    case "Convert" => ConversionPolicy.Convert.asRight[String]
    case _ => "Wrong value for ConversionPolicy".asLeft[ConversionPolicy]
  }

  given Encoder[OpenAccountRequest] = deriveEncoder[OpenAccountRequest]
  given Decoder[OpenAccountRequest] = deriveDecoder[OpenAccountRequest]

  given Encoder[FreezeAccountRequest] = deriveEncoder[FreezeAccountRequest]
  given Decoder[FreezeAccountRequest] = deriveDecoder[FreezeAccountRequest]

  given Encoder[DepositRequest] = deriveEncoder[DepositRequest]
  given Decoder[DepositRequest] = deriveDecoder[DepositRequest]

  given Encoder[WithdrawRequest] = deriveEncoder[WithdrawRequest]
  given Decoder[WithdrawRequest] = deriveDecoder[WithdrawRequest]

  given Encoder[TransferRequest] = deriveEncoder[TransferRequest]
  given Decoder[TransferRequest] = deriveDecoder[TransferRequest]
  // Case class codecs
  given Encoder[Money] = deriveEncoder[Money]
  given Decoder[Money] = deriveDecoder[Money]

  given Encoder[AccountStatus] = Encoder.instance {
    case AccountStatus.Active =>
      io.circe.Json.obj("type" -> io.circe.Json.fromString("Active"))
    case AccountStatus.Frozen(reason) =>
      io.circe.Json.obj(
        "type" -> io.circe.Json.fromString("Frozen"),
        "reason" -> io.circe.Json.fromString(reason)
      )
    case AccountStatus.Closed(closedAt) =>
      io.circe.Json.obj(
        "type" -> io.circe.Json.fromString("Closed"),
        "closedAt" -> io.circe.Json.fromString(closedAt.toString)
      )
  }

  given Decoder[AccountStatus] = Decoder.instance { cursor =>
    cursor.get[String]("type").flatMap {
      case "Active" => Right(AccountStatus.Active)
      case "Frozen" => cursor.get[NonEmptyString]("reason").map(r => AccountStatus.Frozen(r))
      case "Closed" => cursor.get[String]("closedAt").map(dt => AccountStatus.Closed(Instant.parse(dt)))
      case other => Left(io.circe.DecodingFailure(s"Unknown status type: $other", cursor.history))
    }
  }

  given Encoder[Account] = deriveEncoder[Account]
  given Decoder[Account] = deriveDecoder[Account]

  given Encoder[Transaction] = Encoder.instance {
    case Transaction.Deposit(id, account, amount) =>
      io.circe.Json.obj(
        "type" -> io.circe.Json.fromString("Deposit"),
        "id" -> Encoder[TransactionId].apply(id),
        "account" -> Encoder[AccountId].apply(account),
        "amount" -> Encoder[Money].apply(amount)
      )
    case Transaction.Withdrawal(id, account, amount) =>
      io.circe.Json.obj(
        "type" -> io.circe.Json.fromString("Withdrawal"),
        "id" -> Encoder[TransactionId].apply(id),
        "account" -> Encoder[AccountId].apply(account),
        "amount" -> Encoder[Money].apply(amount)
      )
    case Transaction.Transfer(id, from, to, amount) =>
      io.circe.Json.obj(
        "type" -> io.circe.Json.fromString("Transfer"),
        "id" -> Encoder[TransactionId].apply(id),
        "from" -> Encoder[AccountId].apply(from),
        "to" -> Encoder[AccountId].apply(to),
        "amount" -> Encoder[Money].apply(amount)
      )
  }

  given Decoder[Transaction] = Decoder.instance { cursor =>
    cursor.get[String]("type").flatMap {
      case "Deposit" =>
        for {
          id <- cursor.get[TransactionId]("id")
          account <- cursor.get[AccountId]("account")
          amount <- cursor.get[Money]("amount")
        } yield Transaction.Deposit(id, account, amount)

      case "Withdrawal" =>
        for {
          id <- cursor.get[TransactionId]("id")
          account <- cursor.get[AccountId]("account")
          amount <- cursor.get[Money]("amount")
        } yield Transaction.Withdrawal(id, account, amount)

      case "Transfer" =>
        for {
          id <- cursor.get[TransactionId]("id")
          from <- cursor.get[AccountId]("from")
          to <- cursor.get[AccountId]("to")
          amount <- cursor.get[Money]("amount")
        } yield Transaction.Transfer(id, from, to, amount)

      case other =>
        Left(io.circe.DecodingFailure(s"Unknown transaction type: $other", cursor.history))
    }
  }
}
