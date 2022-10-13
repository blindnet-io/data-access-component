package io.blindnet.dataaccess
package endpoints.objects

import models.DataRequestAction

case class DataRequestPayload(
   request_id: String,
   query: DataQueryPayload,
   action: DataRequestAction,
   callback: String,
)
