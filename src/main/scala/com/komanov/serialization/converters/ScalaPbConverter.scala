package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.protos.events.SiteEventPb.Ev
import com.komanov.serialization.domain.protos.events._
import com.komanov.serialization.domain.protos.site.EntryPointPb.{DomainEntryPointPb, FreeEntryPointPb}
import com.komanov.serialization.domain.protos.site.PageComponentDataPb._
import com.komanov.serialization.domain.protos.site._

/** https://github.com/trueaccord/ScalaPB */
object ScalaPbConverter extends SiteConverter with EventConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = SitePb(
      ConversionUtils.uuidToBytes(site.id),
      ConversionUtils.uuidToBytes(site.ownerId),
      site.revision,
      toSiteTypePb(site.siteType),
      site.flags.map(toSiteFlagPb),
      site.name,
      site.description,
      site.domains.map(d => DomainPb(d.name, d.primary)),
      site.defaultMetaTags.map(toMetaTagPb),
      site.pages.map { p =>
        PagePb(p.name, p.path, p.metaTags.map(toMetaTagPb), p.components.map(toComponentPb))
      },
      site.entryPoints.map(toEntryPointPb),
      site.published,
      ConversionUtils.instantToLong(site.dateCreated),
      ConversionUtils.instantToLong(site.dateUpdated)
    )
    proto.toByteArray
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    val site = SitePb.parseFrom(bytes)
    Site(
      ConversionUtils.bytesToUuid(site.id),
      ConversionUtils.bytesToUuid(site.ownerId),
      site.revision,
      site.siteType match {
        case SiteTypePb.Flash => SiteType.Flash
        case SiteTypePb.Silverlight => SiteType.Silverlight
        case SiteTypePb.Html5 => SiteType.Html5
        case SiteTypePb.UnknownSiteType | SiteTypePb.Unrecognized(_) => SiteType.Unknown
      },
      site.flags.map {
        case SiteFlagPb.Free => SiteFlag.Free
        case SiteFlagPb.Premium => SiteFlag.Premium
        case SiteFlagPb.UnknownSiteFlag | SiteFlagPb.Unrecognized(_) => SiteFlag.Unknown
      },
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

  override def toByteArray(event: SiteEventData): Array[Byte] = {
    val eventPb = SiteEventPb(event.event match {
      case e: SiteCreated => Ev.SiteCreatedPb(SiteCreatedPb(ConversionUtils.uuidToBytes(e.id), ConversionUtils.uuidToBytes(e.ownerId), toSiteTypePb(e.siteType)))
      case e: SiteNameSet => Ev.SiteNameSetPb(SiteNameSetPb(e.name))
      case e: SiteDescriptionSet => Ev.SiteDescriptionSetPb(SiteDescriptionSetPb(e.description))
      case e: SiteRevisionSet => Ev.SiteRevisionSetPb(SiteRevisionSetPb(e.revision))
      case e: SitePublished => Ev.SitePublishedPb(SitePublishedPb())
      case e: SiteUnpublished => Ev.SiteUnpublishedPb(SiteUnpublishedPb())
      case e: SiteFlagAdded => Ev.SiteFlagAddedPb(SiteFlagAddedPb(toSiteFlagPb(e.siteFlag)))
      case e: SiteFlagRemoved => Ev.SiteFlagRemovedPb(SiteFlagRemovedPb(toSiteFlagPb(e.siteFlag)))
      case e: DomainAdded => Ev.DomainAddedPb(DomainAddedPb(e.name))
      case e: DomainRemoved => Ev.DomainRemovedPb(DomainRemovedPb(e.name))
      case e: PrimaryDomainSet => Ev.PrimaryDomainSetPb(PrimaryDomainSetPb(e.name))
      case e: DefaultMetaTagAdded => Ev.DefaultMetaTagAddedPb(DefaultMetaTagAddedPb(e.name, e.value))
      case e: DefaultMetaTagRemoved => Ev.DefaultMetaTagRemovedPb(DefaultMetaTagRemovedPb(e.name))
      case e: PageAdded => Ev.PageAddedPb(PageAddedPb(e.path))
      case e: PageRemoved => Ev.PageRemovedPb(PageRemovedPb(e.path))
      case e: PageNameSet => Ev.PageNameSetPb(PageNameSetPb(e.path, e.name))
      case e: PageMetaTagAdded => Ev.PageMetaTagAddedPb(PageMetaTagAddedPb(e.path, e.name, e.value))
      case e: PageMetaTagRemoved => Ev.PageMetaTagRemovedPb(PageMetaTagRemovedPb(e.path, e.name))
      case e: PageComponentAdded => Ev.PageComponentAddedPb(PageComponentAddedPb(e.pagePath, ConversionUtils.uuidToBytes(e.id), toPageComponentTypePb(e.componentType)))
      case e: PageComponentRemoved => Ev.PageComponentRemovedPb(PageComponentRemovedPb(e.pagePath, ConversionUtils.uuidToBytes(e.id)))
      case e: PageComponentPositionSet => Ev.PageComponentPositionSetPb(PageComponentPositionSetPb(ConversionUtils.uuidToBytes(e.id), e.position.x, e.position.y))
      case e: PageComponentPositionReset => Ev.PageComponentPositionResetPb(PageComponentPositionResetPb(ConversionUtils.uuidToBytes(e.id)))
      case e: TextComponentDataSet => Ev.TextComponentDataSetPb(TextComponentDataSetPb(ConversionUtils.uuidToBytes(e.id), e.text))
      case e: ButtonComponentDataSet => Ev.ButtonComponentDataSetPb(ButtonComponentDataSetPb(ConversionUtils.uuidToBytes(e.id), e.name, e.text, ConversionUtils.uuidToBytes(e.action)))
      case e: BlogComponentDataSet => Ev.BlogComponentDataSetPb(BlogComponentDataSetPb(ConversionUtils.uuidToBytes(e.id), e.name, e.rss, e.tags))
      case e: DomainEntryPointAdded => Ev.DomainEntryPointAddedPb(DomainEntryPointAddedPb(e.domain))
      case e: FreeEntryPointAdded => Ev.FreeEntryPointAddedPb(FreeEntryPointAddedPb(e.userName, e.siteName))
      case e: EntryPointRemoved => Ev.EntryPointRemovedPb(EntryPointRemovedPb(e.lookupKey))
      case e: PrimaryEntryPointSet => Ev.PrimaryEntryPointSetPb(PrimaryEntryPointSetPb(e.lookupKey))
    })

    val proto = SiteEventDataPb(
      ConversionUtils.uuidToBytes(event.id),
      Some(eventPb),
      ConversionUtils.instantToLong(event.timestamp)
    )

    proto.toByteArray
  }

  override def eventFromByteArray(bytes: Array[Byte]): SiteEventData = {
    val event = SiteEventDataPb.parseFrom(bytes)

    SiteEventData(
      ConversionUtils.bytesToUuid(event.id),
      event.ev.getOrElse(throw new IllegalStateException("Expected event")).ev match {
        case Ev.SiteCreatedPb(e) => SiteCreated(ConversionUtils.bytesToUuid(e.id), ConversionUtils.bytesToUuid(e.ownerId), fromSiteTypePb(e.siteType))
        case Ev.SiteNameSetPb(e) => SiteNameSet(e.name)
        case Ev.SiteDescriptionSetPb(e) => SiteDescriptionSet(e.description)
        case Ev.SiteRevisionSetPb(e) => SiteRevisionSet(e.revision)
        case Ev.SitePublishedPb(e) => SitePublished()
        case Ev.SiteUnpublishedPb(e) => SiteUnpublished()
        case Ev.SiteFlagAddedPb(e) => SiteFlagAdded(fromSiteFlagPb(e.siteFlag))
        case Ev.SiteFlagRemovedPb(e) => SiteFlagRemoved(fromSiteFlagPb(e.siteFlag))
        case Ev.DomainAddedPb(e) => DomainAdded(e.name)
        case Ev.DomainRemovedPb(e) => DomainRemoved(e.name)
        case Ev.PrimaryDomainSetPb(e) => PrimaryDomainSet(e.name)
        case Ev.DefaultMetaTagAddedPb(e) => DefaultMetaTagAdded(e.name, e.value)
        case Ev.DefaultMetaTagRemovedPb(e) => DefaultMetaTagRemoved(e.name)
        case Ev.PageAddedPb(e) => PageAdded(e.path)
        case Ev.PageRemovedPb(e) => PageRemoved(e.path)
        case Ev.PageNameSetPb(e) => PageNameSet(e.path, e.name)
        case Ev.PageMetaTagAddedPb(e) => PageMetaTagAdded(e.path, e.name, e.value)
        case Ev.PageMetaTagRemovedPb(e) => PageMetaTagRemoved(e.path, e.name)
        case Ev.PageComponentAddedPb(e) => PageComponentAdded(e.pagePath, ConversionUtils.bytesToUuid(e.id), fromPageComponentTypePb(e.componentType))
        case Ev.PageComponentRemovedPb(e) => PageComponentRemoved(e.pagePath, ConversionUtils.bytesToUuid(e.id))
        case Ev.PageComponentPositionSetPb(e) => PageComponentPositionSet(ConversionUtils.bytesToUuid(e.id), PageComponentPosition(e.x, e.y))
        case Ev.PageComponentPositionResetPb(e) => PageComponentPositionReset(ConversionUtils.bytesToUuid(e.id))
        case Ev.TextComponentDataSetPb(e) => TextComponentDataSet(ConversionUtils.bytesToUuid(e.id), e.text)
        case Ev.ButtonComponentDataSetPb(e) => ButtonComponentDataSet(ConversionUtils.bytesToUuid(e.id), e.name, e.text, ConversionUtils.bytesToUuid(e.action))
        case Ev.BlogComponentDataSetPb(e) => BlogComponentDataSet(ConversionUtils.bytesToUuid(e.id), e.name, e.rss, e.tags)
        case Ev.DomainEntryPointAddedPb(e) => DomainEntryPointAdded(e.domain)
        case Ev.FreeEntryPointAddedPb(e) => FreeEntryPointAdded(e.userName, e.siteName)
        case Ev.EntryPointRemovedPb(e) => EntryPointRemoved(e.lookupKey)
        case Ev.PrimaryEntryPointSetPb(e) => PrimaryEntryPointSet(e.lookupKey)

        case Ev.Empty => throw new IllegalStateException("Unknown site type")
      },
      ConversionUtils.longToInstance(event.timestamp)
    )
  }

  private def toMetaTagPb(mt: MetaTag) = MetaTagPb(mt.name, mt.value)

  private def fromMetaTagPb(mt: MetaTagPb) = MetaTag(mt.name, mt.value)

  private def toComponentPb(pc: PageComponent) = PageComponentPb(
    ConversionUtils.uuidToBytes(pc.id),
    pc.componentType match {
      case PageComponentType.Text => PageComponentTypePb.Text
      case PageComponentType.Button => PageComponentTypePb.Button
      case PageComponentType.Blog => PageComponentTypePb.Blog
      case PageComponentType.Unknown => PageComponentTypePb.UnknownPageComponentType
    },
    Some(PageComponentDataPb(
      pc.data match {
        case text: TextComponentData => Data.Text(TextComponentDataPb(text.text))
        case button: ButtonComponentData => Data.Button(ButtonComponentDataPb(button.name, button.text, ConversionUtils.uuidToBytes(button.action)))
        case blog: BlogComponentData => Data.Blog(BlogComponentDataPb(blog.name, blog.rss, blog.tags))
      }
    )),
    pc.position.map(p => PageComponentPositionPb(x = p.x, y = p.y)),
    ConversionUtils.instantToLong(pc.dateCreated),
    ConversionUtils.instantToLong(pc.dateUpdated)
  )

  private def fromComponentPb(pc: PageComponentPb) = PageComponent(
    ConversionUtils.bytesToUuid(pc.id),
    pc.componentType match {
      case PageComponentTypePb.Text => PageComponentType.Text
      case PageComponentTypePb.Button => PageComponentType.Button
      case PageComponentTypePb.Blog => PageComponentType.Blog
      case PageComponentTypePb.UnknownPageComponentType | PageComponentTypePb.Unrecognized(_) => PageComponentType.Unknown
    },
    pc.data.map(_.data).get match {
      case Data.Text(text) => TextComponentData(text.text)
      case Data.Button(button) => ButtonComponentData(button.name, button.text, ConversionUtils.bytesToUuid(button.action))
      case Data.Blog(blog) => BlogComponentData(blog.name, blog.rss, blog.tags)
      case Data.Empty => throw new RuntimeException("Expected data")
    },
    pc.position.map(p => PageComponentPosition(x = p.x, y = p.y)),
    ConversionUtils.longToInstance(pc.dateCreated),
    ConversionUtils.longToInstance(pc.dateUpdated)
  )

  private def toEntryPointPb(entryPoint: EntryPoint): EntryPointPb = entryPoint match {
    case ep: DomainEntryPoint => EntryPointPb(EntryPointPb.Ep.Domain(DomainEntryPointPb(ep.domain, ep.primary)))
    case ep: FreeEntryPoint => EntryPointPb(EntryPointPb.Ep.Free(FreeEntryPointPb(ep.userName, ep.siteName, ep.primary)))
  }

  private def fromEntryPointPb(entryPoint: EntryPointPb): EntryPoint = entryPoint.ep match {
    case EntryPointPb.Ep.Domain(ep) => DomainEntryPoint(ep.domain, ep.primary)
    case EntryPointPb.Ep.Free(ep) => FreeEntryPoint(ep.userName, ep.siteName, ep.primary)
    case EntryPointPb.Ep.Empty => throw new RuntimeException("Expected entry point")
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
    case SiteTypePb.UnknownSiteType | SiteTypePb.Unrecognized(_) => SiteType.Unknown
  }

  private def toSiteFlagPb(f: SiteFlag): SiteFlagPb = f match {
    case SiteFlag.Free => SiteFlagPb.Free
    case SiteFlag.Premium => SiteFlagPb.Premium
    case SiteFlag.Unknown => SiteFlagPb.UnknownSiteFlag
  }

  private def fromSiteFlagPb(f: SiteFlagPb): SiteFlag = f match {
    case SiteFlagPb.Free => SiteFlag.Free
    case SiteFlagPb.Premium => SiteFlag.Premium
    case SiteFlagPb.UnknownSiteFlag | SiteFlagPb.Unrecognized(_) => SiteFlag.Unknown
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
    case PageComponentTypePb.UnknownPageComponentType | PageComponentTypePb.Unrecognized(_) => PageComponentType.Unknown
  }
}
