package ru.ingostrah.task_manager.entities

import org.joda.time.DateTime
import scalikejdbc._
import scalikejdbc.jodatime.JodaTypeBinder._
import org.json4s.native.Serialization._
import ru.ingostrah.task_manager.utils.JsonSupport

case class UserDao(
                    id: Option[Long],
                    first_name: Option[String],
                    last_name: Option[String],
                    username: String,
                    password: String,
                    token: Option[String]
                  )

object UserDao extends SQLSyntaxSupport[UserDao] with JsonSupport {

  override val tableName = "users"

  def apply(user: SyntaxProvider[UserDao])(rs: WrappedResultSet): UserDao = apply(user.resultName)(rs)

  def apply(user: ResultName[UserDao])(rs: WrappedResultSet): UserDao =
    new UserDao(
      id = rs.get(user.id),
      first_name = rs.get(user.first_name),
      last_name = rs.get(user.last_name),
      username = rs.get(user.username),
      password = rs.get(user.password),
      token = rs.get(user.token)
    )

  def apply(user: User): UserDao = UserDao(
    user.id,
    user.first_name,
    user.last_name,
    user.username,
    user.password,
    user.token
  )

}
