package com.komanov.serialization.converters

import java.io.{ByteArrayOutputStream, File}
import java.nio.file.{Files, StandardOpenOption}
import java.util.zip.GZIPOutputStream

import com.komanov.serialization.converters.IoUtils._

/*
Data Sizes (raw)
Converter,1k,ev 1k,2k,ev 2k,4k,ev 4k,8k,ev 8k,64k,ev 64k
JSON,1060,1277,2076,2499,4043,5119,8173,10961,65835,109539
Java PB,554,613,1175,1254,1930,2209,3058,3921,27111,45490
Java Thrift,712,756,1441,1530,2499,2835,4315,5363,38289,61149
Serializable,2207,2716,3311,5078,4549,11538,6615,26228,43168,240267
Pickles,1628,1565,2883,3023,5576,6284,11762,13462,97997,128797
Boopickle,544,593,1130,1220,1855,2117,2882,3655,16290,42150
Chill,908,588,1695,1260,2507,2397,3644,3981,26261,47054
Data Sizes (gzip)
Converter,1k,ev 1k,2k,ev 2k,4k,ev 4k,8k,ev 8k,64k,ev 64k
JSON,683,1364,1135,2562,1634,5259,2676,11799,11672,116005
Java PB,484,812,895,1525,1333,2848,2177,5888,9385,65902
Java Thrift,539,930,966,1762,1400,3378,2250,7105,9580,78989
Serializable,1185,2505,1744,4619,2217,9983,3040,23289,10359,216123
Pickles,686,1611,1143,2971,1604,5991,2563,13487,10843,130737
Boopickle,468,790,861,1489,1269,2748,2077,5603,9034,62488
Chill,602,795,1025,1549,1457,3077,2277,6067,9212,68199
Data Sizes
Converter,1k,ev 1k,2k,ev 2k,4k,ev 4k,8k,ev 8k,64k,ev 64k
JSON (rw),1060,1277,2076,2499,4043,5119,8173,10961,65835,109539
JSON (gz),683,1364,1135,2562,1634,5259,2676,11799,11672,116005
Java PB (rw),554,613,1175,1254,1930,2209,3058,3921,27111,45490
Java PB (gz),484,812,895,1525,1333,2848,2177,5888,9385,65902
Java Thrift (rw),712,756,1441,1530,2499,2835,4315,5363,38289,61149
Java Thrift (gz),539,930,966,1762,1400,3378,2250,7105,9580,78989
Serializable (rw),2207,2716,3311,5078,4549,11538,6615,26228,43168,240267
Serializable (gz),1185,2505,1744,4619,2217,9983,3040,23289,10359,216123
Pickles (rw),1628,1565,2883,3023,5576,6284,11762,13462,97997,128797
Pickles (gz),686,1611,1143,2971,1604,5991,2563,13487,10843,130737
Boopickle (rw),544,593,1130,1220,1855,2117,2882,3655,16290,42150
Boopickle (gz),468,790,861,1489,1269,2748,2077,5603,9034,62488
Chill (rw),908,588,1695,1260,2507,2397,3644,3981,26261,47054
Chill (gz),602,795,1025,1549,1457,3077,2277,6067,9212,68199
 */
object EventsReportGenerator extends App {

  val flush = false

  val dir = new File(new File(System.getProperty("user.home"), "123"), "events")
  require(!flush || dir.exists() || dir.mkdirs())

  val (raws, gzips, both) = (Seq.newBuilder[(String, Seq[Int])], Seq.newBuilder[(String, Seq[Int])], Seq.newBuilder[(String, Seq[Int])])

  for ((converterName, converter) <- Converters.all /*if converter ne ScroogeConverter if converter ne ScalaPbConverter*/) {
    val results = Seq.newBuilder[(Int, Int)]
    for ((name, site, events) <- TestData.all) {
      val bytes = converter.toByteArray(site)
      val gzipLen = getGzipByteLength(bytes)

      val eventsAndBytes = events.map(e => e -> converter.toByteArray(e.event))
      val eventsLen = eventsAndBytes.map(_._2.length).sum
      val eventsGzipLen = eventsAndBytes.map(_._2).map(getGzipByteLength).sum

      results += bytes.length -> gzipLen
      results += eventsLen -> eventsGzipLen

      if (flush) {
        val normalizedConverterName = converterName.toLowerCase().replace(" ", "-")
        Files.write(dir.getParentFile.toPath.resolve(s"site_${name}_$normalizedConverterName.bin"), bytes, StandardOpenOption.CREATE)
        for ((event, eventBytes) <- eventsAndBytes) {
          Files.write(dir.toPath.resolve(s"${name}_${normalizedConverterName}_${event.event.getClass.getSimpleName}.bin"), bytes, StandardOpenOption.CREATE)
        }
      }
    }

    raws += converterName -> results.result().map(_._1)
    gzips += converterName -> results.result().map(_._2)
    both += (converterName + " (rw)") -> results.result().map(_._1)
    both += (converterName + " (gz)") -> results.result().map(_._2)
  }

  println("Data Sizes (raw)")
  printHeaders
  printSizes(raws.result())

  println("Data Sizes (gzip)")
  printHeaders
  printSizes(gzips.result())

  println("Data Sizes")
  printHeaders
  printSizes(both.result())

  private def printHeaders: Any = {
    println("Converter," + TestData.sites.flatMap(t => Seq(t._1, "ev " + t._1)).mkString(","))
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
