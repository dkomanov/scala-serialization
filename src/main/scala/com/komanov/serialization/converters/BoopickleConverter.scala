package com.komanov.serialization.converters

import java.nio.ByteBuffer
import java.time.Instant
import java.util

import boopickle.Default._
import com.komanov.serialization.domain._

/** https://github.com/ochrons/boopickle */
object BoopickleConverter extends MyConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    bbToArray(Pickle.intoBytes(site))
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    Unpickle[Site].fromBytes(ByteBuffer.wrap(bytes))
  }

  override def toByteArray(event: SiteEvent): Array[Byte] = {
    bbToArray(Pickle.intoBytes(event))
  }

  override def toByteArray(event: SiteEventData): Array[Byte] = {
    bbToArray(Pickle.intoBytes(event))
  }

  override def eventFromByteArray(bytes: Array[Byte]): SiteEventData = {
    Unpickle[SiteEventData].fromBytes(ByteBuffer.wrap(bytes))
  }

  private def bbToArray(bb: ByteBuffer) = {
    util.Arrays.copyOfRange(bb.array(), 0, bb.limit())
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

  implicit val siteEventPickler = compositePickler[SiteEvent]
    .addConcreteType[SiteCreated]
    .addConcreteType[SiteNameSet]
    .addConcreteType[SiteDescriptionSet]
    .addConcreteType[SiteRevisionSet]
    .addConcreteType[SitePublished]
    .addConcreteType[SiteUnpublished]
    .addConcreteType[SiteFlagAdded]
    .addConcreteType[SiteFlagRemoved]
    .addConcreteType[DomainAdded]
    .addConcreteType[DomainRemoved]
    .addConcreteType[PrimaryDomainSet]
    .addConcreteType[DefaultMetaTagAdded]
    .addConcreteType[DefaultMetaTagRemoved]
    .addConcreteType[PageAdded]
    .addConcreteType[PageRemoved]
    .addConcreteType[PageNameSet]
    .addConcreteType[PageMetaTagAdded]
    .addConcreteType[PageMetaTagRemoved]
    .addConcreteType[PageComponentAdded]
    .addConcreteType[PageComponentRemoved]
    .addConcreteType[PageComponentPositionSet]
    .addConcreteType[PageComponentPositionReset]
    .addConcreteType[TextComponentDataSet]
    .addConcreteType[ButtonComponentDataSet]
    .addConcreteType[BlogComponentDataSet]
    .addConcreteType[DomainEntryPointAdded]
    .addConcreteType[FreeEntryPointAdded]
    .addConcreteType[EntryPointRemoved]
    .addConcreteType[PrimaryEntryPointSet]
}
