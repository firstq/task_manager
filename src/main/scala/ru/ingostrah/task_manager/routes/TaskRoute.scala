package ru.ingostrah.task_manager.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, Unauthorized}
import akka.http.scaladsl.server.Directives
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.{ApiResponse, ApiResponses}
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import ru.ingostrah.task_manager.entities.{Task, TaskDao, User, UserDao}
import ru.ingostrah.task_manager.repositories.{TaskRepository, UserRepository}
import ru.ingostrah.task_manager.utils.JsonSupport

import javax.ws.rs.core.MediaType
import javax.ws.rs.{Consumes, GET, POST, Path, Produces}
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

@ApiResponses
class TaskRoute(val taskRepository: TaskRepository, userRepository: UserRepository)(
  implicit val system: ActorSystem[_], executionContext: ExecutionContextExecutor) extends Directives with JsonSupport {

  @POST
  @Path("/task")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Add task", description = "Add task",
    parameters = Array(new Parameter(name = "x-ingostrah-task-manager-token", in = ParameterIn.HEADER, description = "Authorization token")),
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Task])))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Add task in database",
        content = Array(new Content(schema = new Schema(implementation = classOf[Map[String, Long]])))),
      new ApiResponse(responseCode = "400", description = "Bad Request"),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "404", description = "Not Found"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @ApiResponse
  def taskAddRoute =
    path("task") {
      post {
        entity(as[Task]) {
          request =>
            headerValue(extractToken) {
              value => value match {
                case token: String if token.nonEmpty =>
                  onComplete(
                    Future {userRepository.selectByToken(token)}
                  ) {
                    case Success(userList) => userList match {
                      case List(userDao) =>
                        onComplete(Future{taskRepository.insertTask(TaskDao(request.copy(user_id = userDao.id)))}) {
                          case Success(taskId) => complete(Map("id" -> taskId))
                          case Failure(ex: Exception) =>
                            val msg = s"An error occurred: ${ex.getMessage}"
                            system.log.error(msg, ex)
                            complete(InternalServerError, msg)
                        }
                      case _ => complete(NotFound, "Not found")
                    }
                    case Failure(ex: Exception) =>
                      val msg = s"An error occurred: ${ex.getMessage}"
                      system.log.error(msg, ex)
                      complete(InternalServerError, msg)
                  }
                case _ => complete(Unauthorized, s"Wrong token")
              }
            }
        }
      }
    }

  @GET
  @Path("/tasks")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Get tasks", description = "Get tasks",
    parameters = Array(new Parameter(name = "x-ingostrah-task-manager-token", in = ParameterIn.HEADER, description = "Authorization token")),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "User tasks",
        content = Array(new Content(schema = new Schema(implementation = classOf[List[TaskDao]])))),
      new ApiResponse(responseCode = "400", description = "Bad Request"),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "404", description = "Not Found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  @ApiResponse
  def tasksGetRoute =
    path("tasks") {
      get {
        headerValue(extractToken) {
          value => value match {
            case token: String if token.nonEmpty =>
              onComplete(
                Future {
                  userRepository.selectByToken(token)
                }
              ) {
                case Success(userList) => userList match {
                  case List(userDao) =>
                    complete(taskRepository.selectAllByUserId(userDao.id))
                  case _ => complete(NotFound, "Not found")
                }
                case Failure(ex: Exception) =>
                  val msg = s"An error occurred: ${ex.getMessage}"
                  system.log.error(msg, ex)
                  complete(InternalServerError, msg)
              }
            case _ => complete(Unauthorized, s"Wrong token")
          }
        }
      }
    }

  @GET
  @Path("tasks/{task-id}")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Get tasks", description = "Get tasks",
    parameters = Array(
      new Parameter(name = "x-ingostrah-task-manager-token", in = ParameterIn.HEADER, description = "Authorization token"),
      new Parameter(name = "task-id", in = ParameterIn.PATH, description = "Task id")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "User tasks",
        content = Array(new Content(schema = new Schema(implementation = classOf[TaskDao])))),
      new ApiResponse(responseCode = "400", description = "Bad Request"),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "404", description = "Not Found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  @ApiResponse
  def taskGetByIdRoute = path("tasks" / IntNumber) { (taskId) =>
    get {
      validate(taskId > 0,"The task id should be greater than zero") {
        headerValue(extractToken) {
          value => value match {
            case token: String if token.nonEmpty =>
              onComplete(
                Future {taskRepository.selectById(taskId)}
              ) {
                case Success(taskDao) => complete(Task(taskDao))
                case Failure(ex: Exception) =>
                  val msg = s"An error occurred: ${ex.getMessage}"
                  system.log.error(msg, ex)
                  complete(InternalServerError, msg)
              }
            case _ => complete(Unauthorized, s"Wrong token")
          }
        }
      }
    }
  }

  val routes = taskAddRoute ~ tasksGetRoute ~ taskGetByIdRoute

}
