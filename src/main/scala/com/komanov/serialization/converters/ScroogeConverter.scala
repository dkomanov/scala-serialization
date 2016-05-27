package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.thriftscala._
import com.twitter.scrooge._
import org.apache.thrift.protocol.TBinaryProtocol

/** https://twitter.github.io/scrooge/ */
object ScroogeConverter extends SiteConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = new SitePb.Immutable(
      Option(ConversionUtils.uuidToByteBuffer(site.id)),
      Option(ConversionUtils.uuidToByteBuffer(site.ownerId)),
      Some(site.revision),
      Some(site.siteType match {
        case SiteType.Flash => SiteTypePb.Flash
        case SiteType.Silverlight => SiteTypePb.Silverlight
        case SiteType.Html5 => SiteTypePb.Html5
        case SiteType.Unknown => SiteTypePb.UnknownSiteType
      }),
      Some(site.flags.map {
        case SiteFlag.Free => SiteFlagPb.Free
        case SiteFlag.Premium => SiteFlagPb.Premium
        case SiteFlag.Unknown => SiteFlagPb.UnknownSiteFlag
      }),
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
      site.siteType.get match {
        case SiteTypePb.Flash => SiteType.Flash
        case SiteTypePb.Silverlight => SiteType.Silverlight
        case SiteTypePb.Html5 => SiteType.Html5
        case SiteTypePb.UnknownSiteType | SiteTypePb.EnumUnknownSiteTypePb(_) => SiteType.Unknown
      },
      site.flags.get.map {
        case SiteFlagPb.Free => SiteFlag.Free
        case SiteFlagPb.Premium => SiteFlag.Premium
        case SiteFlagPb.UnknownSiteFlag | SiteFlagPb.EnumUnknownSiteFlagPb(_) => SiteFlag.Unknown
      },
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

  private def toMetaTagPb(mt: MetaTag) = {
    new MetaTagPb.Immutable(Option(mt.name), Option(mt.value))
  }

  private def fromMetaTagPb(mt: MetaTagPb) = MetaTag(mt.name.get, mt.value.get)

  private def toComponentPb(pc: PageComponent): PageComponentPb = {
    new PageComponentPb.Immutable(
      Option(ConversionUtils.uuidToByteBuffer(pc.id)),
      Some(pc.componentType match {
        case PageComponentType.Text => PageComponentTypePb.Text
        case PageComponentType.Button => PageComponentTypePb.Button
        case PageComponentType.Blog => PageComponentTypePb.Blog
        case PageComponentType.Unknown => PageComponentTypePb.UnknownPageComponentType
      }),
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
    pc.componentType.get match {
      case PageComponentTypePb.Text => PageComponentType.Text
      case PageComponentTypePb.Button => PageComponentType.Button
      case PageComponentTypePb.Blog => PageComponentType.Blog
      case PageComponentTypePb.UnknownPageComponentType | PageComponentTypePb.EnumUnknownPageComponentTypePb(_) => PageComponentType.Unknown
    },
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

}
