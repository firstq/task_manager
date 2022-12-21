package ru.ingostrah.task_manager.routes

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, Unauthorized}
import akka.http.scaladsl.server.Directives
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.{ApiResponse, ApiResponses}
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import ru.ingostrah.task_manager.entities.{User, UserDao}
import ru.ingostrah.task_manager.repositories.UserRepository
import ru.ingostrah.task_manager.utils.JsonSupport

import javax.ws.rs.core.MediaType
import javax.ws.rs._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

@ApiResponses
class UserRoute(val userRepository: UserRepository)(
  implicit val system: ActorSystem[_], executionContext: ExecutionContextExecutor) extends Directives with JsonSupport{

  @POST
  @Path("/user/sign-up")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Add user", description = "Add user",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[User])))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Add user in database",
        content = Array(new Content(schema = new Schema(implementation = classOf[Map[String, Long]])))),
      new ApiResponse(responseCode = "400", description = "Bad Request"),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "404", description = "Not Found"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @ApiResponse
  def userSignUpRoute =
    path("sign-up") {
      post {
        entity(as[User]) { request =>
          onComplete(Future{userRepository.insertUser(UserDao(request))}) {
            case Success(userId) => complete(Map("id" -> userId))
            case Failure(ex: Exception) =>
              val msg = s"An error occurred: ${ex.getMessage}"
              system.log.error(msg, ex)
              complete(InternalServerError, msg)
          }
        }
      }
    }

  @POST
  @Path("/user/token")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Create token", description = "Create token",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[User])))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Create token in database",
        content = Array(new Content(schema = new Schema(implementation = classOf[Map[String, Long]])))),
      new ApiResponse(responseCode = "400", description = "Bad Request"),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "404", description = "Not Found"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  @ApiResponse
  def userTokenRoute =
    path("token") {
      post {
        entity(as[User]) { request =>
          onComplete(
            Future{
              val userDaoList = userRepository.selectByUsernamePass(request.username, request.password)
              userDaoList match {
                case List(userDao) =>
                  val token = java.util.UUID.randomUUID.toString.replaceAll("-","")
                  val copyUserDao = userDao.copy(token = Some(token))
                  userRepository.update(copyUserDao)
                  token
                case _ => throw new Exception("Smth wrong!")
              }
            }
          ) {
            case Success(token) =>
              complete(Map("access_token" -> token))
            case Failure(ex: Exception) =>
              val msg = s"An error occurred: ${ex.getMessage}"
              system.log.error(msg, ex)
              complete(InternalServerError, msg)
          }
        }
      }
    }

  @GET
  @Path("/user/profile")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Get profile", description = "Get profile",
    parameters = Array(new Parameter(name = "x-ingostrah-task-manager-token", in = ParameterIn.HEADER, description = "Authorization token")),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "User profile",
        content = Array(new Content(schema = new Schema(implementation = classOf[User])))),
      new ApiResponse(responseCode = "400", description = "Bad Request"),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "404", description = "Not Found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  @ApiResponse
  def profileGetRoute =
    path("profile") {
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
                  case List(userDao) => complete(User(userDao))
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

  val routes = userSignUpRoute ~ userTokenRoute ~ profileGetRoute

}
