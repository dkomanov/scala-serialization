package com.komanov.serialization.converters

object Converters {

  val list: Seq[(String, SiteConverter)] = Seq(
    "JSON" -> JsonConverter,
    "ScalaPB" -> ScalaPbConverter,
    "Java PB" -> JavaPbConverter,
    "Serializable" -> JavaSerializationConverter,
    "Pickles" -> PicklingConverter
  )

}
