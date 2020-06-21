package com.example.banking

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

/**
  * Root actor bootstrapping the application
  */
object Guardian {

  def apply: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>

    AccountActor.initSharding(context.system)
    val handler = new TransactionHandler(context)
    // plug-in your data source here
    handler.depositMoney(accNumber = 104560, amount = 10000)

    Behaviors.empty
  }
}