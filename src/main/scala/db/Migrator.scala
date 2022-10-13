package io.blindnet.dataaccess
package db

import cats.effect.IO
import org.flywaydb.core.Flyway

object Migrator {
  def migrate(): IO[Unit] = IO {
    Flyway.configure()
      .dataSource(Env.get.dbUri, Env.get.dbUsername, Env.get.dbPassword)
      .group(true)
      .load()
      .migrate()
  }
}
