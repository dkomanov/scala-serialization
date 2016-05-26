package com.komanov.serialization.converters

import java.nio.ByteBuffer
import java.time.Instant

import boopickle.Default._
import com.komanov.serialization.domain._

object BoopickleConverter extends SiteConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    Pickle.intoBytes(site).array()
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    Unpickle[Site].fromBytes(ByteBuffer.wrap(bytes))
  }

  implicit val instantPickler = transformPickler[Instant, Long](_.toEpochMilli, t => Instant.ofEpochMilli(t))

  implicit val pageComponentTypePickler = transformPickler[PageComponentType, String](_.name(), t => PageComponentType.valueOf(t))
  implicit val siteFlagPickler = transformPickler[SiteFlag, String](_.name(), t => SiteFlag.valueOf(t))
  implicit val siteTypePickler = transformPickler[SiteType, String](_.name(), t => SiteType.valueOf(t))

  implicit val entryPointPickler = compositePickler[EntryPoint]
    .addConcreteType[DomainEntryPoint]
    .addConcreteType[FreeEntryPoint]

  implicit val pageComponentDataPickler = compositePickler[PageComponentData]
    .addConcreteType[TextComponentData]
    .addConcreteType[ButtonComponentData]
    .addConcreteType[BlogComponentData]

}
