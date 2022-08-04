package io.blindnet.dataaccess
package objects

import java.time.Instant

case class DataQuery(
  selectors: List[String], // empty = everything
  subjects: List[String],
  provenance: Option[String],

  target: Option[String],
  after: Option[Instant],
  until: Option[Instant],

//  app_id: String,
  request_id: String,
  callback: String,
)
