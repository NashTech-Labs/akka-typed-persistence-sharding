package com.example.banking

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.sharding.external.ExternalShardAllocationStrategy
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import org.slf4j.{Logger, LoggerFactory}

object AccountActor {

  implicit val log: Logger = LoggerFactory.getLogger(getClass)

  //Command
  trait Command extends JsonSerializer

  case class DepositMoney(accountNumber: Long, amount: Long, replyTo: ActorRef[MoneyDepositSuccess]) extends Command

  case class WithdrawMoney(accountNumber: Long, amount: Long, replyTo: ActorRef[MoneyWithdrawnSuccess]) extends Command

  case class CheckBalance(accountNumber: Long, replyTo: ActorRef[BalanceRequestSuccess]) extends Command

  case object Closing extends Command


  //Response
  trait RequestStatus extends JsonSerializer

  case class MoneyDepositSuccess(amountDeposited: Long) extends RequestStatus

  case class MoneyWithdrawnSuccess(finalAmount: Long) extends RequestStatus

  case class BalanceRequestSuccess(accountDetails: Account) extends RequestStatus

  //Event
  trait Event extends JsonSerializer

  case class MoneyDeposited(amount: Long) extends Event

  case class MoneyWithdrawn(amount: Long) extends Event

  //State
  case class AccountActorState(accountDetails: Account)

  object AccountActorState {
    val empty = AccountActorState(Account(0, "", 0))
  }


  private val commandHandler: (AccountActorState, Command) => Effect[Event, AccountActorState] = { (state, cmd) =>
    cmd match {
      case cmd: Command => handleCommand(state, cmd)
      case _ => throw new Exception("Unknown Command.")
    }
  }

  private def handleCommand(state: AccountActorState, cmd: Command): Effect[Event, AccountActorState] =
    cmd match {

      case DepositMoney(accountNumber, amount, replyTo) =>
        log.debug(s"Command received: DepositMoney for account number: $accountNumber")
        Effect
          .persist(MoneyDeposited(amount))
          .thenReply(replyTo)(_ => MoneyDepositSuccess(amount))

      case WithdrawMoney(accountNumber, amount, replyTo) =>
        log.debug(s"Command received: WithdrawMoney for account number: $accountNumber")
        Effect
          .persist(MoneyWithdrawn(amount))
          .thenReply(replyTo)(state => MoneyWithdrawnSuccess(state.accountDetails.amount))

      case CheckBalance(accountNumber, replyTo) =>
        log.debug(s"Command received: CheckBalance for account number: $accountNumber")
        Effect
          .none
          .thenReply(replyTo)(state => BalanceRequestSuccess(state.accountDetails))

      case Closing => Effect.stop()

    }


  private def handleEvent(state: AccountActorState, event: Event): AccountActorState = {

    event match {
      case MoneyDeposited(depositedAmount) =>
        val calculatedAmount = state.accountDetails.amount + depositedAmount
        state.copy(accountDetails = state.accountDetails.copy(amount = calculatedAmount))

      case MoneyWithdrawn(withdrawAmount) =>
        val calculatedAmount = state.accountDetails.amount - withdrawAmount
        state.copy(accountDetails = state.accountDetails.copy(amount = calculatedAmount))

    }
  }

  private val eventHandler: (AccountActorState, Event) => AccountActorState = { (state, evt) =>
    handleEvent(state, evt)
  }

  val TypeKey: EntityTypeKey[Command] =
    EntityTypeKey[Command]("Account")

  def initSharding(system: ActorSystem[_]): Unit = {
    log.debug(s"Initializing Sharding . . .")

    ClusterSharding(system).init(Entity(TypeKey)(createBehavior = entityContext =>
      AccountActor(entityContext.entityId))
      .withAllocationStrategy(new ExternalShardAllocationStrategy(system, TypeKey.name))
      .withStopMessage(Closing))
  }

  def apply(entityId: String): Behavior[Command] = Behaviors.setup { _ =>
    log.debug("Starting Account Actor {}", entityId)
    EventSourcedBehavior(
      PersistenceId("Account", entityId),
      AccountActorState.empty,
      commandHandler,
      eventHandler
    )
  }
}
