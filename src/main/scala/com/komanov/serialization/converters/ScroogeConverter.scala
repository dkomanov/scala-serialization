package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.thriftscala._
import com.twitter.scrooge._
import org.apache.thrift.protocol.TBinaryProtocol

import scala.language.existentials
import scala.reflect.ClassTag

/** https://twitter.github.io/scrooge/ */
object ScroogeConverter extends MyConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = new SitePb.Immutable(
      Option(ConversionUtils.uuidToByteBuffer(site.id)),
      Option(ConversionUtils.uuidToByteBuffer(site.ownerId)),
      Some(site.revision),
      Some(toSiteTypePb(site.siteType)),
      Some(site.flags.map(toSiteFlagPb)),
      Option(site.name),
      Option(site.description),
      Some(site.domains.map(d => new DomainPb.Immutable(Option(d.name), Some(d.primary)))),
      Some(site.defaultMetaTags.map(toMetaTagPb)),
      Some(site.pages.map { p =>
        new PagePb.Immutable(Option(p.name), Option(p.path), Some(p.metaTags.map(toMetaTagPb)), Some(p.components.map(toComponentPb)))
      }),
      Some(site.entryPoints.map(toEntryPointPb)),
      Some(site.published),
      Some(ConversionUtils.instantToLong(site.dateCreated)),
      Some(ConversionUtils.instantToLong(site.dateUpdated))
    )

    val transport = new TArrayByteTransport
    SitePb.encode(proto, new TBinaryProtocol(transport))
    transport.toByteArray
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    val transport = new TArrayByteTransport
    transport.setBytes(bytes)
    val site = SitePb.decode(new TBinaryProtocol(transport))

    Site(
      ConversionUtils.bytesToUuid(site.id.get),
      ConversionUtils.bytesToUuid(site.ownerId.get),
      site.revision.get,
      fromSiteTypePb(site.siteType.get),
      site.flags.get.map(fromSiteFlagPb),
      site.name.get,
      site.description.get,
      site.domains.get.map(d => Domain(d.name.get, d.primary.get)),
      site.defaultMetaTags.get.map(fromMetaTagPb),
      site.pages.get.map { p =>
        Page(p.name.get, p.path.get, p.metaTags.get.map(fromMetaTagPb), p.components.get.map(fromComponentPb))
      },
      site.entryPoints.get.map(fromEntryPointPb),
      site.published.get,
      ConversionUtils.longToInstance(site.dateCreated.get),
      ConversionUtils.longToInstance(site.dateUpdated.get)
    )
  }

  override def toByteArray(event: SiteEvent): Array[Byte] = {
    val BigTuple(_, codec, toMessage, _) = eventMap(event.getClass)
    val proto = toMessage(event)

    val transport = new TArrayByteTransport
    codec.asInstanceOf[ThriftStructCodec[ThriftStruct]].encode(proto, new TBinaryProtocol(transport))
    transport.toByteArray
  }

  override def siteEventFromByteArray(clazz: Class[_], bytes: Array[Byte]): SiteEvent = {
    val BigTuple(_, codec, _, fromMessage) = eventMap(clazz)

    val transport = new TArrayByteTransport
    transport.setBytes(bytes)
    fromMessage(codec.decode(new TBinaryProtocol(transport)))
  }

  private def toMetaTagPb(mt: MetaTag) = {
    new MetaTagPb.Immutable(Option(mt.name), Option(mt.value))
  }

  private def fromMetaTagPb(mt: MetaTagPb) = MetaTag(mt.name.get, mt.value.get)

  private def toComponentPb(pc: PageComponent): PageComponentPb = {
    new PageComponentPb.Immutable(
      Option(ConversionUtils.uuidToByteBuffer(pc.id)),
      Some(toPageComponentTypePb(pc.componentType)),
      Some(pc.data match {
        case text: TextComponentData =>
          new PageComponentDataPb.Immutable(Some(TextComponentDataPb(Option(text.text))), None, None)

        case button: ButtonComponentData =>
          new PageComponentDataPb.Immutable(None, Some(ButtonComponentDataPb(Option(button.name), Option(button.text), Option(ConversionUtils.uuidToByteBuffer(button.action)))), None)

        case blog: BlogComponentData =>
          new PageComponentDataPb.Immutable(None, None, Some(BlogComponentDataPb(Option(blog.name), Some(blog.rss), Some(blog.tags))))
      }),
      pc.position.map(p => new PageComponentPositionPb.Immutable(Some(p.x), Some(p.y))),
      Some(ConversionUtils.instantToLong(pc.dateCreated)),
      Some(ConversionUtils.instantToLong(pc.dateUpdated))
    )
  }

  private def fromComponentPb(pc: PageComponentPb) = PageComponent(
    ConversionUtils.bytesToUuid(pc.id.get),
    fromPageComponentTypePb(pc.componentType.get),
    pc.data.get match {
      case PageComponentDataPb(Some(text), None, None) =>
        TextComponentData(text.text.get)
      case PageComponentDataPb(None, Some(button), None) =>
        ButtonComponentData(button.name.get, button.text.get, ConversionUtils.bytesToUuid(button.action.get))
      case PageComponentDataPb(None, None, Some(blog)) =>
        BlogComponentData(blog.name.get, blog.rss.get, blog.tags.get)
    },
    pc.position.map(p => PageComponentPosition(x = p.x.get, y = p.y.get)),
    ConversionUtils.longToInstance(pc.dateCreated.get),
    ConversionUtils.longToInstance(pc.dateUpdated.get)
  )

  private def toEntryPointPb(entryPoint: EntryPoint): EntryPointPb = entryPoint match {
    case ep: DomainEntryPoint =>
      new EntryPointPb.Immutable(domain = Some(new DomainEntryPointPb.Immutable(Option(ep.domain), Some(ep.primary))))

    case ep: FreeEntryPoint =>
      new EntryPointPb.Immutable(free = Some(new FreeEntryPointPb.Immutable(Option(ep.userName), Option(ep.siteName), Some(ep.primary))))
  }

  private def fromEntryPointPb(entryPoint: EntryPointPb): EntryPoint = {
    entryPoint match {
      case EntryPointPb(Some(ep), None) => DomainEntryPoint(ep.domain.get, ep.primary.get)
      case EntryPointPb(None, Some(ep)) => FreeEntryPoint(ep.userName.get, ep.siteName.get, ep.primary.get)
      case _ => throw new RuntimeException("Expected entry point")
    }
  }

  private def toSiteTypePb(t: SiteType): SiteTypePb = t match {
    case SiteType.Flash => SiteTypePb.Flash
    case SiteType.Silverlight => SiteTypePb.Silverlight
    case SiteType.Html5 => SiteTypePb.Html5
    case SiteType.Unknown => SiteTypePb.UnknownSiteType
  }

  private def fromSiteTypePb(t: SiteTypePb): SiteType = t match {
    case SiteTypePb.Flash => SiteType.Flash
    case SiteTypePb.Silverlight => SiteType.Silverlight
    case SiteTypePb.Html5 => SiteType.Html5
    case SiteTypePb.UnknownSiteType | SiteTypePb.EnumUnknownSiteTypePb(_) => SiteType.Unknown
  }

  private def toSiteFlagPb(f: SiteFlag): SiteFlagPb = f match {
    case SiteFlag.Free => SiteFlagPb.Free
    case SiteFlag.Premium => SiteFlagPb.Premium
    case SiteFlag.Unknown => SiteFlagPb.UnknownSiteFlag
  }

  private def fromSiteFlagPb(f: SiteFlagPb): SiteFlag = f match {
    case SiteFlagPb.Free => SiteFlag.Free
    case SiteFlagPb.Premium => SiteFlag.Premium
    case SiteFlagPb.UnknownSiteFlag | SiteFlagPb.EnumUnknownSiteFlagPb(_) => SiteFlag.Unknown
  }

  private def toPageComponentTypePb(t: PageComponentType): PageComponentTypePb = t match {
    case PageComponentType.Text => PageComponentTypePb.Text
    case PageComponentType.Button => PageComponentTypePb.Button
    case PageComponentType.Blog => PageComponentTypePb.Blog
    case PageComponentType.Unknown => PageComponentTypePb.UnknownPageComponentType
  }

  private def fromPageComponentTypePb(t: PageComponentTypePb): PageComponentType = t match {
    case PageComponentTypePb.Text => PageComponentType.Text
    case PageComponentTypePb.Button => PageComponentType.Button
    case PageComponentTypePb.Blog => PageComponentType.Blog
    case PageComponentTypePb.UnknownPageComponentType | PageComponentTypePb.EnumUnknownPageComponentTypePb(_) => PageComponentType.Unknown
  }

  type Codec = ThriftStructCodec[ThriftStruct]
  type ToMessageF = SiteEvent => ThriftStruct
  type FromMessageF = ThriftStruct => SiteEvent

  case class BigTuple(siteEventClass: Class[_], codec: Codec, toMessage: ToMessageF, fromMessage: FromMessageF)

  private def createEventMapTuple[T: ClassTag, M](codec: ThriftStructCodec[_],
                                                  toMessage: T => M,
                                                  fromMessage: M => T): BigTuple = {
    BigTuple(
      implicitly[ClassTag[T]].runtimeClass,
      codec.asInstanceOf[Codec],
      toMessage.asInstanceOf[ToMessageF],
      fromMessage.asInstanceOf[FromMessageF]
    )
  }

  private val eventMap: Map[Class[_], BigTuple] = Seq(
    createEventMapTuple[SiteCreated, SiteCreatedPb](
      SiteCreatedPb,
      e => SiteCreatedPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(ConversionUtils.uuidToByteBuffer(e.ownerId)), Some(toSiteTypePb(e.siteType))),
      e => SiteCreated(ConversionUtils.bytesToUuid(e.id.get), ConversionUtils.bytesToUuid(e.ownerId.get), fromSiteTypePb(e.siteType.get))
    ),
    createEventMapTuple[SiteNameSet, SiteNameSetPb](
      SiteNameSetPb,
      e => SiteNameSetPb(Some(e.name)),
      e => SiteNameSet(e.name.get)
    ),
    createEventMapTuple[SiteDescriptionSet, SiteDescriptionSetPb](
      SiteDescriptionSetPb,
      e => SiteDescriptionSetPb(Some(e.description)),
      e => SiteDescriptionSet(e.description.get)
    ),
    createEventMapTuple[SiteRevisionSet, SiteRevisionSetPb](
      SiteRevisionSetPb,
      e => SiteRevisionSetPb(Some(e.revision)),
      e => SiteRevisionSet(e.revision.get)
    ),
    createEventMapTuple[SitePublished, SitePublishedPb](
      SitePublishedPb,
      e => SitePublishedPb(),
      e => SitePublished()
    ),
    createEventMapTuple[SiteUnpublished, SiteUnpublishedPb](
      SiteUnpublishedPb,
      e => SiteUnpublishedPb(),
      e => SiteUnpublished()
    ),
    createEventMapTuple[SiteFlagAdded, SiteFlagAddedPb](
      SiteFlagAddedPb,
      e => SiteFlagAddedPb(Some(toSiteFlagPb(e.siteFlag))),
      e => SiteFlagAdded(fromSiteFlagPb(e.siteFlag.get))
    ),
    createEventMapTuple[SiteFlagRemoved, SiteFlagRemovedPb](
      SiteFlagRemovedPb,
      e => SiteFlagRemovedPb(Some(toSiteFlagPb(e.siteFlag))),
      e => SiteFlagRemoved(fromSiteFlagPb(e.siteFlag.get))
    ),
    createEventMapTuple[DomainAdded, DomainAddedPb](
      DomainAddedPb,
      e => DomainAddedPb(Some(e.name)),
      e => DomainAdded(e.name.get)
    ),
    createEventMapTuple[DomainRemoved, DomainRemovedPb](
      DomainRemovedPb,
      e => DomainRemovedPb(Some(e.name)),
      e => DomainRemoved(e.name.get)
    ),
    createEventMapTuple[PrimaryDomainSet, PrimaryDomainSetPb](
      PrimaryDomainSetPb,
      e => PrimaryDomainSetPb(Some(e.name)),
      e => PrimaryDomainSet(e.name.get)
    ),
    createEventMapTuple[DefaultMetaTagAdded, DefaultMetaTagAddedPb](
      DefaultMetaTagAddedPb,
      e => DefaultMetaTagAddedPb(Some(e.name), Some(e.value)),
      e => DefaultMetaTagAdded(e.name.get, e.value.get)
    ),
    createEventMapTuple[DefaultMetaTagRemoved, DefaultMetaTagRemovedPb](
      DefaultMetaTagRemovedPb,
      e => DefaultMetaTagRemovedPb(Some(e.name)),
      e => DefaultMetaTagRemoved(e.name.get)
    ),
    createEventMapTuple[PageAdded, PageAddedPb](
      PageAddedPb,
      e => PageAddedPb(Some(e.path)),
      e => PageAdded(e.path.get)
    ),
    createEventMapTuple[PageRemoved, PageRemovedPb](
      PageRemovedPb,
      e => PageRemovedPb(Some(e.path)),
      e => PageRemoved(e.path.get)
    ),
    createEventMapTuple[PageNameSet, PageNameSetPb](
      PageNameSetPb,
      e => PageNameSetPb(Some(e.path), Some(e.name)),
      e => PageNameSet(e.path.get, e.name.get)
    ),
    createEventMapTuple[PageMetaTagAdded, PageMetaTagAddedPb](
      PageMetaTagAddedPb,
      e => PageMetaTagAddedPb(Some(e.path), Some(e.name), Some(e.value)),
      e => PageMetaTagAdded(e.path.get, e.name.get, e.value.get)
    ),
    createEventMapTuple[PageMetaTagRemoved, PageMetaTagRemovedPb](
      PageMetaTagRemovedPb,
      e => PageMetaTagRemovedPb(Some(e.path), Some(e.name)),
      e => PageMetaTagRemoved(e.path.get, e.name.get)
    ),
    createEventMapTuple[PageComponentAdded, PageComponentAddedPb](
      PageComponentAddedPb,
      e => PageComponentAddedPb(Some(e.pagePath), Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(toPageComponentTypePb(e.componentType))),
      e => PageComponentAdded(e.pagePath.get, ConversionUtils.bytesToUuid(e.id.get), fromPageComponentTypePb(e.componentType.get))
    ),
    createEventMapTuple[PageComponentRemoved, PageComponentRemovedPb](
      PageComponentRemovedPb,
      e => PageComponentRemovedPb(Some(e.pagePath), Some(ConversionUtils.uuidToByteBuffer(e.id))),
      e => PageComponentRemoved(e.pagePath.get, ConversionUtils.bytesToUuid(e.id.get))
    ),
    createEventMapTuple[PageComponentPositionSet, PageComponentPositionSetPb](
      PageComponentPositionSetPb,
      e => PageComponentPositionSetPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(e.position.x), Some(e.position.y)),
      e => PageComponentPositionSet(ConversionUtils.bytesToUuid(e.id.get), PageComponentPosition(e.x.get, e.y.get))
    ),
    createEventMapTuple[PageComponentPositionReset, PageComponentPositionResetPb](
      PageComponentPositionResetPb,
      e => PageComponentPositionResetPb(Some(ConversionUtils.uuidToByteBuffer(e.id))),
      e => PageComponentPositionReset(ConversionUtils.bytesToUuid(e.id.get))
    ),
    createEventMapTuple[TextComponentDataSet, TextComponentDataSetPb](
      TextComponentDataSetPb,
      e => TextComponentDataSetPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(e.text)),
      e => TextComponentDataSet(ConversionUtils.bytesToUuid(e.id.get), e.text.get)
    ),
    createEventMapTuple[ButtonComponentDataSet, ButtonComponentDataSetPb](
      ButtonComponentDataSetPb,
      e => ButtonComponentDataSetPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(e.name), Some(e.text), Some(ConversionUtils.uuidToByteBuffer(e.action))),
      e => ButtonComponentDataSet(ConversionUtils.bytesToUuid(e.id.get), e.name.get, e.text.get, ConversionUtils.bytesToUuid(e.action.get))
    ),
    createEventMapTuple[BlogComponentDataSet, BlogComponentDataSetPb](
      BlogComponentDataSetPb,
      e => BlogComponentDataSetPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(e.name), Some(e.rss), Some(e.tags)),
      e => BlogComponentDataSet(ConversionUtils.bytesToUuid(e.id.get), e.name.get, e.rss.get, e.tags.get)
    ),
    createEventMapTuple[DomainEntryPointAdded, DomainEntryPointAddedPb](
      DomainEntryPointAddedPb,
      e => DomainEntryPointAddedPb(Some(e.domain)),
      e => DomainEntryPointAdded(e.domain.get)
    ),
    createEventMapTuple[FreeEntryPointAdded, FreeEntryPointAddedPb](
      FreeEntryPointAddedPb,
      e => FreeEntryPointAddedPb(Some(e.userName), Some(e.siteName)),
      e => FreeEntryPointAdded(e.userName.get, e.siteName.get)
    ),
    createEventMapTuple[EntryPointRemoved, EntryPointRemovedPb](
      EntryPointRemovedPb,
      e => EntryPointRemovedPb(Some(e.lookupKey)),
      e => EntryPointRemoved(e.lookupKey.get)
    ),
    createEventMapTuple[PrimaryEntryPointSet, PrimaryEntryPointSetPb](
      PrimaryEntryPointSetPb,
      e => PrimaryEntryPointSetPb(Some(e.lookupKey)),
      e => PrimaryEntryPointSet(e.lookupKey.get)
    )
  ).map(t => t.siteEventClass -> t).toMap

}
