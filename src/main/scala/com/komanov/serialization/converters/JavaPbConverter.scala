package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.protos.Events.SiteEventPb.EvCase
import com.komanov.serialization.domain.protos.Events._
import com.komanov.serialization.domain.protos.Site.EntryPointPb._
import com.komanov.serialization.domain.protos.Site.PageComponentDataPb._
import com.komanov.serialization.domain.protos.Site._

import scala.collection.JavaConversions._

/** https://developers.google.com/protocol-buffers */
object JavaPbConverter extends SiteConverter with EventConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = SitePb.newBuilder()
      .setId(ConversionUtils.uuidToBytes(site.id))
      .setOwnerId(ConversionUtils.uuidToBytes(site.ownerId))
      .setRevision(site.revision)
      .setSiteType(toSiteTypePb(site.siteType))
      .addAllFlags(site.flags.map(toSiteFlagPb))
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
      fromSiteTypePb(site.getSiteType),
      site.getFlagsList.map(fromSiteFlagPb),
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

  override def toByteArray(event: SiteEventData): Array[Byte] = {
    val evPb: SiteEventPb.Builder = event.event match {
      case e: SiteCreated =>
        SiteEventPb.newBuilder().setSiteCreatedPb(SiteCreatedPb.newBuilder()
            .setId(ConversionUtils.uuidToBytes(e.id))
            .setOwnerId(ConversionUtils.uuidToBytes(e.ownerId))
            .setSiteType(toSiteTypePb(e.siteType))
        )
      case e: SiteNameSet =>
        SiteEventPb.newBuilder().setSiteNameSetPb(SiteNameSetPb.newBuilder()
            .setName(e.name)
        )
      case e: SiteDescriptionSet =>
        SiteEventPb.newBuilder().setSiteDescriptionSetPb(SiteDescriptionSetPb.newBuilder()
            .setDescription(e.description)
        )
      case e: SiteRevisionSet =>
        SiteEventPb.newBuilder().setSiteRevisionSetPb(SiteRevisionSetPb.newBuilder()
            .setRevision(e.revision)
        )
      case e: SitePublished =>
        SiteEventPb.newBuilder().setSitePublishedPb(SitePublishedPb.newBuilder())
      case e: SiteUnpublished =>
        SiteEventPb.newBuilder().setSiteUnpublishedPb(SiteUnpublishedPb.newBuilder())
      case e: SiteFlagAdded =>
        SiteEventPb.newBuilder().setSiteFlagAddedPb(SiteFlagAddedPb.newBuilder()
            .setSiteFlag(toSiteFlagPb(e.siteFlag))
        )
      case e: SiteFlagRemoved =>
        SiteEventPb.newBuilder().setSiteFlagRemovedPb(SiteFlagRemovedPb.newBuilder()
          .setSiteFlag(toSiteFlagPb(e.siteFlag))
        )
      case e: DomainAdded =>
        SiteEventPb.newBuilder().setDomainAddedPb(DomainAddedPb.newBuilder()
            .setName(e.name)
        )
      case e: DomainRemoved =>
        SiteEventPb.newBuilder().setDomainRemovedPb(DomainRemovedPb.newBuilder()
          .setName(e.name)
        )
      case e: PrimaryDomainSet =>
        SiteEventPb.newBuilder().setPrimaryDomainSetPb(PrimaryDomainSetPb.newBuilder()
          .setName(e.name)
        )
      case e: DefaultMetaTagAdded =>
        SiteEventPb.newBuilder().setDefaultMetaTagAddedPb(DefaultMetaTagAddedPb.newBuilder()
            .setName(e.name)
            .setValue(e.value)
        )
      case e: DefaultMetaTagRemoved =>
        SiteEventPb.newBuilder().setDefaultMetaTagRemovedPb(DefaultMetaTagRemovedPb.newBuilder()
          .setName(e.name)
        )
      case e: PageAdded =>
        SiteEventPb.newBuilder().setPageAddedPb(PageAddedPb.newBuilder()
            .setPath(e.path)
        )
      case e: PageRemoved =>
        SiteEventPb.newBuilder().setPageRemovedPb(PageRemovedPb.newBuilder()
          .setPath(e.path)
        )
      case e: PageNameSet =>
        SiteEventPb.newBuilder().setPageNameSetPb(PageNameSetPb.newBuilder()
          .setPath(e.path)
          .setName(e.name)
        )
      case e: PageMetaTagAdded =>
        SiteEventPb.newBuilder().setPageMetaTagAddedPb(PageMetaTagAddedPb.newBuilder()
          .setPath(e.path)
          .setName(e.name)
          .setValue(e.value)
        )
      case e: PageMetaTagRemoved =>
        SiteEventPb.newBuilder().setPageMetaTagRemovedPb(PageMetaTagRemovedPb.newBuilder()
          .setPath(e.path)
          .setName(e.name)
        )
      case e: PageComponentAdded =>
        SiteEventPb.newBuilder().setPageComponentAddedPb(PageComponentAddedPb.newBuilder()
          .setPagePath(e.pagePath)
          .setId(ConversionUtils.uuidToBytes(e.id))
          .setComponentType(toPageComponentTypePb(e.componentType))
        )
      case e: PageComponentRemoved =>
        SiteEventPb.newBuilder().setPageComponentRemovedPb(PageComponentRemovedPb.newBuilder()
          .setPagePath(e.pagePath)
          .setId(ConversionUtils.uuidToBytes(e.id))
        )
      case e: PageComponentPositionSet =>
        SiteEventPb.newBuilder().setPageComponentPositionSetPb(PageComponentPositionSetPb.newBuilder()
          .setId(ConversionUtils.uuidToBytes(e.id))
          .setX(e.position.x)
          .setY(e.position.y)
        )
      case e: PageComponentPositionReset =>
        SiteEventPb.newBuilder().setPageComponentPositionResetPb(PageComponentPositionResetPb.newBuilder()
          .setId(ConversionUtils.uuidToBytes(e.id))
        )
      case e: TextComponentDataSet =>
        SiteEventPb.newBuilder().setTextComponentDataSetPb(TextComponentDataSetPb.newBuilder()
          .setId(ConversionUtils.uuidToBytes(e.id))
          .setText(e.text)
        )
      case e: ButtonComponentDataSet =>
        SiteEventPb.newBuilder().setButtonComponentDataSetPb(ButtonComponentDataSetPb.newBuilder()
          .setId(ConversionUtils.uuidToBytes(e.id))
          .setName(e.name)
          .setText(e.text)
          .setAction(ConversionUtils.uuidToBytes(e.action))
        )
      case e: BlogComponentDataSet =>
        SiteEventPb.newBuilder().setBlogComponentDataSetPb(BlogComponentDataSetPb.newBuilder()
          .setId(ConversionUtils.uuidToBytes(e.id))
            .setName(e.name)
            .setRss(e.rss)
            .setTags(e.tags)
       )
      case e: DomainEntryPointAdded =>
        SiteEventPb.newBuilder().setDomainEntryPointAddedPb(DomainEntryPointAddedPb.newBuilder()
            .setDomain(e.domain)
        )
      case e: FreeEntryPointAdded =>
        SiteEventPb.newBuilder().setFreeEntryPointAddedPb(FreeEntryPointAddedPb.newBuilder()
            .setUserName(e.userName)
            .setSiteName(e.siteName)
        )
      case e: EntryPointRemoved =>
        SiteEventPb.newBuilder().setEntryPointRemovedPb(EntryPointRemovedPb.newBuilder()
          .setLookupKey(e.lookupKey)
        )
      case e: PrimaryEntryPointSet =>
        SiteEventPb.newBuilder().setPrimaryEntryPointSetPb(PrimaryEntryPointSetPb.newBuilder()
          .setLookupKey(e.lookupKey)
        )
    }

    SiteEventDataPb.newBuilder()
      .setId(ConversionUtils.uuidToBytes(event.id))
      .setTimestamp(ConversionUtils.instantToLong(event.timestamp))
      .setEv(evPb)
      .build()
      .toByteArray
  }

  override def eventFromByteArray(bytes: Array[Byte]): SiteEventData = {
    val proto = SiteEventDataPb.parseFrom(bytes)
    val siteEvent: SiteEvent = proto.getEv.getEvCase match {
      case EvCase.SITECREATEDPB =>
        val ev = proto.getEv.getSiteCreatedPb
        SiteCreated(ConversionUtils.bytesToUuid(ev.getId), ConversionUtils.bytesToUuid(ev.getOwnerId), fromSiteTypePb(ev.getSiteType))
      case EvCase.SITENAMESETPB =>
        val ev = proto.getEv.getSiteNameSetPb
        SiteNameSet(ev.getName)
      case EvCase.SITEDESCRIPTIONSETPB =>
        val ev = proto.getEv.getSiteDescriptionSetPb
        SiteDescriptionSet(ev.getDescription)
      case EvCase.SITEREVISIONSETPB =>
        val ev = proto.getEv.getSiteRevisionSetPb
        SiteRevisionSet(ev.getRevision)
      case EvCase.SITEPUBLISHEDPB =>
        val ev = proto.getEv.getSitePublishedPb
        SitePublished()
      case EvCase.SITEUNPUBLISHEDPB =>
        val ev = proto.getEv.getSiteUnpublishedPb
        SiteUnpublished()
      case EvCase.SITEFLAGADDEDPB =>
        val ev = proto.getEv.getSiteFlagAddedPb
        SiteFlagAdded(fromSiteFlagPb(ev.getSiteFlag))
      case EvCase.SITEFLAGREMOVEDPB =>
        val ev = proto.getEv.getSiteFlagRemovedPb
        SiteFlagRemoved(fromSiteFlagPb(ev.getSiteFlag))
      case EvCase.DOMAINADDEDPB =>
        val ev = proto.getEv.getDomainAddedPb
        DomainAdded(ev.getName)
      case EvCase.DOMAINREMOVEDPB =>
        val ev = proto.getEv.getDomainRemovedPb
        DomainRemoved(ev.getName)
      case EvCase.PRIMARYDOMAINSETPB =>
        val ev = proto.getEv.getPrimaryDomainSetPb
        PrimaryDomainSet(ev.getName)
      case EvCase.DEFAULTMETATAGADDEDPB =>
        val ev = proto.getEv.getDefaultMetaTagAddedPb
        DefaultMetaTagAdded(ev.getName, ev.getValue)
      case EvCase.DEFAULTMETATAGREMOVEDPB =>
        val ev = proto.getEv.getDefaultMetaTagRemovedPb
        DefaultMetaTagRemoved(ev.getName)
      case EvCase.PAGEADDEDPB =>
        val ev = proto.getEv.getPageAddedPb
        PageAdded(ev.getPath)
      case EvCase.PAGEREMOVEDPB =>
        val ev = proto.getEv.getPageRemovedPb
        PageRemoved(ev.getPath)
      case EvCase.PAGENAMESETPB =>
        val ev = proto.getEv.getPageNameSetPb
        PageNameSet(ev.getPath, ev.getName)
      case EvCase.PAGEMETATAGADDEDPB =>
        val ev = proto.getEv.getPageMetaTagAddedPb
        PageMetaTagAdded(ev.getPath, ev.getName, ev.getValue)
      case EvCase.PAGEMETATAGREMOVEDPB =>
        val ev = proto.getEv.getPageMetaTagRemovedPb
        PageMetaTagRemoved(ev.getPath, ev.getName)
      case EvCase.PAGECOMPONENTADDEDPB =>
        val ev = proto.getEv.getPageComponentAddedPb
        PageComponentAdded(ev.getPagePath, ConversionUtils.bytesToUuid(ev.getId), fromPageComponentTypePb(ev.getComponentType))
      case EvCase.PAGECOMPONENTREMOVEDPB =>
        val ev = proto.getEv.getPageComponentRemovedPb
        PageComponentRemoved(ev.getPagePath, ConversionUtils.bytesToUuid(ev.getId))
      case EvCase.PAGECOMPONENTPOSITIONSETPB =>
        val ev = proto.getEv.getPageComponentPositionSetPb
        PageComponentPositionSet(ConversionUtils.bytesToUuid(ev.getId), PageComponentPosition(ev.getX, ev.getY))
      case EvCase.PAGECOMPONENTPOSITIONRESETPB =>
        val ev = proto.getEv.getPageComponentPositionResetPb
        PageComponentPositionReset(ConversionUtils.bytesToUuid(ev.getId))
      case EvCase.TEXTCOMPONENTDATASETPB =>
        val ev = proto.getEv.getTextComponentDataSetPb
        TextComponentDataSet(ConversionUtils.bytesToUuid(ev.getId), ev.getText)
      case EvCase.BUTTONCOMPONENTDATASETPB =>
        val ev = proto.getEv.getButtonComponentDataSetPb
        ButtonComponentDataSet(ConversionUtils.bytesToUuid(ev.getId), ev.getName, ev.getText, ConversionUtils.bytesToUuid(ev.getAction))
      case EvCase.BLOGCOMPONENTDATASETPB =>
        val ev = proto.getEv.getBlogComponentDataSetPb
        BlogComponentDataSet(ConversionUtils.bytesToUuid(ev.getId), ev.getName, ev.getRss, ev.getTags)
      case EvCase.DOMAINENTRYPOINTADDEDPB =>
        val ev = proto.getEv.getDomainEntryPointAddedPb
        DomainEntryPointAdded(ev.getDomain)
      case EvCase.FREEENTRYPOINTADDEDPB =>
        val ev = proto.getEv.getFreeEntryPointAddedPb
        FreeEntryPointAdded(ev.getUserName, ev.getSiteName)
      case EvCase.ENTRYPOINTREMOVEDPB =>
        val ev = proto.getEv.getEntryPointRemovedPb
        EntryPointRemoved(ev.getLookupKey)
      case EvCase.PRIMARYENTRYPOINTSETPB =>
        val ev = proto.getEv.getPrimaryEntryPointSetPb
        PrimaryEntryPointSet(ev.getLookupKey)
      case EvCase.EV_NOT_SET =>
        throw new IllegalStateException("Unknown event type")
    }

    SiteEventData(
      ConversionUtils.bytesToUuid(proto.getId),
      siteEvent,
      ConversionUtils.longToInstance(proto.getTimestamp)
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
      .setComponentType(toPageComponentTypePb(pc.componentType))
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
    fromPageComponentTypePb(pc.getComponentType),
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
    case SiteTypePb.UnknownSiteType | SiteTypePb.UNRECOGNIZED => SiteType.Unknown
  }

  private def toSiteFlagPb(f: SiteFlag): SiteFlagPb = f match {
    case SiteFlag.Free => SiteFlagPb.Free
    case SiteFlag.Premium => SiteFlagPb.Premium
    case SiteFlag.Unknown => SiteFlagPb.UnknownSiteFlag
  }

  private def fromSiteFlagPb(f: SiteFlagPb): SiteFlag = f match {
    case SiteFlagPb.Free => SiteFlag.Free
    case SiteFlagPb.Premium => SiteFlag.Premium
    case SiteFlagPb.UnknownSiteFlag | SiteFlagPb.UNRECOGNIZED => SiteFlag.Unknown
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
    case PageComponentTypePb.UnknownPageComponentType | PageComponentTypePb.UNRECOGNIZED => PageComponentType.Unknown
  }
}
