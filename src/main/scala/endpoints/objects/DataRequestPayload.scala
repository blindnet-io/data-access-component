package io.blindnet.dataaccess
package endpoints.objects

case class DataRequestPayload(
   query: DataQueryPayload,

   //  app_id: String,
   request_id: String,
   callback: String,
)
