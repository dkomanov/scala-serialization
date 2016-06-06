package com.komanov.serialization.converters

import java.io.ByteArrayOutputStream

import com.komanov.serialization.domain.SiteEventData
import org.apache.commons.io.HexDump
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.specification.core.Fragments

class SerializationTest extends Specification {

  sequential

  doSiteTest("JSON", JsonConverter)
  doSiteTest("ScalaPB", ScalaPbConverter)
  doSiteTest("Java Protobuf", JavaPbConverter)
  doSiteTest("Java Thrift", JavaThriftConverter)
  doSiteTest("Scrooge", ScroogeConverter)
  doSiteTest("Serializable", JavaSerializationConverter)
  doSiteTest("Pickles", PicklingConverter)
  doSiteTest("Boopickle", BoopickleConverter)
  doSiteTest("Chill", ChillConverter)

  doEventTest("JSON", JsonConverter)
  doEventTest("ScalaPB", ScalaPbConverter)
  doEventTest("Java Protobuf", JavaPbConverter)
  doEventTest("Java Thrift", JavaThriftConverter)
  doEventTest("Scrooge", ScroogeConverter)
  doEventTest("Serializable", JavaSerializationConverter)
  doEventTest("Pickles", PicklingConverter)
  doEventTest("Boopickle", BoopickleConverter)
  doEventTest("Chill", ChillConverter)

  "ScalaPB and Java Protobuf" should {
    Fragments.foreach(TestData.sites) { case (name, site) =>
      s"be interoperable for site of $name" in new ctx {
        val javaMessage = JavaPbConverter.toByteArray(site)
        val scalaMessage = ScalaPbConverter.toByteArray(site)
        toHexDump(javaMessage) must be_===(toHexDump(scalaMessage))
      }
    }

    Fragments.foreach(TestData.events) { case (name, events) =>
      s"be interoperable events of $name" in new ctx {
        for (event <- events) {
          val javaMessage = JavaPbConverter.toByteArray(event)
          val scalaMessage = ScalaPbConverter.toByteArray(event)
          toHexDump(javaMessage) must be_===(toHexDump(scalaMessage))
        }
      }
    }
  }

  "Scrooge and Java Thrift" should {
    Fragments.foreach(TestData.sites) { case (name, site) =>
      s"be interoperable for site of $name" in new ctx {
        val javaMessage = JavaThriftConverter.toByteArray(site)
        val scalaMessage = ScroogeConverter.toByteArray(site)
        toHexDump(javaMessage) must be_===(toHexDump(scalaMessage))
      }
    }

    Fragments.foreach(TestData.events) { case (name, events) =>
      s"be interoperable events of $name" in new ctx {
        for (event <- events) {
          val javaMessage = JavaThriftConverter.toByteArray(event)
          val scalaMessage = ScroogeConverter.toByteArray(event)
          toHexDump(javaMessage) must be_===(toHexDump(scalaMessage))
        }
      }
    }
  }

  class ctx extends Scope

  def toHexDump(arr: Array[Byte]): String = {
    val baos = new ByteArrayOutputStream
    HexDump.dump(arr, 0, baos, 0)
    new String(baos.toByteArray)
  }

  def doSiteTest(converterName: String, converter: SiteConverter) = {
    converterName should {
      Fragments.foreach(TestData.sites) { case (name, site) =>
        s"serialize-parse site of $name" in new ctx {
          val bytes = converter.toByteArray(site)
          println(s"$converterName ($name): ${bytes.length}")
          val parsed = converter.fromByteArray(bytes)
          parsed must be_===(site)
        }
      }
    }
  }

  def doEventTest(converterName: String, converter: EventConverter) = {
    converterName should {
      Fragments.foreach(TestData.events) { case (name, events) =>
        s"serialize-parse events of $name" in new ctx {
          for (event <- events) {
            val bytes = converter.toByteArray(event)
            val parsed = converter.eventFromByteArray(bytes)
            parsed must be_===(event)
          }
        }
      }
    }
  }

}
