package io.blindnet.dataaccess

import cats.effect.{ExitCode, IO, IOApp};

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    ServerApp().server.use(_ => IO.never).as(ExitCode.Success)
}
