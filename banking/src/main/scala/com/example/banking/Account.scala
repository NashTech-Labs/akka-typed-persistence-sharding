package com.example.banking

import play.api.libs.json.{Format, Json}

case class Account(id: Long, holderName: String, amount: Long)

object Account {
  implicit val format: Format[Account] = Json.format
}