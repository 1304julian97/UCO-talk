package co.edu.uco.xebia.bank.codecs

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import co.edu.uco.xebia.bank.accounts.models.*
import co.edu.uco.xebia.bank.shared.*
import java.util.UUID
import java.time.Instant

object JsonCodecs {

  // UUID codecs for opaque types
  given Encoder[AccountId] = Encoder[UUID].contramap(identity)
  given Decoder[AccountId] = Decoder[UUID].map(AccountId(_))

  given Encoder[CustomerId] = Encoder[UUID].contramap(identity)
  given Decoder[CustomerId] = Decoder[UUID].map(CustomerId(_))

  // String codecs for opaque types
  given Encoder[NonEmptyString] = Encoder[String].contramap(identity)
  given Decoder[NonEmptyString] = Decoder[String].emap(NonEmptyString.from)

  given Encoder[AccountName] = Encoder[String].contramap(identity)
  given Decoder[AccountName] = Decoder[String].emap(AccountName.from)

  // Enum codecs
  given Encoder[Currency] = Encoder[String].contramap(_.toString)
  given Decoder[Currency] = Decoder[String].emap {
    case "USD" => Right(Currency.USD)
    case "EUR" => Right(Currency.EUR)
    case "COP" => Right(Currency.COP)
    case other => Left(s"Unknown currency: $other")
  }

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
}
