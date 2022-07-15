package io.blindnet.dataaccess
package objects

import java.time.Instant

case class DataQuery(
  selectors: List[String],
  subjects: List[String],
  provenance: Option[String],
  target: Option[String],
  after: Option[Instant],
  until: Option[Instant],
  request_id: String,
)
