package io.blindnet.dataaccess
package endpoints.objects

import models.DataRequestAction

case class DataRequestPayload(
   //  app_id: String,
   request_id: String,
   query: DataQueryPayload,
   action: DataRequestAction,
   callback: String,
)
