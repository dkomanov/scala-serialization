package com.komanov.serialization.converters

import com.komanov.serialization.domain.Site
import com.twitter.chill.ScalaKryoInstantiator

/** https://github.com/twitter/chill */
object ChillConverter extends SiteConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    ScalaKryoInstantiator.defaultPool.toBytesWithoutClass(site)
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    ScalaKryoInstantiator.defaultPool.fromBytes(bytes, classOf[Site])
  }

}
