package com.komanov.serialization.converters

import java.time.Instant

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser, Version}
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.module.{SimpleDeserializers, SimpleSerializers}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.komanov.serialization.domain.Site

object JsonConverter extends SiteConverter {

  private object InstantModule extends Module {
    override def getModuleName: String = "Instant"

    override def setupModule(context: SetupContext): Unit = {
      val serializers = new SimpleSerializers
      serializers.addSerializer(classOf[Instant], new JsonSerializer[Instant] {
        override def serialize(value: Instant, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
          gen.writeNumber(value.toEpochMilli)
        }
      })

      val deserializers = new SimpleDeserializers
      deserializers.addDeserializer(classOf[Instant], new JsonDeserializer[Instant] {
        override def deserialize(p: JsonParser, ctxt: DeserializationContext): Instant = {
          Instant.ofEpochMilli(p.getLongValue)
        }
      })

      context.addSerializers(serializers)
      context.addDeserializers(deserializers)
    }

    override def version(): Version = new Version(1, 0, 0, "RELEASE", "group", "artifact")
  }

  private val objectMapper = {
    val om = new ObjectMapper()
    om.registerModule(new DefaultScalaModule)
    om.registerModule(InstantModule)
    om
  }
  private val objectReader: ObjectReader = objectMapper.readerFor(classOf[Site])
  private val objectWriter: ObjectWriter = objectMapper.writerFor(classOf[Site])

  override def toByteArray(site: Site): Array[Byte] = {
    objectWriter.writeValueAsBytes(site)
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    objectReader.readValue(bytes)
  }
}
