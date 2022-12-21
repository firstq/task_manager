package ru.ingostrah.task_manager.repositories

import ru.ingostrah.task_manager.entities.TaskDao

class TaskRepository {

  import scalikejdbc._

  implicit val session = AutoSession

  val syntax = TaskDao.syntax

  def prepareTable() = try {
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE if not exists tasks (
          id SERIAL PRIMARY KEY,
          user_id int,
          title varchar(100),
          created_at TIMESTAMP NOT NULL DEFAULT NOW()
        );
        """
        .execute.apply()
      sql"CREATE INDEX if not exists user_id_index ON tasks (user_id)".execute.apply()
    }
  } catch {
    case t:Throwable => t.printStackTrace
  }

  def insertTask(taskDao: TaskDao): Long = {
    val c = TaskDao.column
    withSQL {
      insert.into(TaskDao)
        .namedValues(c.title -> taskDao.title,
          c.user_id -> taskDao.user_id)
    }.updateAndReturnGeneratedKey.apply()
  }

  def selectAll: List[TaskDao] = DB readOnly {
    implicit session =>
      withSQL {
        select.all[TaskDao].from(TaskDao as syntax)
      }.map(TaskDao(syntax)).list.apply()
  }

  def selectAllByUserId(user_id: Option[Long]): List[TaskDao] = DB readOnly {
    implicit session =>
      val c = TaskDao.column
      withSQL {
        select.all[TaskDao].from(TaskDao as syntax).where.eq(c.user_id, user_id)
      }.map(TaskDao(syntax)).list.apply()
  }

  def selectByIds(ids: List[Int]): List[TaskDao] = DB readOnly {
    implicit session =>
      val c = TaskDao.column
      withSQL {
        select.all[TaskDao].from(TaskDao as syntax).where.in(c.id, ids)
      }.map(TaskDao(syntax)).list.apply()
  }

  def selectById(id: Long):TaskDao = DB readOnly {
    implicit session =>
      val c = TaskDao.column
      withSQL {
        select.all[TaskDao].from(TaskDao as syntax).where.eq(c.id, id)
      }.map(TaskDao(syntax)).list.apply().head
  }

  def deleteAll: Unit = sql"DELETE FROM tasks".update.apply()

  def deleteById(id: String): Unit = sql"DELETE FROM tasks WHERE id=${id}".update.apply()

  def deleteByIds(ids: List[String]): Unit = sql"DELETE FROM tasks WHERE id IN (${ids})".update.apply()

}
