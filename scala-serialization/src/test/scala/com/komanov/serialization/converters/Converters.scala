package com.komanov.serialization.converters

object Converters {

  val all: Seq[(String, MyConverter)] = Seq(
    "KryoMacros" -> KryoMacrosConverter,
    "JsoniterScala" -> JsoniterScalaConverter,
    "JSON" -> JsonConverter,
    "ScalaPB" -> ScalaPbConverter,
    "Java PB" -> JavaPbConverter,
    "Java Thrift" -> JavaThriftConverter,
    "Scrooge" -> ScroogeConverter,
    "Serializable" -> JavaSerializationConverter,
    "Pickling" -> PicklingConverter,
    "BooPickle" -> BoopickleConverter,
    "Chill" -> ChillConverter
  )

}
