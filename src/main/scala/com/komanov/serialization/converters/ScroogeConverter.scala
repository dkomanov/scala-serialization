package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.thriftscala._
import com.twitter.scrooge._
import org.apache.thrift.protocol.TBinaryProtocol

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
    val (codec, generator, _) = eventMap(event.getClass)
    val proto = generator(event)

    val transport = new TArrayByteTransport
    codec.asInstanceOf[ThriftStructCodec[ThriftStruct]].encode(proto, new TBinaryProtocol(transport))
    transport.toByteArray
  }

  override def toByteArray(event: SiteEventData): Array[Byte] = {
    val proto = SiteEventDataPb(
      Some(ConversionUtils.uuidToByteBuffer(event.id)),
      Some(toEventPb(event.event)),
      Some(ConversionUtils.instantToLong(event.timestamp))
    )

    val transport = new TArrayByteTransport
    SiteEventDataPb.encode(proto, new TBinaryProtocol(transport))
    transport.toByteArray
  }

  override def eventFromByteArray(bytes: Array[Byte]): SiteEventData = {
    val transport = new TArrayByteTransport
    transport.setBytes(bytes)
    val event = SiteEventDataPb.decode(new TBinaryProtocol(transport))

    SiteEventData(
      ConversionUtils.bytesToUuid(event.id.get),
      event.ev.get.productIterator.collectFirst {
        case Some(e: SiteCreatedPb) => SiteCreated(ConversionUtils.bytesToUuid(e.id.get), ConversionUtils.bytesToUuid(e.ownerId.get), fromSiteTypePb(e.siteType.get))
        case Some(e: SiteNameSetPb) => SiteNameSet(e.name.get)
        case Some(e: SiteDescriptionSetPb) => SiteDescriptionSet(e.description.get)
        case Some(e: SiteRevisionSetPb) => SiteRevisionSet(e.revision.get)
        case Some(e: SitePublishedPb) => SitePublished()
        case Some(e: SiteUnpublishedPb) => SiteUnpublished()
        case Some(e: SiteFlagAddedPb) => SiteFlagAdded(fromSiteFlagPb(e.siteFlag.get))
        case Some(e: SiteFlagRemovedPb) => SiteFlagRemoved(fromSiteFlagPb(e.siteFlag.get))
        case Some(e: DomainAddedPb) => DomainAdded(e.name.get)
        case Some(e: DomainRemovedPb) => DomainRemoved(e.name.get)
        case Some(e: PrimaryDomainSetPb) => PrimaryDomainSet(e.name.get)
        case Some(e: DefaultMetaTagAddedPb) => DefaultMetaTagAdded(e.name.get, e.value.get)
        case Some(e: DefaultMetaTagRemovedPb) => DefaultMetaTagRemoved(e.name.get)
        case Some(e: PageAddedPb) => PageAdded(e.path.get)
        case Some(e: PageRemovedPb) => PageRemoved(e.path.get)
        case Some(e: PageNameSetPb) => PageNameSet(e.path.get, e.name.get)
        case Some(e: PageMetaTagAddedPb) => PageMetaTagAdded(e.path.get, e.name.get, e.value.get)
        case Some(e: PageMetaTagRemovedPb) => PageMetaTagRemoved(e.path.get, e.name.get)
        case Some(e: PageComponentAddedPb) => PageComponentAdded(e.pagePath.get, ConversionUtils.bytesToUuid(e.id.get), fromPageComponentTypePb(e.componentType.get))
        case Some(e: PageComponentRemovedPb) => PageComponentRemoved(e.pagePath.get, ConversionUtils.bytesToUuid(e.id.get))
        case Some(e: PageComponentPositionSetPb) => PageComponentPositionSet(ConversionUtils.bytesToUuid(e.id.get), PageComponentPosition(e.x.get, e.y.get))
        case Some(e: PageComponentPositionResetPb) => PageComponentPositionReset(ConversionUtils.bytesToUuid(e.id.get))
        case Some(e: TextComponentDataSetPb) => TextComponentDataSet(ConversionUtils.bytesToUuid(e.id.get), e.text.get)
        case Some(e: ButtonComponentDataSetPb) => ButtonComponentDataSet(ConversionUtils.bytesToUuid(e.id.get), e.name.get, e.text.get, ConversionUtils.bytesToUuid(e.action.get))
        case Some(e: BlogComponentDataSetPb) => BlogComponentDataSet(ConversionUtils.bytesToUuid(e.id.get), e.name.get, e.rss.get, e.tags.get)
        case Some(e: DomainEntryPointAddedPb) => DomainEntryPointAdded(e.domain.get)
        case Some(e: FreeEntryPointAddedPb) => FreeEntryPointAdded(e.userName.get, e.siteName.get)
        case Some(e: EntryPointRemovedPb) => EntryPointRemoved(e.lookupKey.get)
        case Some(e: PrimaryEntryPointSetPb) => PrimaryEntryPointSet(e.lookupKey.get)
      }.get,
      ConversionUtils.longToInstance(event.timestamp.get)
    )
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

  type GeneratorType = SiteEvent => ThriftStruct
  type SetterType = Option[ThriftStruct] => SiteEventPb

  private def createEventMapTuple[T: ClassTag, M](codec: ThriftStructCodec[_],
                                                  generator: T => M,
                                                  setter: Option[M] => SiteEventPb): (Class[_], (ThriftStructCodec[ThriftStruct], GeneratorType, SetterType)) = {
    (implicitly[ClassTag[T]].runtimeClass, (codec.asInstanceOf[ThriftStructCodec[ThriftStruct]], generator.asInstanceOf[GeneratorType], setter.asInstanceOf[SetterType]))
  }

  private val eventMap = Map[Class[_], (ThriftStructCodec[ThriftStruct], GeneratorType, SetterType)](
    createEventMapTuple[SiteCreated, SiteCreatedPb](SiteCreatedPb, e => SiteCreatedPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(ConversionUtils.uuidToByteBuffer(e.ownerId)), Some(toSiteTypePb(e.siteType))), m => SiteEventPb(siteCreatedPb = m)),
    createEventMapTuple[SiteNameSet, SiteNameSetPb](SiteNameSetPb, e => SiteNameSetPb(Some(e.name)), m => SiteEventPb(siteNameSetPb = m)),
    createEventMapTuple[SiteDescriptionSet, SiteDescriptionSetPb](SiteDescriptionSetPb, e => SiteDescriptionSetPb(Some(e.description)), m => SiteEventPb(siteDescriptionSetPb = m)),
    createEventMapTuple[SiteRevisionSet, SiteRevisionSetPb](SiteRevisionSetPb, e => SiteRevisionSetPb(Some(e.revision)), m => SiteEventPb(siteRevisionSetPb = m)),
    createEventMapTuple[SitePublished, SitePublishedPb](SitePublishedPb, e => SitePublishedPb(), m => SiteEventPb(sitePublishedPb = m)),
    createEventMapTuple[SiteUnpublished, SiteUnpublishedPb](SiteUnpublishedPb, e => SiteUnpublishedPb(), m => SiteEventPb(siteUnpublishedPb = m)),
    createEventMapTuple[SiteFlagAdded, SiteFlagAddedPb](SiteFlagAddedPb, e => SiteFlagAddedPb(Some(toSiteFlagPb(e.siteFlag))), m => SiteEventPb(siteFlagAddedPb = m)),
    createEventMapTuple[SiteFlagRemoved, SiteFlagRemovedPb](SiteFlagRemovedPb, e => SiteFlagRemovedPb(Some(toSiteFlagPb(e.siteFlag))), m => SiteEventPb(siteFlagRemovedPb = m)),
    createEventMapTuple[DomainAdded, DomainAddedPb](DomainAddedPb, e => DomainAddedPb(Some(e.name)), m => SiteEventPb(domainAddedPb = m)),
    createEventMapTuple[DomainRemoved, DomainRemovedPb](DomainRemovedPb, e => DomainRemovedPb(Some(e.name)), m => SiteEventPb(domainRemovedPb = m)),
    createEventMapTuple[PrimaryDomainSet, PrimaryDomainSetPb](PrimaryDomainSetPb, e => PrimaryDomainSetPb(Some(e.name)), m => SiteEventPb(primaryDomainSetPb = m)),
    createEventMapTuple[DefaultMetaTagAdded, DefaultMetaTagAddedPb](DefaultMetaTagAddedPb, e => DefaultMetaTagAddedPb(Some(e.name), Some(e.value)), m => SiteEventPb(defaultMetaTagAddedPb = m)),
    createEventMapTuple[DefaultMetaTagRemoved, DefaultMetaTagRemovedPb](DefaultMetaTagRemovedPb, e => DefaultMetaTagRemovedPb(Some(e.name)), m => SiteEventPb(defaultMetaTagRemovedPb = m)),
    createEventMapTuple[PageAdded, PageAddedPb](PageAddedPb, e => PageAddedPb(Some(e.path)), m => SiteEventPb(pageAddedPb = m)),
    createEventMapTuple[PageRemoved, PageRemovedPb](PageRemovedPb, e => PageRemovedPb(Some(e.path)), m => SiteEventPb(pageRemovedPb = m)),
    createEventMapTuple[PageNameSet, PageNameSetPb](PageNameSetPb, e => PageNameSetPb(Some(e.path), Some(e.name)), m => SiteEventPb(pageNameSetPb = m)),
    createEventMapTuple[PageMetaTagAdded, PageMetaTagAddedPb](PageMetaTagAddedPb, e => PageMetaTagAddedPb(Some(e.path), Some(e.name), Some(e.value)), m => SiteEventPb(pageMetaTagAddedPb = m)),
    createEventMapTuple[PageMetaTagRemoved, PageMetaTagRemovedPb](PageMetaTagRemovedPb, e => PageMetaTagRemovedPb(Some(e.path), Some(e.name)), m => SiteEventPb(pageMetaTagRemovedPb = m)),
    createEventMapTuple[PageComponentAdded, PageComponentAddedPb](PageComponentAddedPb, e => PageComponentAddedPb(Some(e.pagePath), Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(toPageComponentTypePb(e.componentType))), m => SiteEventPb(pageComponentAddedPb = m)),
    createEventMapTuple[PageComponentRemoved, PageComponentRemovedPb](PageComponentRemovedPb, e => PageComponentRemovedPb(Some(e.pagePath), Some(ConversionUtils.uuidToByteBuffer(e.id))), m => SiteEventPb(pageComponentRemovedPb = m)),
    createEventMapTuple[PageComponentPositionSet, PageComponentPositionSetPb](PageComponentPositionSetPb, e => PageComponentPositionSetPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(e.position.x), Some(e.position.y)), m => SiteEventPb(pageComponentPositionSetPb = m)),
    createEventMapTuple[PageComponentPositionReset, PageComponentPositionResetPb](PageComponentPositionResetPb, e => PageComponentPositionResetPb(Some(ConversionUtils.uuidToByteBuffer(e.id))), m => SiteEventPb(pageComponentPositionResetPb = m)),
    createEventMapTuple[TextComponentDataSet, TextComponentDataSetPb](TextComponentDataSetPb, e => TextComponentDataSetPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(e.text)), m => SiteEventPb(textComponentDataSetPb = m)),
    createEventMapTuple[ButtonComponentDataSet, ButtonComponentDataSetPb](ButtonComponentDataSetPb, e => ButtonComponentDataSetPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(e.name), Some(e.text), Some(ConversionUtils.uuidToByteBuffer(e.action))), m => SiteEventPb(buttonComponentDataSetPb = m)),
    createEventMapTuple[BlogComponentDataSet, BlogComponentDataSetPb](BlogComponentDataSetPb, e => BlogComponentDataSetPb(Some(ConversionUtils.uuidToByteBuffer(e.id)), Some(e.name), Some(e.rss), Some(e.tags)), m => SiteEventPb(blogComponentDataSetPb = m)),
    createEventMapTuple[DomainEntryPointAdded, DomainEntryPointAddedPb](DomainEntryPointAddedPb, e => DomainEntryPointAddedPb(Some(e.domain)), m => SiteEventPb(domainEntryPointAddedPb = m)),
    createEventMapTuple[FreeEntryPointAdded, FreeEntryPointAddedPb](FreeEntryPointAddedPb, e => FreeEntryPointAddedPb(Some(e.userName), Some(e.siteName)), m => SiteEventPb(freeEntryPointAddedPb = m)),
    createEventMapTuple[EntryPointRemoved, EntryPointRemovedPb](EntryPointRemovedPb, e => EntryPointRemovedPb(Some(e.lookupKey)), m => SiteEventPb(entryPointRemovedPb = m)),
    createEventMapTuple[PrimaryEntryPointSet, PrimaryEntryPointSetPb](PrimaryEntryPointSetPb, e => PrimaryEntryPointSetPb(Some(e.lookupKey)), m => SiteEventPb(primaryEntryPointSetPb = m))
  )

  private def toEventPb(event: SiteEvent): SiteEventPb = {
    val (_, generator, setter) = eventMap(event.getClass)
    setter(Some(generator(event)))
  }
}
