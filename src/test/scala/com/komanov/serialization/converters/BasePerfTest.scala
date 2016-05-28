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
JSON,4365,8437,16771,35164,270175
ScalaPB,2176,3936,7475,14822,133119
Java PB,3173,6393,10123,21379,209716
Java Thrift,3657,6805,13074,27667,261673
Scrooge,3572,6506,12050,25036,233895
Serializable,13156,21203,36457,79045,652942
Pickles,53991,83601,220440,589888,4162785
Boopickle,5451,10628,17533,29765,225717
Chill,7202,9783,15130,27338,207871
 */
object SerializationPerfTestApp extends App with BasePerfTest[Site, Array[Byte]] {
  override def createInput(converter: SiteConverter, site: Site): Site = site

  override def convert(converter: SiteConverter, input: Site): Array[Byte] = converter.toByteArray(input)

  doTest()
}

/*
Converter,1k,2k,4k,8k,64k
JSON,7670,12964,24804,51578,384623
ScalaPB,2335,4576,7326,14754,128730
Java PB,3504,6076,10269,19792,168952
Java Thrift,3451,5812,10048,20693,176020
Scrooge,3640,6522,12740,25081,230556
Serializable,61455,84196,102870,126839,575232
Pickles,40337,63840,165109,446043,3201348
Boopickle,2848,5017,8454,15962,97270
Chill,6675,9654,14770,25261,193136
 */
object DeserializationPerfTestApp extends App with BasePerfTest[Array[Byte], Site] {
  override def createInput(converter: SiteConverter, site: Site): Array[Byte] = converter.toByteArray(site)

  override def convert(converter: SiteConverter, input: Array[Byte]): Site = converter.fromByteArray(input)

  doTest()
}
