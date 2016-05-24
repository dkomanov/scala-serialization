package com.komanov.serialization.converters

import java.io.ByteArrayOutputStream

import org.apache.commons.io.HexDump
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.specification.core.Fragments

class SerializationTest extends Specification {

  sequential

  doTest("JSON", JsonConverter)
  doTest("ScalaPB", ScalaPbConverter)
  doTest("Java Protobuf", JavaPbConverter)

  "ScalaPB and Java Protobuf" should {
    Fragments.foreach(TestData.all) { case (name, site) =>
      s"be interoperable for site of $name" in new ctx {
        val javaMessage = JavaPbConverter.toByteArray(site)
        val scalaMessage = ScalaPbConverter.toByteArray(site)
        toHexDump(javaMessage) must be_===(toHexDump(scalaMessage))
      }
    }
  }

  class ctx extends Scope

  def toHexDump(arr: Array[Byte]): String = {
    val baos = new ByteArrayOutputStream
    HexDump.dump(arr, 0, baos, 0)
    new String(baos.toByteArray)
  }

  def doTest(converterName: String, converter: SiteConverter) = {
    converterName should {
      Fragments.foreach(TestData.all) { case (name, site) =>
        s"serialize-parse site of $name" in new ctx {
          val bytes = converter.toByteArray(site)
          println(s"$converterName ($name): ${bytes.length}")
          val parsed = converter.fromByteArray(bytes)
          parsed must be_===(site)
        }
      }
    }
  }

}
