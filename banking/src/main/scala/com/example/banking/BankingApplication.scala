package com.example.banking

import akka.actor.typed.ActorSystem
import akka.util.Timeout
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

object BankingApplication extends App {

  val log = LoggerFactory.getLogger(this.getClass)

  log.debug("Starting Banking App")

  implicit val askTimeout: Timeout = Timeout(5.seconds)

   ActorSystem[Nothing](Guardian.apply, "AccountActor")

}
