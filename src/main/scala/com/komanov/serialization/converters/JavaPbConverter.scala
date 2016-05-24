package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.protos.Site.EntryPointPb._
import com.komanov.serialization.domain.protos.Site.PageComponentDataPb._
import com.komanov.serialization.domain.protos.Site._

import scala.collection.JavaConversions._

object JavaPbConverter extends SiteConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = SitePb.newBuilder()
      .setId(ConversionUtils.uuidToBytes(site.id))
      .setOwnerId(ConversionUtils.uuidToBytes(site.ownerId))
      .setRevision(site.revision)
      .setSiteType(site.siteType match {
        case SiteType.Flash => SiteTypePb.Flash
        case SiteType.Silverlight => SiteTypePb.Silverlight
        case SiteType.Html5 => SiteTypePb.Html5
        case SiteType.Unknown => SiteTypePb.UnknownSiteType
      })
      .addAllFlags(site.flags.map {
        case SiteFlag.Free => SiteFlagPb.Free
        case SiteFlag.Premium => SiteFlagPb.Premium
        case SiteFlag.Unknown => SiteFlagPb.UnknownSiteFlag
      }.toSeq)
      .setName(site.name)
      .setDescription(site.description)
      .addAllDomains(site.domains.map(d =>
        DomainPb.newBuilder()
          .setName(d.name)
          .setPrimary(d.primary)
          .build()
      ))
      .addAllDefaultMetaTags(site.defaultMetaTags.map(toMetaTagPb))
      .addAllPages(site.pages.map { p =>
        PagePb.newBuilder()
          .setName(p.name)
          .setPath(p.path)
          .addAllMetaTags(p.metaTags.map(toMetaTagPb))
          .addAllComponents(p.components.map(toComponentPb))
          .build()
      })
      .addAllEntryPoints(site.entryPoints.map(toEntryPointPb).toSeq)
      .setPublished(site.published)
      .setDateCreated(ConversionUtils.instantToLong(site.dateCreated))
      .setDateUpdated(ConversionUtils.instantToLong(site.dateUpdated))
    proto.build().toByteArray
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    val site = SitePb.parseFrom(bytes)
    Site(
      ConversionUtils.bytesToUuid(site.getId),
      ConversionUtils.bytesToUuid(site.getOwnerId),
      site.getRevision,
      site.getSiteType match {
        case SiteTypePb.Flash => SiteType.Flash
        case SiteTypePb.Silverlight => SiteType.Silverlight
        case SiteTypePb.Html5 => SiteType.Html5
        case SiteTypePb.UnknownSiteType | SiteTypePb.UNRECOGNIZED => SiteType.Unknown
      },
      site.getFlagsList.map {
        case SiteFlagPb.Free => SiteFlag.Free
        case SiteFlagPb.Premium => SiteFlag.Premium
        case SiteFlagPb.UnknownSiteFlag | SiteFlagPb.UNRECOGNIZED => SiteFlag.Unknown
      },
      site.getName,
      site.getDescription,
      site.getDomainsList.map(d => Domain(d.getName, d.getPrimary)),
      site.getDefaultMetaTagsList.map(fromMetaTagPb),
      site.getPagesList.map { p =>
        Page(p.getName, p.getPath, p.getMetaTagsList.map(fromMetaTagPb), p.getComponentsList.map(fromComponentPb))
      },
      site.getEntryPointsList.map(fromEntryPointPb),
      site.getPublished,
      ConversionUtils.longToInstance(site.getDateCreated),
      ConversionUtils.longToInstance(site.getDateUpdated)
    )
  }

  private def toMetaTagPb(mt: MetaTag) = {
    MetaTagPb.newBuilder()
      .setName(mt.name)
      .setValue(mt.value)
      .build()
  }

  private def fromMetaTagPb(mt: MetaTagPb) = MetaTag(mt.getName, mt.getValue)

  private def toComponentPb(pc: PageComponent) = {
    val proto = PageComponentPb.newBuilder()
      .setId(ConversionUtils.uuidToBytes(pc.id))
      .setComponentType(pc.componentType match {
        case PageComponentType.Text => PageComponentTypePb.Text
        case PageComponentType.Button => PageComponentTypePb.Button
        case PageComponentType.Blog => PageComponentTypePb.Blog
        case PageComponentType.Unknown => PageComponentTypePb.UnknownPageComponentType
      })
      .setData(pc.data match {
        case text: TextComponentData =>
          PageComponentDataPb
            .newBuilder()
            .setText(
              TextComponentDataPb.newBuilder()
                .setText(text.text)
            )
            .build()

        case button: ButtonComponentData =>
          PageComponentDataPb.newBuilder()
            .setButton(
              ButtonComponentDataPb.newBuilder()
                .setName(button.name)
                .setText(button.text)
                .setAction(ConversionUtils.uuidToBytes(button.action))
                .build()
            )
            .build()

        case blog: BlogComponentData =>
          PageComponentDataPb.newBuilder()
            .setBlog(
              BlogComponentDataPb.newBuilder()
                .setName(blog.name)
                .setRss(blog.rss)
                .setTags(blog.tags)
                .build()
            )
            .build()
      })
      .setDateCreated(ConversionUtils.instantToLong(pc.dateCreated))
      .setDateUpdated(ConversionUtils.instantToLong(pc.dateUpdated))

    pc.position.foreach(p => proto.setPosition(PageComponentPositionPb.newBuilder().setX(p.x).setY(p.y).build()))

    proto.build()
  }

  private def fromComponentPb(pc: PageComponentPb) = PageComponent(
    ConversionUtils.bytesToUuid(pc.getId),
    pc.getComponentType match {
      case PageComponentTypePb.Text => PageComponentType.Text
      case PageComponentTypePb.Button => PageComponentType.Button
      case PageComponentTypePb.Blog => PageComponentType.Blog
      case PageComponentTypePb.UnknownPageComponentType | PageComponentTypePb.UNRECOGNIZED => PageComponentType.Unknown
    },
    pc.getData.getDataCase match {
      case DataCase.TEXT =>
        val text = pc.getData.getText
        TextComponentData(text.getText)

      case DataCase.BUTTON =>
        val button = pc.getData.getButton
        ButtonComponentData(button.getName, button.getText, ConversionUtils.bytesToUuid(button.getAction))

      case DataCase.BLOG =>
        val blog = pc.getData.getBlog
        BlogComponentData(blog.getName, blog.getRss, blog.getTags)

      case DataCase.DATA_NOT_SET =>
        throw new RuntimeException("Expected data")
    },
    if (pc.hasPosition) Some(PageComponentPosition(x = pc.getPosition.getX, y = pc.getPosition.getY)) else None,
    ConversionUtils.longToInstance(pc.getDateCreated),
    ConversionUtils.longToInstance(pc.getDateUpdated)
  )

  private def toEntryPointPb(entryPoint: EntryPoint): EntryPointPb = entryPoint match {
    case ep: DomainEntryPoint =>
      EntryPointPb.newBuilder()
        .setDomain(
          DomainEntryPointPb.newBuilder()
            .setPrimary(ep.primary)
            .setDomain(ep.domain)
        )
        .build()

    case ep: FreeEntryPoint =>
      EntryPointPb.newBuilder()
        .setFree(
          FreeEntryPointPb.newBuilder()
            .setPrimary(ep.primary)
            .setUserName(ep.userName)
            .setSiteName(ep.siteName)
        )
        .build()
  }

  private def fromEntryPointPb(entryPoint: EntryPointPb): EntryPoint = entryPoint.getEpCase match {
    case EpCase.DOMAIN =>
      val ep = entryPoint.getDomain
      DomainEntryPoint(ep.getDomain, ep.getPrimary)

    case EpCase.FREE =>
      val ep = entryPoint.getFree
      FreeEntryPoint(ep.getUserName, ep.getSiteName, ep.getPrimary)

    case EpCase.EP_NOT_SET =>
      throw new RuntimeException("Expected entry point")
  }

}
