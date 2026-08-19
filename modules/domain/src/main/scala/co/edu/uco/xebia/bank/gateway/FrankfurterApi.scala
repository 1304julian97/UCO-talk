package co.edu.uco.xebia.bank.gateway

import cats.effect.IO
import cats.effect.Resource
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.Uri
import org.http4s.circe.CirceEntityDecoder
import org.http4s.syntax.all.*
import org.http4s.ember.client.EmberClientBuilder

import co.edu.uco.xebia.bank.shared.Currency
import co.edu.uco.xebia.bank.shared.Rate

import scala.concurrent.duration.Duration
import scala.concurrent.duration.given

trait FrankfurterApi:
  def fetchRate(base: Currency, quote: Currency): IO[Option[Rate]]

object FrankfurterApi:
  private val endpoint: Uri = uri"https://api.frankfurter.dev/v2/rate/"

  private given Uri.Path.SegmentEncoder[Currency] =
    Uri.Path.SegmentEncoder.stringSegmentEncoder.contramap(_.toString)

  private given Decoder[Rate] =
    Decoder[BigDecimal].at(field = "rate").emap(Rate.either)

  private given EntityDecoder[IO, Rate] =
    CirceEntityDecoder.circeEntityDecoder

  def make: Resource[IO, FrankfurterApi] =
    EmberClientBuilder.default[IO].build.map { client =>
      new FrankfurterApi:
        override def fetchRate(base: Currency, quote: Currency): IO[Option[Rate]] =
          retry {
            client.get(endpoint / base / quote) { response =>
              response.as[Rate]
            }
          }

        private def retry[A](
            operation: IO[A],
            maxRetries: Int = 3,
            delay: Duration = 100.millis
        ): IO[Option[A]] =
          operation.attempt.flatMap {
            case Right(value) =>
              IO.some(value)

            case Left(_) =>
              if (maxRetries > 0) then
                IO.sleep(delay) >>
                  retry(
                    operation,
                    maxRetries = maxRetries - 1,
                    delay = delay * 2
                  )
              else IO.none
          }
    }
