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

object SerializationPerfTestApp extends App with BasePerfTest[Site, Array[Byte]] {
  override def createInput(converter: SiteConverter, site: Site): Site = site

  override def convert(converter: SiteConverter, input: Site): Array[Byte] = converter.toByteArray(input)

  doTest()
}

object DeserializationPerfTestApp extends App with BasePerfTest[Array[Byte], Site] {
  override def createInput(converter: SiteConverter, site: Site): Array[Byte] = converter.toByteArray(site)

  override def convert(converter: SiteConverter, input: Array[Byte]): Site = converter.fromByteArray(input)

  doTest()
}
