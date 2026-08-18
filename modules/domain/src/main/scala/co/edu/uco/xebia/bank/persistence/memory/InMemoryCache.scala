package co.edu.uco.xebia.bank.persistence.memory

import cats.effect.IO
import cats.effect.Resource
import cats.effect.std.MapRef
import cats.syntax.all.*

import scala.concurrent.duration.Duration
import cats.effect.std.Supervisor

trait InMemoryCache[K, V]:
  def get(key: K): IO[Option[V]]
  def save(key: K, value: V, ttl: Duration): IO[Unit]

object InMemoryCache:
  def make[K, V]: Resource[IO, InMemoryCache[K, V]] =
    (
      MapRef[IO, K, V].toResource,
      Supervisor[IO]
    ).mapN { case (mapRef, supervisor) =>
      new InMemoryCache[K, V]:
        override def get(key: K): IO[Option[V]] =
          mapRef(key).get

        override def save(key: K, value: V, ttl: Duration): IO[Unit] =
          mapRef.setKeyValue(key, value) >>
            supervisor.supervise {
              mapRef.unsetKey(key).delayBy(ttl)
            }.void
    }
