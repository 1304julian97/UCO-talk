package co.edu.uco.xebia.bank

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  override val run: IO[Unit] =
    IO.println("Hello, world!")
