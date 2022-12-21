package ru.ingostrah.task_manager.entities

import ru.ingostrah.task_manager.utils.JsonSupport

case class User(
               id: Option[Long],
               first_name: Option[String],
               last_name: Option[String],
               username: String,
               password: String,
               token: Option[String]
               )

object User extends JsonSupport {

  def apply(dao: UserDao): User = User(
    dao.id,
    dao.first_name,
    dao.last_name,
    dao.username,
    dao.password,
    dao.token
  )
}
