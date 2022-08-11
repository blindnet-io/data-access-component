package io.blindnet.dataaccess
package endpoints.objects

import java.time.Instant

case class DataQueryPayload(
  selectors: List[String], // empty = everything
  subjects: List[String],
  provenance: Option[String],

  target: Option[String],
  after: Option[Instant],
  until: Option[Instant],
)
