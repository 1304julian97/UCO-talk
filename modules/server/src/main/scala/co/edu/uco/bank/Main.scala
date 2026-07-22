package co.edu.uco.bank

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  override val run: IO[Unit] =
    IO.println("Hello, world!")
