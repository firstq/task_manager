package ru.ingostrah.task_manager.repositories

import ru.ingostrah.task_manager.entities.UserDao

class UserRepository {

  import scalikejdbc._

  implicit val session = AutoSession

  val syntax = UserDao.syntax

  def prepareTable() = try {
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE if not exists users (
         id SERIAL PRIMARY KEY,
         first_name varchar(50),
         last_name varchar(50),
         username varchar(50),
         password varchar(50),
         token varchar(100) DEFAULT Null
        )
        """
        .execute.apply()
      sql"CREATE UNIQUE INDEX if not exists username_uniq ON users (username)".execute.apply()
      sql"CREATE UNIQUE INDEX if not exists token_uniq ON users (token)".execute.apply()
    }
  } catch {
    case t:Throwable => t.printStackTrace
  }

  def insertUser(userDao: UserDao): Long = {
    val c = UserDao.column
    withSQL {
      insert.into(UserDao)
        .namedValues(c.first_name -> userDao.first_name,
          c.last_name -> userDao.last_name,
          c.username -> userDao.username,
          c.password -> userDao.password)
    }.updateAndReturnGeneratedKey.apply()
  }

  def selectAll: List[UserDao] = DB readOnly {
    implicit session =>
      withSQL {
        select.all[UserDao].from(UserDao as syntax)
      }.map(UserDao(syntax)).list.apply()
  }

  def selectByIds(ids: List[Int]): List[UserDao] = DB readOnly {
    implicit session =>
      val c = UserDao.column
      withSQL {
        select.all[UserDao].from(UserDao as syntax).where.in(c.id, ids)
      }.map(UserDao(syntax)).list.apply()
  }

  def selectById(id: Long): UserDao = DB readOnly {
    implicit session =>
      val c = UserDao.column
      withSQL {
        select.all[UserDao].from(UserDao as syntax).where.eq(c.id, id)
      }.map(UserDao(syntax)).list.apply().head
  }

  def selectByUsernamePass(username: String, password: String): List[UserDao] = DB readOnly {
    implicit session =>
      val c = UserDao.column
      withSQL {
        select.all[UserDao].from(UserDao as syntax).where.eq(c.username, username).and.eq(c.password, password)
      }.map(UserDao(syntax)).list.apply()
  }

  def selectByToken(token: String): List[UserDao] = DB readOnly {
    implicit session =>
      val c = UserDao.column
      withSQL {
        select.all[UserDao].from(UserDao as syntax).where.eq(c.token, token)
      }.map(UserDao(syntax)).list.apply()
  }

  def update(user: UserDao): Unit = {
    val c = UserDao.column
    withSQL {
      QueryDSL.update(UserDao)
        .set(c.first_name -> user.first_name,
          c.last_name -> user.last_name,
          c.username -> user.username,
          c.password -> user.password,
          c.token -> user.token)
        .where.eq(c.id, user.id)
    }.update.apply()
  }

  def deleteAll: Unit = sql"DELETE FROM users".update.apply()

  def deleteById(id: String): Unit = sql"DELETE FROM users WHERE id=${id}".update.apply()

  def deleteByIds(ids: List[String]): Unit = sql"DELETE FROM users WHERE id IN (${ids})".update.apply()

}
