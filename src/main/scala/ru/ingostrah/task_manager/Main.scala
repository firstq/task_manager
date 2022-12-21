package ru.ingostrah.task_manager

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.RouteConcatenation._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ru.ingostrah.task_manager.repositories.{TaskRepository, UserRepository}
import ru.ingostrah.task_manager.routes.{SwaggerDocService, TaskRoute, UserRoute}
import scalikejdbc._
import scalikejdbc.config._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.sys.exit
import scala.util.{Failure, Success}

object Main  extends App {
  implicit val system: ActorSystem[Command] = ActorSystem(MainActor(), "task-manager")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  try {
    DBs.setupAll()
    ConnectionPool.borrow()
  } catch {
    case t: Throwable =>
      system.log.error(s"database error ${t.getMessage}", t)
      exit(1)
  }

  val token = system.settings.config.getString("task-manager.x-ingostrah-task-manager-token")
  val httpHost = system.settings.config.getString("task-manager.rest.host")
  val httpPort = system.settings.config.getInt("task-manager.rest.port")
  val swaggerHost = system.settings.config.getString("task-manager.rest.swagger-host")
  val swaggerBasePath = system.settings.config.getString("task-manager.rest.swagger-base-path")

  val userRepository = new UserRepository
  userRepository.prepareTable()
  val taskRepository = new TaskRepository
  taskRepository.prepareTable()

  val swaggerDocService = new SwaggerDocService(swaggerHost, swaggerBasePath)

  val scheduler = system.scheduler

  val swagger = new SwaggerDocService(swaggerHost, swaggerBasePath)
  val bindingFuture = Http().newServerAt(httpHost, httpPort).bindFlow(
    cors()(
      pathPrefix("user") {
        new UserRoute(userRepository).routes
      } ~
        new TaskRoute(taskRepository, userRepository).routes
       ~ swaggerDocService.assets ~ swaggerDocService.routes
    )
  ).map(_.addToCoordinatedShutdown(hardTerminationDeadline = 5.seconds))

  bindingFuture.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }
}
