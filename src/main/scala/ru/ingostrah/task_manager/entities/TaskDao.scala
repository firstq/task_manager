package ru.ingostrah.task_manager.entities

import org.joda.time.DateTime
import scalikejdbc._
import scalikejdbc.jodatime.JodaTypeBinder._
import org.json4s.native.Serialization._
import ru.ingostrah.task_manager.utils.JsonSupport

case class TaskDao(
                    id: Option[Long],
                    user_id: Option[Long],
                    title: String,
                    created_at: Option[DateTime]
                  )

object TaskDao  extends SQLSyntaxSupport[TaskDao] with JsonSupport {

  override val tableName = "tasks"

  def apply(task: SyntaxProvider[TaskDao])(rs: WrappedResultSet): TaskDao = apply(task.resultName)(rs)

  def apply(task: ResultName[TaskDao])(rs: WrappedResultSet): TaskDao =
    new TaskDao(
      id = rs.get(task.id),
      user_id = rs.get(task.user_id),
      title = rs.get(task.title),
      created_at = rs.get(task.created_at)
    )

  def apply(task: Task): TaskDao = TaskDao(
    task.id,
    task.user_id,
    task.title,
    task.created_at
  )

}
