package io.blindnet.dataaccess
package azure

import cats.effect.IO
import com.azure.storage.common.StorageSharedKeyCredential

case class AzureSAS(
  verb: String,
  resource: String,

  contentEncoding: String = "",
  contentLanguage: String = "",
  contentLength: String = "",
  contentMd5: String = "",
  contentType: String = "",
  date: String = "",
  ifModifiedSince: String = "",
  ifMatch: String = "",
  ifNoneMatch: String = "",
  ifUnmodifiedSince: String = "",
  range: String = "",

  headers: Map[String, String] = Map.empty,
  params: Map[String, String] = Map.empty,
) {
  def contentEncoding(v: String): AzureSAS = copy(contentEncoding = v)
  def contentLanguage(v: String): AzureSAS = copy(contentLanguage = v)
  def contentLength(v: String): AzureSAS = copy(contentLength = v)
  def contentMd5(v: String): AzureSAS = copy(contentMd5 = v)
  def contentType(v: String): AzureSAS = copy(contentType = v)
  def date(v: String): AzureSAS = copy(date = v)
  def ifModifiedSince(v: String): AzureSAS = copy(ifModifiedSince = v)
  def ifMatch(v: String): AzureSAS = copy(ifMatch = v)
  def ifNoneMatch(v: String): AzureSAS = copy(ifNoneMatch = v)
  def ifUnmodifiedSince(v: String): AzureSAS = copy(ifUnmodifiedSince = v)
  def range(v: String): AzureSAS = copy(range = v)

  def add(header: String, v: String): AzureSAS = copy(headers = headers + (header -> v))
  def param(k: String, v: String): AzureSAS = copy(params = params + (k -> v))

  def sign(credential: StorageSharedKeyCredential): IO[String] = IO {
    val l = List(verb,
      contentEncoding, contentLanguage, contentLength, contentMd5, contentType,
      date, ifModifiedSince, ifMatch, ifNoneMatch, ifUnmodifiedSince, range)
      ++ headers.map((header, v) => header + ":" + v)
      ++ List(resource)
      ++ params.map((k, v) => k + ":" + v)

    credential.computeHmac256(l.mkString("\n"))
  }
}

object AzureSAS {
  def of(verb: String, resource: String): AzureSAS = AzureSAS(verb, resource)

  def delete(resource: String): AzureSAS = of("DELETE", resource)
  def get(resource: String): AzureSAS = of("GET", resource)
  def put(resource: String): AzureSAS = of("PUT", resource)
}

