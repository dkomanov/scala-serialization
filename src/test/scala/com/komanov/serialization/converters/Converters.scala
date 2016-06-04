package com.komanov.serialization.converters

object Converters {

  val list: Seq[(String, SiteConverter)] = Seq(
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

  val events: Seq[(String, EventConverter)] = list.collect {
    case (name, converter: EventConverter) => name -> converter
  }

}
