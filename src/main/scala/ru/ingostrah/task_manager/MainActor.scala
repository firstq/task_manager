package ru.ingostrah.task_manager

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

trait Command

object MainActor {

  def apply(): Behavior[Command] =
    Behaviors.setup(context => new MainActor(context))

}

class MainActor(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  override def onMessage(msg: Command): Behavior[Command] =
    msg match {
      case _ => this
    }

}
