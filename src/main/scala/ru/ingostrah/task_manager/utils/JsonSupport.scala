package ru.ingostrah.task_manager.utils

import akka.http.scaladsl.model.HttpHeader
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.native.Serialization
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization._

trait JsonSupport extends Json4sSupport {
  implicit val serialization: Serialization.type = native.Serialization
  implicit val formats: Formats = DefaultFormats.preservingEmptyValues

  def toJsonStr(value: Map[String, Any]): Option[String] = Some(write(value))

  def parseJsonStr(json: String): Option[Map[String, Any]] = Some(parse(json).extract[Map[String, Any]])

  def extractToken: HttpHeader => Option[String] = {
    case HttpHeader("x-ingostrah-task-manager-token", value) =>
      Some(value)
    case _ => None
  }

}
