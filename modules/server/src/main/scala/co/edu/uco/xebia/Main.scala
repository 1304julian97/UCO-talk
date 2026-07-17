package co.edu.uco.xebia

import cats.effect.IOApp
import cats.effect.IO

object Main extends IOApp.Simple:
  override val run: IO[Unit] =
    IO.println("Hello, world!")
