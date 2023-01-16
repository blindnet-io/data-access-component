package io.blindnet.dataaccess

import cats.effect.*
import cats.effect.std.Random

def generateStaticToken(): IO[String] =
  for {
    random <- Random.javaSecuritySecureRandom[IO]
    token  <- random.nextAlphaNumeric.replicateA(128).map(_.mkString)
  } yield token
