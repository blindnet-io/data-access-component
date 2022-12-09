package io.blindnet.dataaccess
package errors

import cats.data.OptionT
import cats.effect.*
import io.blindnet.identityclient.auth.AuthException
import org.http4s.*
import org.http4s.server.middleware.ErrorHandling
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.*

class ErrorHandler(env: Env) {
  val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val handler: PartialFunction[Throwable, IO[Response[IO]]] = {
    case e: BadRequestException => for {
      _ <- logger.debug(e)("Bad request exception")
    } yield Response(Status.BadRequest).condEntity(e.getMessage)

    case e: MessageFailure => for {
      _ <- logger.debug(e)("Message handling exception")
    } yield Response(Status.BadRequest).condEntity(e.getMessage)

    case e: AuthException => for {
      _ <- logger.debug(e)("Authentication exception")
    } yield Response(Status.Unauthorized).condEntity(e.getMessage)

    case e: ForbiddenException => for {
      _ <- logger.debug(e)("Forbidden exception")
    } yield Response(Status.Forbidden).condEntity(e.getMessage)

    case e: NotFoundException => for {
      _ <- logger.debug(e)("NotFound exception")
    } yield Response(Status.NotFound).condEntity(e.getMessage)

    case e: Exception => for {
      _ <- logger.error(e)("Unhandled exception")
    } yield Response(Status.InternalServerError).condEntity(e.getMessage, env.advancedLogging)
  }

  def apply(httpRoutes: HttpRoutes[IO]): HttpRoutes[IO] =
    ErrorHandling.Custom.recoverWith(httpRoutes)(handler.andThen(OptionT.liftF))
}

extension(m: Response[IO]) {
  def condEntity[T](entity: T, cond: Boolean = true)(implicit enc: EntityEncoder[IO, T]): Response[IO] =
    if cond && entity != null then m.withEntity(entity) else m
}
