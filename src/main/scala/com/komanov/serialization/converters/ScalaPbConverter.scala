package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.protos.site.EntryPointPb.{DomainEntryPointPb, FreeEntryPointPb}
import com.komanov.serialization.domain.protos.site.PageComponentDataPb._
import com.komanov.serialization.domain.protos.site._

/** https://github.com/trueaccord/ScalaPB */
object ScalaPbConverter extends SiteConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = SitePb(
      ConversionUtils.uuidToBytes(site.id),
      ConversionUtils.uuidToBytes(site.ownerId),
      site.revision,
      site.siteType match {
        case SiteType.Flash => SiteTypePb.Flash
        case SiteType.Silverlight => SiteTypePb.Silverlight
        case SiteType.Html5 => SiteTypePb.Html5
        case SiteType.Unknown => SiteTypePb.UnknownSiteType
      },
      site.flags.map {
        case SiteFlag.Free => SiteFlagPb.Free
        case SiteFlag.Premium => SiteFlagPb.Premium
        case SiteFlag.Unknown => SiteFlagPb.UnknownSiteFlag
      },
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

}
