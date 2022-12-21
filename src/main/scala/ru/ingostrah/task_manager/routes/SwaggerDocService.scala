package ru.ingostrah.task_manager.routes

import akka.http.scaladsl.model.StatusCodes
import com.github.swagger.akka._
import com.github.swagger.akka.model.Info

import scala.collection.immutable.Set

class SwaggerDocService(swaggerHost: String, swaggerBasePath: String) extends SwaggerHttpService {
  override val apiClasses: Set[Class[_]] = Set(
    classOf[UserRoute],
    classOf[TaskRoute]
  )
  override val host: String = swaggerHost
  override val basePath: String = swaggerBasePath
  override val schemes: List[String] = List("http")
  override val info: Info = Info(title = "Task Manager")

  def assets = pathPrefix("swagger") {
    getFromResourceDirectory("dist") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect)))
  }
}
