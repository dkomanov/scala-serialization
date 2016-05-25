package com.komanov.serialization.converters

import com.komanov.serialization.domain.Site

trait BasePerfTest[Input, Output] {

  val N = 100000

  def createInput(converter: SiteConverter, site: Site): Input

  def convert(converter: SiteConverter, input: Input): Output

  def doTest(): Unit = {
    println("Warming up...")
    doWarmUp()

    println("Testing!")

    println("Converter," + TestData.all.map(_._1).mkString(","))

    for ((converterName, converter) <- Converters.list) {
      val results = for {
        (name, site) <- TestData.all
        input = createInput(converter, site)
      } yield doTest(converter, input)

      println(converterName + "," + results.map(_._2).mkString(","))
    }
  }

  private def doTest(c: SiteConverter, input: Input): (Long, Long) = {
    Runtime.getRuntime.gc()
    Runtime.getRuntime.runFinalization()
    Runtime.getRuntime.gc()
    Runtime.getRuntime.gc()

    val start = System.nanoTime()
    runXTimes(c, input, N)
    val duration = System.nanoTime() - start
    val avg = duration / N
    duration -> avg
  }

  private def runXTimes(c: SiteConverter, input: Input, x: Int): Unit = {
    for (_ <- 0 until x) {
      convert(c, input)
    }
  }

  private def doWarmUp() = {
    val x = N / 10

    for ((converterName, c) <- Converters.list) {
      print(s"$converterName... ")
      for (data <- TestData.all) {
        val input = createInput(c, data._2)
        runXTimes(c, input, x)
      }
      println("done")
    }
  }

}

/*
Converter,1k,2k,4k,8k,64k
JSON,4483,9478,19129,40738,308020
ScalaPB,2635,4632,9747,18381,152629
Java PB,3562,6530,11926,22986,234368
Pickles,61370,95536,248517,674345,4797155
 */
object SerializationPerfTestApp extends App with BasePerfTest[Site, Array[Byte]] {
  override def createInput(converter: SiteConverter, site: Site): Site = site

  override def convert(converter: SiteConverter, input: Site): Array[Byte] = converter.toByteArray(input)

  doTest()
}

/*
Converter,1k,2k,4k,8k,64k
JSON,7885,12686,24804,54639,390988
ScalaPB,2399,4325,8297,15774,134520
Java PB,3367,6182,10893,20676,173379
Pickles,41108,67136,176470,467082,3362515
 */
object DeserializationPerfTestApp extends App with BasePerfTest[Array[Byte], Site] {
  override def createInput(converter: SiteConverter, site: Site): Array[Byte] = converter.toByteArray(site)

  override def convert(converter: SiteConverter, input: Array[Byte]): Site = converter.fromByteArray(input)

  doTest()
}
