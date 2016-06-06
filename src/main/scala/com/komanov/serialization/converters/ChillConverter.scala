package com.komanov.serialization.converters

import com.komanov.serialization.domain.{Site, SiteEvent, SiteEventData}
import com.twitter.chill.ScalaKryoInstantiator

/** https://github.com/twitter/chill */
object ChillConverter extends MyConverter {

  private val pool = ScalaKryoInstantiator.defaultPool

  override def toByteArray(site: Site): Array[Byte] = {
    pool.toBytesWithoutClass(site)
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    pool.fromBytes(bytes, classOf[Site])
  }

  override def toByteArray(event: SiteEvent): Array[Byte] = {
    pool.toBytesWithoutClass(event)
  }

  override def toByteArray(event: SiteEventData): Array[Byte] = {
    pool.toBytesWithoutClass(event)
  }

  override def eventDataFromByteArray(bytes: Array[Byte]): SiteEventData = {
    pool.fromBytes(bytes, classOf[SiteEventData])
  }
}
