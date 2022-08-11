package io.blindnet.dataaccess
package endpoints.objects

import models.DataRequestActions

case class DataRequestPayload(
   //  app_id: String,
   request_id: String,
   query: DataQueryPayload,
   action: DataRequestActions.DataRequestAction,
   callback: String,
)
