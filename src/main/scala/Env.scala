package io.blindnet.dataaccess

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

  lazy val azureStorageAccountName: String = sys.env("BN_AZURE_STORAGE_ACC_NAME")
  lazy val azureStorageAccountKey: String = sys.env("BN_AZURE_STORAGE_ACC_KEY")
  lazy val azureStorageContainerName: String = sys.env("BN_AZURE_STORAGE_CONT_NAME")
}

class ProductionEnv() extends Env {
  override val name: String = "production"
}

class StagingEnv() extends ProductionEnv {
  override val name: String = "staging"
}

class DevelopmentEnv() extends StagingEnv {
  override val name: String = "development"

  // Fake values for testing purposes
  override lazy val azureStorageAccountName: String = sys.env.getOrElse("BN_AZURE_STORAGE_ACC_NAME", "account_name")
  override lazy val azureStorageAccountKey: String = sys.env.getOrElse("BN_AZURE_STORAGE_ACC_KEY", "lDiergZCKWA5MvfFQ3qkGWDnFU/Ri7DSNQNJhH7mnM7TOZR7+UUJ2aAuEp7oIdAbvhMvYtR4shWO+AStAwyfmA==")
  override lazy val azureStorageContainerName: String = sys.env.getOrElse("BN_AZURE_STORAGE_CONT_NAME", "container_name")
}
