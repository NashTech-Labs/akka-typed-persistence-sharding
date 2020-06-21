package com.example.banking

import akka.actor.typed.scaladsl.ActorContext
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.util.Timeout
import com.example.banking.AccountActor.{MoneyDepositSuccess, MoneyWithdrawnSuccess}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TransactionHandler(context: ActorContext[Nothing]) {

  val log: Logger = LoggerFactory.getLogger(getClass)
  val sharding = ClusterSharding(context.system)

  implicit val askTimeout: Timeout = Timeout(5.seconds)

  def depositMoney(accNumber: Long, amount: Long) =
    sharding.entityRefFor(AccountActor.TypeKey, accNumber.toString)
      .ask[MoneyDepositSuccess](AccountActor.DepositMoney(accNumber, amount, _))
      .foreach(d => log.debug(s"Amount deposited: ${d.amountDeposited}"))


  def withdrawMoney(accNumber: Long, amount: Long) =
    sharding.entityRefFor(AccountActor.TypeKey, accNumber.toString)
      .ask[MoneyWithdrawnSuccess](AccountActor.WithdrawMoney(accNumber, amount, _))
      .map {
      result =>
        log.debug(s"Remaining Balance: ${result.finalAmount}")
        result
    }
}
