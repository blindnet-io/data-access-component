package io.blindnet.dataaccess

import db.Migrator

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- Migrator.migrate()
      _ <- ServerApp().server.use(_ => IO.never)
    } yield ExitCode.Success
}