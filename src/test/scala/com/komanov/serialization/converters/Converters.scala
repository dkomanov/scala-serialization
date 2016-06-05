package com.komanov.serialization.converters

object Converters {

  val all: Seq[(String, MyConverter)] = Seq(
    "JSON" -> JsonConverter,
    "ScalaPB" -> ScalaPbConverter,
    "Java PB" -> JavaPbConverter,
    "Java Thrift" -> JavaThriftConverter,
    "Scrooge" -> ScroogeConverter,
    "Serializable" -> JavaSerializationConverter,
    "Pickles" -> PicklingConverter,
    "Boopickle" -> BoopickleConverter,
    "Chill" -> ChillConverter
  )

}
