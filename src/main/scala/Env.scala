package io.blindnet.dataaccess

import io.blindnet.identityclient.IdentityClientBuilder
import org.http4s.Uri

object Env {
  val get: Env = sys.env.getOrElse("BN_ENV", "") match
    case "production" => ProductionEnv()
    case "staging" => StagingEnv()
    case _ => DevelopmentEnv()
}

abstract class Env() {
  val name: String

  val port: Int = sys.env.getOrElse("BN_PORT", "8028").toInt
  val host: String = sys.env.getOrElse("BN_HOST", "127.0.0.1")

  val baseUrl: String = sys.env.getOrElse("BN_BASE_URL", s"http://$host:$port").stripSuffix("/")

  lazy val migrate: Boolean
  lazy val dbUri: String
  lazy val dbUsername: String
  lazy val dbPassword: String
  
  val advancedLogging: Boolean = false

  val identityUrl: Uri = sys.env.get("BN_IDENTITY_URL")
    .map(Uri.fromString.andThen(_.getOrElse(throw RuntimeException("BN_IDENTITY_URL is not an URI"))))
    .getOrElse(IdentityClientBuilder.defaultBaseUri)
  
  val globalConnectorToken: String = sys.env("BN_GLOBAL_CONNECTOR_TOKEN")

  lazy val azureStorageAccountName: String = sys.env("BN_AZURE_STORAGE_ACC_NAME")
  lazy val azureStorageAccountKey: String = sys.env("BN_AZURE_STORAGE_ACC_KEY")
  lazy val azureStorageContainerName: String = sys.env("BN_AZURE_STORAGE_CONT_NAME")

  lazy val identityKey: String = sys.env("BN_IDENTITY_KEY")
}

class ProductionEnv() extends Env {
  override val name: String = "production"

  override lazy val migrate: Boolean = sys.env.get("BN_MIGRATE").contains("yes")
  override lazy val dbUri: String = sys.env("BN_DB_URI")
  override lazy val dbUsername: String = sys.env("BN_DB_USER")
  override lazy val dbPassword: String = sys.env("BN_DB_PASSWORD")
}

class StagingEnv() extends ProductionEnv {
  override val name: String = "staging"
}

class DevelopmentEnv() extends StagingEnv {
  override val name: String = "development"

  override lazy val migrate: Boolean = sys.env.get("BN_MIGRATE").forall(_ == "yes")
  override lazy val dbUri: String = sys.env.getOrElse("BN_DB_URI", "jdbc:postgresql://127.0.0.1/dac")
  override lazy val dbUsername: String = sys.env.getOrElse("BN_DB_USER", "dac")
  override lazy val dbPassword: String = sys.env.getOrElse("BN_DB_PASSWORD", "dac")
  
  override val advancedLogging: Boolean = true

  // Fake values for testing purposes
  override lazy val azureStorageAccountName: String = sys.env.getOrElse("BN_AZURE_STORAGE_ACC_NAME", "account_name")
  override lazy val azureStorageAccountKey: String = sys.env.getOrElse("BN_AZURE_STORAGE_ACC_KEY", "lDiergZCKWA5MvfFQ3qkGWDnFU/Ri7DSNQNJhH7mnM7TOZR7+UUJ2aAuEp7oIdAbvhMvYtR4shWO+AStAwyfmA==")
  override lazy val azureStorageContainerName: String = sys.env.getOrElse("BN_AZURE_STORAGE_CONT_NAME", "container_name")
}
