package com.komanov.serialization.converters

import com.komanov.serialization.domain.{SiteEvent, SiteEventData}

trait EventConverter {

  def toByteArray(event: SiteEvent): Array[Byte]

  def toByteArray(event: SiteEventData): Array[Byte]

  def eventDataFromByteArray(bytes: Array[Byte]): SiteEventData

}
