package com.komanov.serialization.converters

import java.nio.ByteBuffer
import java.time.Instant
import java.util

import boopickle.Default._
import boopickle.{BufferPool, DecoderSize, EncoderSize}
import com.komanov.serialization.domain._

/** https://github.com/ochrons/boopickle */
object BoopickleConverter extends MyConverter {

  implicit def pickleState = new PickleState(new EncoderSize, false, false)
  implicit val unpickleState = (bb: ByteBuffer) => new UnpickleState(new DecoderSize(bb), false, false)

  override def toByteArray(site: Site): Array[Byte] = {
    val bb = Pickle.intoBytes(site)
    val a = bbToArray(bb)
    BufferPool.release(bb)
    a
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    Unpickle[Site].fromBytes(ByteBuffer.wrap(bytes))
  }

  override def toByteArray(event: SiteEvent): Array[Byte] = {
    val bb = Pickle.intoBytes(event)
    val a = bbToArray(bb)
    BufferPool.release(bb)
    a
  }

  override def siteEventFromByteArray(clazz: Class[_], bytes: Array[Byte]): SiteEvent = {
    Unpickle[SiteEvent].fromBytes(ByteBuffer.wrap(bytes))
  }

  override def toByteArray(events: Seq[SiteEvent]): Array[Byte] = {
    val bb = Pickle.intoBytes(events)
    val a = bbToArray(bb)
    BufferPool.release(bb)
    a
  }

  override def siteEventSeqFromByteArray(bytes: Array[Byte]): Seq[SiteEvent] = {
    Unpickle[Seq[SiteEvent]].fromBytes(ByteBuffer.wrap(bytes))
  }

  private def bbToArray(bb: ByteBuffer) = {
    util.Arrays.copyOfRange(bb.array(), 0, bb.limit())
  }

  implicit val instantPickler = transformPickler[Instant, Long](t => Instant.ofEpochMilli(t))(_.toEpochMilli)

  val pageComponentTypeValues = PageComponentType.values()
  val siteFlagValues = SiteFlag.values()
  val siteTypeValues = SiteType.values()

  implicit val pageComponentTypePickler = transformPickler((i: Int) => pageComponentTypeValues(i))(_.ordinal())
  implicit val siteFlagPickler = transformPickler((i: Int) => siteFlagValues(i))(_.ordinal())
  implicit val siteTypePickler = transformPickler((i: Int) => siteTypeValues(i))(_.ordinal())

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
