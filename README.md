# akka-typed-persistence-sharding

This is a template that demonstrates akka persistence with cluster sharding by using Typed actors.

`Akka Persistence` enables stateful actors to persist their state. The key concept behind Akka Persistence is that only the events that are persisted by the actor are stored,
not the actual state of the actor (though actor state snapshot support is also available).

`Event Sourced Actor`

An event sourced actor (also known as a persistent actor) receives a (non-persistent) command which is first validated if it can be applied to the current state.
If validation succeeds, events are generated from the command, representing the effect of the command. These events are then persisted and, after successful persistence,
used to change the actor’s state. When the event sourced actor needs to be recovered, only the persisted events are replayed of which we know that they can be successfully applied.

`Cluster Sharding`
Cluster sharding is useful when you need to distribute actors across several nodes in the cluster and want to be able to interact with them using their logical identifier, 
but without having to care about their physical location in the cluster, which might also change over time.


In this template, I've considered a simple Banking application. There is a typed Actor named, the `AccoundActor`. That for now, just serves three types of `Commands`:
1. `DepositMoney`
2. `WithdrawMoney`
3. `CheckBalance`

Corresponding events are: `MoneyDeposited` and `MoneyWithdrawn`. No event will be persisted for CheckBalance command as it is not changing the state.

Also, there are four replies (`RequestStatus`) for each of the Commands to the `AccountActor`. They are:
1. `MoneyDepositSuccess`
2. `MoneyWithdrawnSuccess`
3. `BalanceRequestSuccess`

`BankingApplication.scala` is the starting point. It creates a top level ActorSystem that takes the behaviour from `Guardian.scala`.
Guardian is calling `depositMoney` method of `TransactionHandler` after initializing ClusterSharding. `TransactionHandler` is the place where we send different commands to the AccountActor.
 
For Serialization of commands(messages), events, and state(snapshot), this template uses Serialization with Jackson.
You can use Cbor Serializer. You can configure that in the application.conf file. Currently, it has `jackson-json` value:

    serialization-bindings {
      "com.example.banking.JsonSerializer" = jackson-json
    }
                                              


### Steps to run

1. Run Cassandra on your localhost (`./runCassandra.sh`, need docker)
2. sbt "project banking" run

