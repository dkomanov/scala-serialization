package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.thrift._
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.{TDeserializer, TSerializer}

import scala.collection.JavaConversions._

/** https://thrift.apache.org/ */
object JavaThriftConverter extends SiteConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = new SitePb()
      .setId(ConversionUtils.uuidToByteBuffer(site.id))
      .setOwnerId(ConversionUtils.uuidToByteBuffer(site.ownerId))
      .setRevision(site.revision)
      .setSiteType(site.siteType match {
        case SiteType.Flash => SiteTypePb.Flash
        case SiteType.Silverlight => SiteTypePb.Silverlight
        case SiteType.Html5 => SiteTypePb.Html5
        case SiteType.Unknown => SiteTypePb.UnknownSiteType
      })
      .setFlags(site.flags.map {
        case SiteFlag.Free => SiteFlagPb.Free
        case SiteFlag.Premium => SiteFlagPb.Premium
        case SiteFlag.Unknown => SiteFlagPb.UnknownSiteFlag
      })
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

    new TSerializer(new TBinaryProtocol.Factory()).serialize(proto)
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    val site = new SitePb()
    new TDeserializer(new TBinaryProtocol.Factory()).deserialize(site, bytes)

    Site(
      ConversionUtils.bytesToUuid(site.id),
      ConversionUtils.bytesToUuid(site.ownerId),
      site.revision,
      site.siteType match {
        case SiteTypePb.Flash => SiteType.Flash
        case SiteTypePb.Silverlight => SiteType.Silverlight
        case SiteTypePb.Html5 => SiteType.Html5
        case SiteTypePb.UnknownSiteType => SiteType.Unknown
      },
      site.flags.map {
        case SiteFlagPb.Free => SiteFlag.Free
        case SiteFlagPb.Premium => SiteFlag.Premium
        case SiteFlagPb.UnknownSiteFlag => SiteFlag.Unknown
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

  private def toMetaTagPb(mt: MetaTag) = {
    new MetaTagPb()
      .setName(mt.name)
      .setValue(mt.value)
  }

  private def fromMetaTagPb(mt: MetaTagPb) = MetaTag(mt.name, mt.value)

  private def toComponentPb(pc: PageComponent): PageComponentPb = {
    new PageComponentPb()
      .setId(ConversionUtils.uuidToByteBuffer(pc.id))
      .setComponentType(pc.componentType match {
        case PageComponentType.Text => PageComponentTypePb.Text
        case PageComponentType.Button => PageComponentTypePb.Button
        case PageComponentType.Blog => PageComponentTypePb.Blog
        case PageComponentType.Unknown => PageComponentTypePb.UnknownPageComponentType
      })
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
    pc.componentType match {
      case PageComponentTypePb.Text => PageComponentType.Text
      case PageComponentTypePb.Button => PageComponentType.Button
      case PageComponentTypePb.Blog => PageComponentType.Blog
      case PageComponentTypePb.UnknownPageComponentType => PageComponentType.Unknown
    }, {
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

}
