package com.komanov.serialization.jmh

import java.util.concurrent.TimeUnit

import com.komanov.serialization.converters._
import com.komanov.serialization.domain.{EventProcessor, Site, SiteEvent}
import org.openjdk.jmh.annotations._

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime, Mode.SampleTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Measurement(iterations = 10, batchSize = 100)
@Warmup(iterations = 1, batchSize = 100)
abstract class BenchmarkBase {

  import TestData._

  def converter: MyConverter

  private val site1kBytes = converter.toByteArray(site1k)
  private val site2kBytes = converter.toByteArray(site2k)
  private val site4kBytes = converter.toByteArray(site4k)
  private val site8kBytes = converter.toByteArray(site8k)
  private val site64kBytes = converter.toByteArray(site64k)

  private val events1kInput = createEventsInput(site1k)
  private val events2kInput = createEventsInput(site2k)
  private val events4kInput = createEventsInput(site4k)
  private val events8kInput = createEventsInput(site8k)
  private val events64kInput = createEventsInput(site64k)

  private val events1kOutput = createEventsOutput(site1k)
  private val events2kOutput = createEventsOutput(site2k)
  private val events4kOutput = createEventsOutput(site4k)
  private val events8kOutput = createEventsOutput(site8k)
  private val events64kOutput = createEventsOutput(site64k)

  @Benchmark
  def serialization_site_1k(): Array[Byte] = {
    converter.toByteArray(TestData.site1k)
  }

  @Benchmark
  def serialization_site_2k(): Array[Byte] = {
    converter.toByteArray(TestData.site2k)
  }

  @Benchmark
  def serialization_site_4k(): Array[Byte] = {
    converter.toByteArray(TestData.site4k)
  }

  @Benchmark
  def serialization_site_8k(): Array[Byte] = {
    converter.toByteArray(TestData.site8k)
  }

  @Benchmark
  def serialization_site_64k(): Array[Byte] = {
    converter.toByteArray(TestData.site64k)
  }

  @Benchmark
  def deserialization_site_1k(): Site = {
    converter.fromByteArray(site1kBytes)
  }

  @Benchmark
  def deserialization_site_2k(): Site = {
    converter.fromByteArray(site2kBytes)
  }

  @Benchmark
  def deserialization_site_4k(): Site = {
    converter.fromByteArray(site4kBytes)
  }

  @Benchmark
  def deserialization_site_8k(): Site = {
    converter.fromByteArray(site8kBytes)
  }

  @Benchmark
  def deserialization_site_64k(): Site = {
    converter.fromByteArray(site64kBytes)
  }

  @Benchmark
  def serialization_events_1k(): Any = {
    for (e <- events1kInput) {
      converter.toByteArray(e)
    }
  }

  @Benchmark
  def serialization_events_2k(): Any = {
    for (e <- events2kInput) {
      converter.toByteArray(e)
    }
  }

  @Benchmark
  def serialization_events_4k(): Any = {
    for (e <- events4kInput) {
      converter.toByteArray(e)
    }
  }

  @Benchmark
  def serialization_events_8k(): Any = {
    for (e <- events8kInput) {
      converter.toByteArray(e)
    }
  }

  @Benchmark
  def serialization_events_64k(): Any = {
    for (e <- events64kInput) {
      converter.toByteArray(e)
    }
  }

  @Benchmark
  def deserialization_events_1k(): Any = {
    for ((t, bytes) <- events1kOutput) {
      converter.siteEventFromByteArray(t, bytes)
    }
  }

  @Benchmark
  def deserialization_events_2k(): Any = {
    for ((t, bytes) <- events2kOutput) {
      converter.siteEventFromByteArray(t, bytes)
    }
  }

  @Benchmark
  def deserialization_events_4k(): Any = {
    for ((t, bytes) <- events4kOutput) {
      converter.siteEventFromByteArray(t, bytes)
    }
  }

  @Benchmark
  def deserialization_events_8k(): Any = {
    for ((t, bytes) <- events8kOutput) {
      converter.siteEventFromByteArray(t, bytes)
    }
  }

  @Benchmark
  def deserialization_events_64k(): Any = {
    for ((t, bytes) <- events64kOutput) {
      converter.siteEventFromByteArray(t, bytes)
    }
  }

  private def createEventsInput(site: Site): Seq[SiteEvent] = {
    EventProcessor.unapply(site).map(_.event)
  }

  private def createEventsOutput(site: Site): Seq[(Class[_], Array[Byte])] = {
    EventProcessor.unapply(site).map(_.event).map(e => e.getClass -> converter.toByteArray(e))
  }

}

class JsonBenchmark extends BenchmarkBase {
  override def converter = JsonConverter
}

class ScalaPbBenchmark extends BenchmarkBase {
  override def converter = ScalaPbConverter
}

class JavaPbBenchmark extends BenchmarkBase {
  override def converter = JavaPbConverter
}

class JavaThriftBenchmark extends BenchmarkBase {
  override def converter = JavaThriftConverter
}

class ScroogeBenchmark extends BenchmarkBase {
  override def converter = ScroogeConverter
}

class JavaSerializationBenchmark extends BenchmarkBase {
  override def converter = JavaSerializationConverter
}

class PicklingBenchmark extends BenchmarkBase {
  override def converter = PicklingConverter
}

class BooPickleBenchmark extends BenchmarkBase {
  override def converter = BoopickleConverter
}

class ChillBenchmark extends BenchmarkBase {
  override def converter = ChillConverter
}
