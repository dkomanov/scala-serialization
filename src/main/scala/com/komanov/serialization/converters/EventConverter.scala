package com.komanov.serialization.converters

import com.komanov.serialization.domain.SiteEventData

trait EventConverter {

  def toByteArray(event: SiteEventData): Array[Byte]

  def eventFromByteArray(bytes: Array[Byte]): SiteEventData

}
