package com.komanov.serialization.converters

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, StandardOpenOption}
import java.util.zip.GZIPOutputStream

import com.komanov.serialization.converters.IoUtils._

/*
Data Sizes (raw)
Converter,1k,2k,4k,8k,64k
JSON,1060,2076,4043,8173,65835
ScalaPB,554,1175,1930,3058,27111
Java PB,554,1175,1930,3058,27111
Java Thrift,712,1441,2499,4315,38289
Scrooge,712,1441,2499,4315,38289
Serializable,2207,3311,4549,6615,43168
Pickles,1628,2883,5576,11762,97997
Boopickle,544,1130,1855,2882,16290
Chill,908,1695,2507,3644,26260
Data Sizes (gzip)
Converter,1k,2k,4k,8k,64k
JSON,681,1133,1633,2677,11649
ScalaPB,486,896,1339,2187,9394
Java PB,486,896,1339,2187,9394
Java Thrift,542,964,1406,2268,9608
Scrooge,542,964,1406,2268,9608
Serializable,1189,1741,2224,3053,10354
Pickles,688,1144,1610,2570,10862
Boopickle,470,859,1276,2092,9048
Chill,598,1022,1456,2283,9207
 */
object ReportGenerator extends App {

  val flush = false

  val dir = new File(System.getProperty("user.home"), "123")
  require(!flush || dir.exists() || dir.mkdirs())

  val (raws, gzips) = (Seq.newBuilder[(String, Seq[Int])], Seq.newBuilder[(String, Seq[Int])])

  for ((converterName, converter) <- Converters.list) {
    val results = Seq.newBuilder[(Int, Int)]
    for ((name, site) <- TestData.sites) {
      val bytes = converter.toByteArray(site)
      val gzipLen = getGzipByteLength(bytes)

      results += bytes.length -> gzipLen

      if (flush) {
        val normalizedConverterName = converterName.toLowerCase().replace(" ", "-")
        Files.write(dir.toPath.resolve(s"site_${name}_$normalizedConverterName.bin"), bytes, StandardOpenOption.CREATE)
      }
    }

    raws += converterName -> results.result().map(_._1)
    gzips += converterName -> results.result().map(_._2)
  }

  println("Data Sizes (raw)")
  printHeaders
  printSizes(raws.result())

  println("Data Sizes (gzip)")
  printHeaders
  printSizes(gzips.result())

  private def printHeaders: Any = {
    println("Converter," + TestData.sites.map(_._1).mkString(","))
  }

  private def printSizes(all: Seq[(String, Seq[Int])]): Unit = {
    for ((name, list) <- all) {
      println(name + "," + list.mkString(","))
    }
  }

  private def getGzipByteLength(bytes: Array[Byte]): Int = {
    using(new ByteArrayOutputStream()) { baos =>
      using(new GZIPOutputStream(baos)) { os =>
        os.write(bytes)
      }
      baos.toByteArray.length
    }
  }

}
