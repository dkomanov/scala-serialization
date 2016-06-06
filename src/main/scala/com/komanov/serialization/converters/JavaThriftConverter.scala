package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.thrift._
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.{TBase, TDeserializer, TSerializer}

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

/** https://thrift.apache.org/ */
object JavaThriftConverter extends MyConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = new SitePb()
      .setId(ConversionUtils.uuidToByteBuffer(site.id))
      .setOwnerId(ConversionUtils.uuidToByteBuffer(site.ownerId))
      .setRevision(site.revision)
      .setSiteType(toSiteTypePb(site.siteType))
      .setFlags(site.flags.map(toSiteFlagPb))
      .setName(site.name)
      .setDescription(site.description)
      .setDomains(site.domains.map { d =>
        new DomainPb()
          .setName(d.name)
          .setPrimary(d.primary)
      })
      .setDefaultMetaTags(site.defaultMetaTags.map(toMetaTagPb))
      .setPages(site.pages.map { p =>
        new PagePb()
          .setName(p.name)
          .setPath(p.path)
          .setMetaTags(p.metaTags.map(toMetaTagPb))
          .setComponents(p.components.map(toComponentPb))
      })
      .setEntryPoints(site.entryPoints.map(toEntryPointPb))
      .setPublished(site.published)
      .setDateCreated(ConversionUtils.instantToLong(site.dateCreated))
      .setDateUpdated(ConversionUtils.instantToLong(site.dateUpdated))

    serializer.serialize(proto)
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    val site = new SitePb()
    deserializer.deserialize(site, bytes)

    Site(
      ConversionUtils.bytesToUuid(site.id),
      ConversionUtils.bytesToUuid(site.ownerId),
      site.revision,
      fromSiteTypePb(site.siteType),
      site.flags.map(fromSiteFlagPb),
      site.name,
      site.description,
      site.domains.map(d => Domain(d.name, d.primary)),
      site.defaultMetaTags.map(fromMetaTagPb),
      site.pages.map { p =>
        Page(p.name, p.path, p.metaTags.map(fromMetaTagPb), p.components.map(fromComponentPb))
      },
      site.entryPoints.map(fromEntryPointPb),
      site.published,
      ConversionUtils.longToInstance(site.dateCreated),
      ConversionUtils.longToInstance(site.dateUpdated)
    )
  }

  override def toByteArray(event: SiteEvent): Array[Byte] = {
    val (generator, _) = eventMap(event.getClass)
    val proto = generator(event)
    serializer.serialize(proto)
  }

  override def toByteArray(event: SiteEventData): Array[Byte] = {
    val proto = new SiteEventDataPb()
      .setId(ConversionUtils.uuidToByteBuffer(event.id))
      .setEv(toEvent(event.event))
      .setTimestamp(ConversionUtils.instantToLong(event.timestamp))

    serializer.serialize(proto)
  }

  override def eventFromByteArray(bytes: Array[Byte]): SiteEventData = {
    val proto = new SiteEventDataPb()
    deserializer.deserialize(proto, bytes)

    SiteEventData(
      ConversionUtils.bytesToUuid(proto.id),
      fromEvent(proto.ev),
      ConversionUtils.longToInstance(proto.timestamp)
    )
  }

  private def serializer = new TSerializer(new TBinaryProtocol.Factory())

  private def deserializer = new TDeserializer(new TBinaryProtocol.Factory())

  private def toMetaTagPb(mt: MetaTag) = {
    new MetaTagPb()
      .setName(mt.name)
      .setValue(mt.value)
  }

  private def fromMetaTagPb(mt: MetaTagPb) = MetaTag(mt.name, mt.value)

  private def toComponentPb(pc: PageComponent): PageComponentPb = {
    new PageComponentPb()
      .setId(ConversionUtils.uuidToByteBuffer(pc.id))
      .setComponentType(toPageComponentTypePb(pc.componentType))
      .setData(pc.data match {
        case text: TextComponentData =>
          new PageComponentDataPb()
            .setText(new TextComponentDataPb().setText(text.text))

        case button: ButtonComponentData =>
          new PageComponentDataPb()
            .setButton(
              new ButtonComponentDataPb()
                .setName(button.name)
                .setText(button.text)
                .setAction(ConversionUtils.uuidToByteBuffer(button.action))
            )

        case blog: BlogComponentData =>
          new PageComponentDataPb()
            .setBlog(
              new BlogComponentDataPb()
                .setName(blog.name)
                .setRss(blog.rss)
                .setTags(blog.tags)
            )
      })
      .setPosition(pc.position.map(p => new PageComponentPositionPb().setX(p.x).setY(p.y)).orNull)
      .setDateCreated(ConversionUtils.instantToLong(pc.dateCreated))
      .setDateUpdated(ConversionUtils.instantToLong(pc.dateUpdated))
  }

  private def fromComponentPb(pc: PageComponentPb) = PageComponent(
    ConversionUtils.bytesToUuid(pc.id),
    fromPageComponentTypePb(pc.componentType), {
      if (pc.data.isSetText) {
        val text = pc.data.text
        TextComponentData(text.text)
      } else if (pc.data.isSetButton) {
        val button = pc.data.button
        ButtonComponentData(button.name, button.text, ConversionUtils.bytesToUuid(button.action))
      } else if (pc.data.isSetBlog) {
        val blog = pc.data.blog
        BlogComponentData(blog.name, blog.rss, blog.tags)
      } else {
        throw new RuntimeException("Expected data")
      }
    },
    Option(pc.position).map(p => PageComponentPosition(x = p.x, y = p.y)),
    ConversionUtils.longToInstance(pc.dateCreated),
    ConversionUtils.longToInstance(pc.dateUpdated)
  )

  private def toEntryPointPb(entryPoint: EntryPoint): EntryPointPb = entryPoint match {
    case ep: DomainEntryPoint =>
      new EntryPointPb().setDomain(
        new DomainEntryPointPb()
          .setDomain(ep.domain)
          .setPrimary(ep.primary)
      )

    case ep: FreeEntryPoint =>
      new EntryPointPb().setFree(
        new FreeEntryPointPb()
          .setUserName(ep.userName)
          .setSiteName(ep.siteName)
          .setPrimary(ep.primary)
      )
  }

  private def fromEntryPointPb(entryPoint: EntryPointPb): EntryPoint = {
    if (entryPoint.isSetDomain) {
      val ep = entryPoint.domain
      DomainEntryPoint(ep.domain, ep.primary)
    } else if (entryPoint.isSetFree) {
      val ep = entryPoint.free
      FreeEntryPoint(ep.userName, ep.siteName, ep.primary)
    } else {
      throw new RuntimeException("Expected entry point")
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
    case SiteTypePb.UnknownSiteType => SiteType.Unknown
  }

  private def toSiteFlagPb(f: SiteFlag): SiteFlagPb = f match {
    case SiteFlag.Free => SiteFlagPb.Free
    case SiteFlag.Premium => SiteFlagPb.Premium
    case SiteFlag.Unknown => SiteFlagPb.UnknownSiteFlag
  }

  private def fromSiteFlagPb(f: SiteFlagPb): SiteFlag = f match {
    case SiteFlagPb.Free => SiteFlag.Free
    case SiteFlagPb.Premium => SiteFlag.Premium
    case SiteFlagPb.UnknownSiteFlag => SiteFlag.Unknown
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
    case PageComponentTypePb.UnknownPageComponentType => PageComponentType.Unknown
  }

  type GeneratorType = SiteEvent => TBase[_, _]
  type SetterType = TBase[_, _] => SiteEventPb

  private def createEventMapTuple[T: ClassTag, M](generator: T => M,
                                                  setter: M => SiteEventPb): (Class[_], (GeneratorType, SetterType)) = {
    (implicitly[ClassTag[T]].runtimeClass, (generator.asInstanceOf[GeneratorType], setter.asInstanceOf[SetterType]))
  }

  private val eventMap = Map[Class[_], (GeneratorType, SetterType)](
    createEventMapTuple[SiteCreated, SiteCreatedPb](e => new SiteCreatedPb().setId(ConversionUtils.uuidToByteBuffer(e.id)).setOwnerId(ConversionUtils.uuidToByteBuffer(e.ownerId)).setSiteType(toSiteTypePb(e.siteType)), m => new SiteEventPb().setSiteCreatedPb(m)),
    createEventMapTuple[SiteNameSet, SiteNameSetPb](e => new SiteNameSetPb().setName(e.name), m => new SiteEventPb().setSiteNameSetPb(m)),
    createEventMapTuple[SiteDescriptionSet, SiteDescriptionSetPb](e => new SiteDescriptionSetPb().setDescription(e.description), m => new SiteEventPb().setSiteDescriptionSetPb(m)),
    createEventMapTuple[SiteRevisionSet, SiteRevisionSetPb](e => new SiteRevisionSetPb().setRevision(e.revision), m => new SiteEventPb().setSiteRevisionSetPb(m)),
    createEventMapTuple[SitePublished, SitePublishedPb](e => new SitePublishedPb(), m => new SiteEventPb().setSitePublishedPb(m)),
    createEventMapTuple[SiteUnpublished, SiteUnpublishedPb](e => new SiteUnpublishedPb(), m => new SiteEventPb().setSiteUnpublishedPb(m)),
    createEventMapTuple[SiteFlagAdded, SiteFlagAddedPb](e => new SiteFlagAddedPb().setSiteFlag(toSiteFlagPb(e.siteFlag)), m => new SiteEventPb().setSiteFlagAddedPb(m)),
    createEventMapTuple[SiteFlagRemoved, SiteFlagRemovedPb](e => new SiteFlagRemovedPb().setSiteFlag(toSiteFlagPb(e.siteFlag)), m => new SiteEventPb().setSiteFlagRemovedPb(m)),
    createEventMapTuple[DomainAdded, DomainAddedPb](e => new DomainAddedPb().setName(e.name), m => new SiteEventPb().setDomainAddedPb(m)),
    createEventMapTuple[DomainRemoved, DomainRemovedPb](e => new DomainRemovedPb().setName(e.name), m => new SiteEventPb().setDomainRemovedPb(m)),
    createEventMapTuple[PrimaryDomainSet, PrimaryDomainSetPb](e => new PrimaryDomainSetPb().setName(e.name), m => new SiteEventPb().setPrimaryDomainSetPb(m)),
    createEventMapTuple[DefaultMetaTagAdded, DefaultMetaTagAddedPb](e => new DefaultMetaTagAddedPb().setName(e.name).setValue(e.value), m => new SiteEventPb().setDefaultMetaTagAddedPb(m)),
    createEventMapTuple[DefaultMetaTagRemoved, DefaultMetaTagRemovedPb](e => new DefaultMetaTagRemovedPb().setName(e.name), m => new SiteEventPb().setDefaultMetaTagRemovedPb(m)),
    createEventMapTuple[PageAdded, PageAddedPb](e => new PageAddedPb().setPath(e.path), m => new SiteEventPb().setPageAddedPb(m)),
    createEventMapTuple[PageRemoved, PageRemovedPb](e => new PageRemovedPb().setPath(e.path), m => new SiteEventPb().setPageRemovedPb(m)),
    createEventMapTuple[PageNameSet, PageNameSetPb](e => new PageNameSetPb().setPath(e.path).setName(e.name), m => new SiteEventPb().setPageNameSetPb(m)),
    createEventMapTuple[PageMetaTagAdded, PageMetaTagAddedPb](e => new PageMetaTagAddedPb().setPath(e.path).setName(e.name).setValue(e.value), m => new SiteEventPb().setPageMetaTagAddedPb(m)),
    createEventMapTuple[PageMetaTagRemoved, PageMetaTagRemovedPb](e => new PageMetaTagRemovedPb().setPath(e.path).setName(e.name), m => new SiteEventPb().setPageMetaTagRemovedPb(m)),
    createEventMapTuple[PageComponentAdded, PageComponentAddedPb](e => new PageComponentAddedPb().setPagePath(e.pagePath).setId(ConversionUtils.uuidToByteBuffer(e.id)).setComponentType(toPageComponentTypePb(e.componentType)), m => new SiteEventPb().setPageComponentAddedPb(m)),
    createEventMapTuple[PageComponentRemoved, PageComponentRemovedPb](e => new PageComponentRemovedPb().setPagePath(e.pagePath).setId(ConversionUtils.uuidToByteBuffer(e.id)), m => new SiteEventPb().setPageComponentRemovedPb(m)),
    createEventMapTuple[PageComponentPositionSet, PageComponentPositionSetPb](e => new PageComponentPositionSetPb().setId(ConversionUtils.uuidToByteBuffer(e.id)).setX(e.position.x).setY(e.position.y), m => new SiteEventPb().setPageComponentPositionSetPb(m)),
    createEventMapTuple[PageComponentPositionReset, PageComponentPositionResetPb](e => new PageComponentPositionResetPb().setId(ConversionUtils.uuidToByteBuffer(e.id)), m => new SiteEventPb().setPageComponentPositionResetPb(m)),
    createEventMapTuple[TextComponentDataSet, TextComponentDataSetPb](e => new TextComponentDataSetPb().setId(ConversionUtils.uuidToByteBuffer(e.id)).setText(e.text), m => new SiteEventPb().setTextComponentDataSetPb(m)),
    createEventMapTuple[ButtonComponentDataSet, ButtonComponentDataSetPb](e => new ButtonComponentDataSetPb().setId(ConversionUtils.uuidToByteBuffer(e.id)).setName(e.name).setText(e.text).setAction(ConversionUtils.uuidToByteBuffer(e.action)), m => new SiteEventPb().setButtonComponentDataSetPb(m)),
    createEventMapTuple[BlogComponentDataSet, BlogComponentDataSetPb](e => new BlogComponentDataSetPb().setId(ConversionUtils.uuidToByteBuffer(e.id)).setName(e.name).setRss(e.rss).setTags(e.tags), m => new SiteEventPb().setBlogComponentDataSetPb(m)),
    createEventMapTuple[DomainEntryPointAdded, DomainEntryPointAddedPb](e => new DomainEntryPointAddedPb().setDomain(e.domain), m => new SiteEventPb().setDomainEntryPointAddedPb(m)),
    createEventMapTuple[FreeEntryPointAdded, FreeEntryPointAddedPb](e => new FreeEntryPointAddedPb().setUserName(e.userName).setSiteName(e.siteName), m => new SiteEventPb().setFreeEntryPointAddedPb(m)),
    createEventMapTuple[EntryPointRemoved, EntryPointRemovedPb](e => new EntryPointRemovedPb().setLookupKey(e.lookupKey), m => new SiteEventPb().setEntryPointRemovedPb(m)),
    createEventMapTuple[PrimaryEntryPointSet, PrimaryEntryPointSetPb](e => new PrimaryEntryPointSetPb().setLookupKey(e.lookupKey), m => new SiteEventPb().setPrimaryEntryPointSetPb(m))
  )

  private def toEvent(siteEvent: SiteEvent): SiteEventPb = {
    val (generator, setter) = eventMap(siteEvent.getClass)
    setter(generator(siteEvent))
  }

  private def fromEvent(proto: SiteEventPb): SiteEvent = {
    if (proto.isSetSiteCreatedPb) {
      val ev = proto.getSiteCreatedPb
      SiteCreated(ConversionUtils.bytesToUuid(ev.id), ConversionUtils.bytesToUuid(ev.ownerId), fromSiteTypePb(ev.getSiteType))
    } else if (proto.isSetSiteNameSetPb) {
      val ev = proto.getSiteNameSetPb
      SiteNameSet(ev.getName)
    } else if (proto.isSetSiteDescriptionSetPb) {
      val ev = proto.getSiteDescriptionSetPb
      SiteDescriptionSet(ev.getDescription)
    } else if (proto.isSetSiteRevisionSetPb) {
      val ev = proto.getSiteRevisionSetPb
      SiteRevisionSet(ev.getRevision)
    } else if (proto.isSetSitePublishedPb) {
      val ev = proto.getSitePublishedPb
      SitePublished()
    } else if (proto.isSetSiteUnpublishedPb) {
      val ev = proto.getSiteUnpublishedPb
      SiteUnpublished()
    } else if (proto.isSetSiteFlagAddedPb) {
      val ev = proto.getSiteFlagAddedPb
      SiteFlagAdded(fromSiteFlagPb(ev.getSiteFlag))
    } else if (proto.isSetSiteFlagRemovedPb) {
      val ev = proto.getSiteFlagRemovedPb
      SiteFlagRemoved(fromSiteFlagPb(ev.getSiteFlag))
    } else if (proto.isSetDomainAddedPb) {
      val ev = proto.getDomainAddedPb
      DomainAdded(ev.getName)
    } else if (proto.isSetDomainRemovedPb) {
      val ev = proto.getDomainRemovedPb
      DomainRemoved(ev.getName)
    } else if (proto.isSetPrimaryDomainSetPb) {
      val ev = proto.getPrimaryDomainSetPb
      PrimaryDomainSet(ev.getName)
    } else if (proto.isSetDefaultMetaTagAddedPb) {
      val ev = proto.getDefaultMetaTagAddedPb
      DefaultMetaTagAdded(ev.getName, ev.getValue)
    } else if (proto.isSetDefaultMetaTagRemovedPb) {
      val ev = proto.getDefaultMetaTagRemovedPb
      DefaultMetaTagRemoved(ev.getName)
    } else if (proto.isSetPageAddedPb) {
      val ev = proto.getPageAddedPb
      PageAdded(ev.getPath)
    } else if (proto.isSetPageRemovedPb) {
      val ev = proto.getPageRemovedPb
      PageRemoved(ev.getPath)
    } else if (proto.isSetPageNameSetPb) {
      val ev = proto.getPageNameSetPb
      PageNameSet(ev.getPath, ev.getName)
    } else if (proto.isSetPageMetaTagAddedPb) {
      val ev = proto.getPageMetaTagAddedPb
      PageMetaTagAdded(ev.getPath, ev.getName, ev.getValue)
    } else if (proto.isSetPageMetaTagRemovedPb) {
      val ev = proto.getPageMetaTagRemovedPb
      PageMetaTagRemoved(ev.getPath, ev.getName)
    } else if (proto.isSetPageComponentAddedPb) {
      val ev = proto.getPageComponentAddedPb
      PageComponentAdded(ev.getPagePath, ConversionUtils.bytesToUuid(ev.id), fromPageComponentTypePb(ev.getComponentType))
    } else if (proto.isSetPageComponentRemovedPb) {
      val ev = proto.getPageComponentRemovedPb
      PageComponentRemoved(ev.getPagePath, ConversionUtils.bytesToUuid(ev.id))
    } else if (proto.isSetPageComponentPositionSetPb) {
      val ev = proto.getPageComponentPositionSetPb
      PageComponentPositionSet(ConversionUtils.bytesToUuid(ev.id), PageComponentPosition(ev.getX, ev.getY))
    } else if (proto.isSetPageComponentPositionResetPb) {
      val ev = proto.getPageComponentPositionResetPb
      PageComponentPositionReset(ConversionUtils.bytesToUuid(ev.id))
    } else if (proto.isSetTextComponentDataSetPb) {
      val ev = proto.getTextComponentDataSetPb
      TextComponentDataSet(ConversionUtils.bytesToUuid(ev.id), ev.getText)
    } else if (proto.isSetButtonComponentDataSetPb) {
      val ev = proto.getButtonComponentDataSetPb
      ButtonComponentDataSet(ConversionUtils.bytesToUuid(ev.id), ev.getName, ev.getText, ConversionUtils.bytesToUuid(ev.action))
    } else if (proto.isSetBlogComponentDataSetPb) {
      val ev = proto.getBlogComponentDataSetPb
      BlogComponentDataSet(ConversionUtils.bytesToUuid(ev.id), ev.getName, ev.isRss, ev.isTags)
    } else if (proto.isSetDomainEntryPointAddedPb) {
      val ev = proto.getDomainEntryPointAddedPb
      DomainEntryPointAdded(ev.getDomain)
    } else if (proto.isSetFreeEntryPointAddedPb) {
      val ev = proto.getFreeEntryPointAddedPb
      FreeEntryPointAdded(ev.getUserName, ev.getSiteName)
    } else if (proto.isSetEntryPointRemovedPb) {
      val ev = proto.getEntryPointRemovedPb
      EntryPointRemoved(ev.getLookupKey)
    } else if (proto.isSetPrimaryEntryPointSetPb) {
      val ev = proto.getPrimaryEntryPointSetPb
      PrimaryEntryPointSet(ev.getLookupKey)
    } else {
      throw new IllegalStateException("Unknown event type")
    }
  }
}
