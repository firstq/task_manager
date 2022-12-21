package ru.ingostrah.task_manager.entities

import org.joda.time.DateTime
import ru.ingostrah.task_manager.utils.JsonSupport

case class Task(
                 id: Option[Long],
                 user_id: Option[Long],
                 title: String,
                 created_at: Option[DateTime]
               )

object Task extends JsonSupport {

  def apply(dao: TaskDao): Task = Task(
    dao.id,
    dao.user_id,
    dao.title,
    dao.created_at
  )
}
