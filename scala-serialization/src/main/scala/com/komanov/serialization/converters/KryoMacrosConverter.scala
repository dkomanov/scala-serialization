package com.komanov.serialization.converters

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{FastInput, FastOutput}
import com.evolutiongaming.kryo.Serializer
import com.komanov.serialization.domain._

/** https://github.com/evolution-gaming/kryo-macros */
case class KryoMacrosConverter(kryo: Kryo, in: FastInput, out: FastOutput) {
  def read[A <: AnyRef](bs: Array[Byte])(implicit s: com.esotericsoftware.kryo.Serializer[A], m: Manifest[A]): A = {
    in.setBuffer(bs)
    kryo.readObject(in, m.runtimeClass.asInstanceOf[Class[A]], s)
  }

  def write[A <: AnyRef](a: A)(implicit s: com.esotericsoftware.kryo.Serializer[A]): Array[Byte] = {
    out.clear()
    kryo.writeObject(out, a, s)
    out.toBytes
  }
}

object KryoMacrosConverter extends MyConverter {
  private[this] val pool = new ThreadLocal[KryoMacrosConverter] {
    override def initialValue(): KryoMacrosConverter =
      KryoMacrosConverter(new Kryo, new FastInput(), new FastOutput(131072, 131072))
  }
  private[this] implicit val domainSerializer = Serializer.make[Domain]
  private[this] implicit val metaTagSerializer = Serializer.make[MetaTag]
  private[this] implicit val entryPointSerializer = Serializer.makeCommon[EntryPoint] {
    case 0 => Serializer.inner[DomainEntryPoint]
    case 1 => Serializer.inner[FreeEntryPoint]
  }
  private[this] implicit val pageComponentDataSerializer = Serializer.makeCommon[PageComponentData] {
    case 0 => Serializer.inner[TextComponentData]
    case 1 => Serializer.inner[ButtonComponentData]
    case 2 => Serializer.inner[BlogComponentData]
  }
  private[this] implicit val pageComponentPositionSerializer = Serializer.make[PageComponentPosition]
  private[this] implicit val pageComponentSerializer = Serializer.make[PageComponent]
  private[this] implicit val pageSerializer = Serializer.make[Page]
  private[this] implicit val siteSerializer = Serializer.make[Site]
  private[this] implicit val siteEventSerializer = Serializer.makeCommon[SiteEvent] {
    case 0 => Serializer.inner[SiteCreated]
    case 1 => Serializer.inner[SiteNameSet]
    case 2 => Serializer.inner[SiteDescriptionSet]
    case 3 => Serializer.inner[SiteRevisionSet]
    case 4 => Serializer.inner[SitePublished]
    case 5 => Serializer.inner[SiteUnpublished]
    case 6 => Serializer.inner[SiteFlagAdded]
    case 7 => Serializer.inner[SiteFlagRemoved]
    case 8 => Serializer.inner[DomainAdded]
    case 9 => Serializer.inner[DomainRemoved]
    case 10 => Serializer.inner[PrimaryDomainSet]
    case 11 => Serializer.inner[DefaultMetaTagAdded]
    case 12 => Serializer.inner[DefaultMetaTagRemoved]
    case 13 => Serializer.inner[PageAdded]
    case 14 => Serializer.inner[PageRemoved]
    case 15 => Serializer.inner[PageNameSet]
    case 16 => Serializer.inner[PageMetaTagAdded]
    case 17 => Serializer.inner[PageMetaTagRemoved]
    case 18 => Serializer.inner[PageComponentAdded]
    case 19 => Serializer.inner[PageComponentRemoved]
    case 20 => Serializer.inner[PageComponentPositionSet]
    case 21 => Serializer.inner[PageComponentPositionReset]
    case 22 => Serializer.inner[TextComponentDataSet]
    case 23 => Serializer.inner[ButtonComponentDataSet]
    case 24 => Serializer.inner[BlogComponentDataSet]
    case 25 => Serializer.inner[DomainEntryPointAdded]
    case 26 => Serializer.inner[FreeEntryPointAdded]
    case 27 => Serializer.inner[EntryPointRemoved]
    case 28 => Serializer.inner[PrimaryEntryPointSet]
  }

  def toByteArray(site: Site): Array[Byte] = pool.get().write(site)

  def fromByteArray(bytes: Array[Byte]): Site = pool.get().read[Site](bytes)

  def toByteArray(event: SiteEvent): Array[Byte] = pool.get().write(event)

  def siteEventFromByteArray(clazz: Class[_], bytes: Array[Byte]): SiteEvent = pool.get().read[SiteEvent](bytes)
}
