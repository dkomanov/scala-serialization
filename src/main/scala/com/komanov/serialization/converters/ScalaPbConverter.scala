package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import com.komanov.serialization.domain.protos.events.SiteEventPb.Ev
import com.komanov.serialization.domain.protos.events._
import com.komanov.serialization.domain.protos.site.EntryPointPb.{DomainEntryPointPb, FreeEntryPointPb}
import com.komanov.serialization.domain.protos.site.PageComponentDataPb._
import com.komanov.serialization.domain.protos.site._
import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import scala.language.existentials
import scala.reflect.ClassTag

/** https://github.com/trueaccord/ScalaPB */
object ScalaPbConverter extends MyConverter {

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

  override def toByteArray(event: SiteEvent): Array[Byte] = {
    val (generator: GeneratorType, _, _, _) = toEventMap(event.getClass)

    generator(event).toByteArray
  }

  override def siteEventFromByteArray(clazz: Class[_], bytes: Array[Byte]): SiteEvent = {
    val (_, _, messageClass, companion) = toEventMap(clazz)
    val message = companion.asInstanceOf[GeneratedMessageCompanion[_]].parseFrom(bytes)
    val extractor = fromEventMap(messageClass)
    extractor(message.asInstanceOf[GeneratedMessage])
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

  type GeneratorType = SiteEvent => GeneratedMessage
  type SetterType = GeneratedMessage => SiteEventPb.Ev
  type ExtractorType = GeneratedMessage => SiteEvent
  type UnwrapperType = SiteEventPb.Ev => GeneratedMessage
  type Companion = GeneratedMessageCompanion[SiteEventPb]

  case class BigTuple(siteEventClass: Class[_],
                      generator: GeneratorType,
                      setter: SetterType,
                      messageClass: Class[_],
                      evClass: Class[_],
                      companion: Companion,
                      extractor: ExtractorType,
                      unwrapper: UnwrapperType)

  private def createEventMapTuple[T: ClassTag, M: ClassTag, Ev <: SiteEventPb.Ev : ClassTag](generator: T => M,
                                                                                           setter: M => Ev,
                                                                                           extractor: M => T,
                                                                                           unwrapper: Ev => M): BigTuple = {

    val messageClass = implicitly[ClassTag[M]].runtimeClass
    BigTuple(
      implicitly[ClassTag[T]].runtimeClass,
      generator.asInstanceOf[GeneratorType],
      setter.asInstanceOf[SetterType],
      messageClass,
      implicitly[ClassTag[Ev]].runtimeClass,
      getCompanionObject(messageClass).asInstanceOf[Companion],
      extractor.asInstanceOf[ExtractorType],
      unwrapper.asInstanceOf[UnwrapperType]
    )
  }

  private val eventHandlers = Seq[BigTuple](
    createEventMapTuple[SiteCreated, SiteCreatedPb, Ev.SiteCreatedPb](
      e => SiteCreatedPb(ConversionUtils.uuidToBytes(e.id), ConversionUtils.uuidToBytes(e.ownerId), toSiteTypePb(e.siteType)),
      m => Ev.SiteCreatedPb(m),
      e => SiteCreated(ConversionUtils.bytesToUuid(e.id), ConversionUtils.bytesToUuid(e.ownerId), fromSiteTypePb(e.siteType)),
      _.siteCreatedPb.get
    ),
    createEventMapTuple[SiteNameSet, SiteNameSetPb, Ev.SiteNameSetPb](
      e => SiteNameSetPb(e.name),
      m => Ev.SiteNameSetPb(m),
      e => SiteNameSet(e.name),
      _.siteNameSetPb.get
    ),
    createEventMapTuple[SiteDescriptionSet, SiteDescriptionSetPb, Ev.SiteDescriptionSetPb](
      e => SiteDescriptionSetPb(e.description),
      m => Ev.SiteDescriptionSetPb(m),
      e => SiteDescriptionSet(e.description),
      _.siteDescriptionSetPb.get
    ),
    createEventMapTuple[SiteRevisionSet, SiteRevisionSetPb, Ev.SiteRevisionSetPb](
      e => SiteRevisionSetPb(e.revision),
      m => Ev.SiteRevisionSetPb(m),
      e => SiteRevisionSet(e.revision),
      _.siteRevisionSetPb.get
    ),
    createEventMapTuple[SitePublished, SitePublishedPb, Ev.SitePublishedPb](
      e => SitePublishedPb(),
      m => Ev.SitePublishedPb(m),
      e => SitePublished(),
      _.sitePublishedPb.get
    ),
    createEventMapTuple[SiteUnpublished, SiteUnpublishedPb, Ev.SiteUnpublishedPb](
      e => SiteUnpublishedPb(),
      m => Ev.SiteUnpublishedPb(m),
      e => SiteUnpublished(),
      _.siteUnpublishedPb.get
    ),
    createEventMapTuple[SiteFlagAdded, SiteFlagAddedPb, Ev.SiteFlagAddedPb](
      e => SiteFlagAddedPb(toSiteFlagPb(e.siteFlag)),
      m => Ev.SiteFlagAddedPb(m),
      e => SiteFlagAdded(fromSiteFlagPb(e.siteFlag)),
      _.siteFlagAddedPb.get
    ),
    createEventMapTuple[SiteFlagRemoved, SiteFlagRemovedPb, Ev.SiteFlagRemovedPb](
      e => SiteFlagRemovedPb(toSiteFlagPb(e.siteFlag)),
      m => Ev.SiteFlagRemovedPb(m),
      e => SiteFlagRemoved(fromSiteFlagPb(e.siteFlag)),
      _.siteFlagRemovedPb.get
    ),
    createEventMapTuple[DomainAdded, DomainAddedPb, Ev.DomainAddedPb](
      e => DomainAddedPb(e.name),
      m => Ev.DomainAddedPb(m),
      e => DomainAdded(e.name),
      _.domainAddedPb.get
    ),
    createEventMapTuple[DomainRemoved, DomainRemovedPb, Ev.DomainRemovedPb](
      e => DomainRemovedPb(e.name),
      m => Ev.DomainRemovedPb(m),
      e => DomainRemoved(e.name),
      _.domainRemovedPb.get
    ),
    createEventMapTuple[PrimaryDomainSet, PrimaryDomainSetPb, Ev.PrimaryDomainSetPb](
      e => PrimaryDomainSetPb(e.name),
      m => Ev.PrimaryDomainSetPb(m),
      e => PrimaryDomainSet(e.name),
      _.primaryDomainSetPb.get
    ),
    createEventMapTuple[DefaultMetaTagAdded, DefaultMetaTagAddedPb, Ev.DefaultMetaTagAddedPb](
      e => DefaultMetaTagAddedPb(e.name, e.value),
      m => Ev.DefaultMetaTagAddedPb(m),
      e => DefaultMetaTagAdded(e.name, e.value),
      _.defaultMetaTagAddedPb.get
    ),
    createEventMapTuple[DefaultMetaTagRemoved, DefaultMetaTagRemovedPb, Ev.DefaultMetaTagRemovedPb](
      e => DefaultMetaTagRemovedPb(e.name),
      m => Ev.DefaultMetaTagRemovedPb(m),
      e => DefaultMetaTagRemoved(e.name),
      _.defaultMetaTagRemovedPb.get
    ),
    createEventMapTuple[PageAdded, PageAddedPb, Ev.PageAddedPb](
      e => PageAddedPb(e.path),
      m => Ev.PageAddedPb(m),
      e => PageAdded(e.path),
      _.pageAddedPb.get
    ),
    createEventMapTuple[PageRemoved, PageRemovedPb, Ev.PageRemovedPb](
      e => PageRemovedPb(e.path),
      m => Ev.PageRemovedPb(m),
      e => PageRemoved(e.path),
      _.pageRemovedPb.get
    ),
    createEventMapTuple[PageNameSet, PageNameSetPb, Ev.PageNameSetPb](
      e => PageNameSetPb(e.path, e.name),
      m => Ev.PageNameSetPb(m),
      e => PageNameSet(e.path, e.name),
      _.pageNameSetPb.get
    ),
    createEventMapTuple[PageMetaTagAdded, PageMetaTagAddedPb, Ev.PageMetaTagAddedPb](
      e => PageMetaTagAddedPb(e.path, e.name, e.value),
      m => Ev.PageMetaTagAddedPb(m),
      e => PageMetaTagAdded(e.path, e.name, e.value),
      _.pageMetaTagAddedPb.get
    ),
    createEventMapTuple[PageMetaTagRemoved, PageMetaTagRemovedPb, Ev.PageMetaTagRemovedPb](
      e => PageMetaTagRemovedPb(e.path, e.name),
      m => Ev.PageMetaTagRemovedPb(m),
      e => PageMetaTagRemoved(e.path, e.name),
      _.pageMetaTagRemovedPb.get
    ),
    createEventMapTuple[PageComponentAdded, PageComponentAddedPb, Ev.PageComponentAddedPb](
      e => PageComponentAddedPb(e.pagePath, ConversionUtils.uuidToBytes(e.id), toPageComponentTypePb(e.componentType)),
      m => Ev.PageComponentAddedPb(m),
      e => PageComponentAdded(e.pagePath, ConversionUtils.bytesToUuid(e.id), fromPageComponentTypePb(e.componentType)),
      _.pageComponentAddedPb.get
    ),
    createEventMapTuple[PageComponentRemoved, PageComponentRemovedPb, Ev.PageComponentRemovedPb](
      e => PageComponentRemovedPb(e.pagePath, ConversionUtils.uuidToBytes(e.id)),
      m => Ev.PageComponentRemovedPb(m),
      e => PageComponentRemoved(e.pagePath, ConversionUtils.bytesToUuid(e.id)),
      _.pageComponentRemovedPb.get
    ),
    createEventMapTuple[PageComponentPositionSet, PageComponentPositionSetPb, Ev.PageComponentPositionSetPb](
      e => PageComponentPositionSetPb(ConversionUtils.uuidToBytes(e.id), e.position.x, e.position.y),
      m => Ev.PageComponentPositionSetPb(m),
      e => PageComponentPositionSet(ConversionUtils.bytesToUuid(e.id), PageComponentPosition(e.x, e.y)),
      _.pageComponentPositionSetPb.get
    ),
    createEventMapTuple[PageComponentPositionReset, PageComponentPositionResetPb, Ev.PageComponentPositionResetPb](
      e => PageComponentPositionResetPb(ConversionUtils.uuidToBytes(e.id)),
      m => Ev.PageComponentPositionResetPb(m),
      e => PageComponentPositionReset(ConversionUtils.bytesToUuid(e.id)),
      _.pageComponentPositionResetPb.get
    ),
    createEventMapTuple[TextComponentDataSet, TextComponentDataSetPb, Ev.TextComponentDataSetPb](
      e => TextComponentDataSetPb(ConversionUtils.uuidToBytes(e.id), e.text),
      m => Ev.TextComponentDataSetPb(m),
      e => TextComponentDataSet(ConversionUtils.bytesToUuid(e.id), e.text),
      _.textComponentDataSetPb.get
    ),
    createEventMapTuple[ButtonComponentDataSet, ButtonComponentDataSetPb, Ev.ButtonComponentDataSetPb](
      e => ButtonComponentDataSetPb(ConversionUtils.uuidToBytes(e.id), e.name, e.text, ConversionUtils.uuidToBytes(e.action)),
      m => Ev.ButtonComponentDataSetPb(m),
      e => ButtonComponentDataSet(ConversionUtils.bytesToUuid(e.id), e.name, e.text, ConversionUtils.bytesToUuid(e.action)),
      _.buttonComponentDataSetPb.get
    ),
    createEventMapTuple[BlogComponentDataSet, BlogComponentDataSetPb, Ev.BlogComponentDataSetPb](
      e => BlogComponentDataSetPb(ConversionUtils.uuidToBytes(e.id), e.name, e.rss, e.tags),
      m => Ev.BlogComponentDataSetPb(m),
      e => BlogComponentDataSet(ConversionUtils.bytesToUuid(e.id), e.name, e.rss, e.tags),
      _.blogComponentDataSetPb.get
    ),
    createEventMapTuple[DomainEntryPointAdded, DomainEntryPointAddedPb, Ev.DomainEntryPointAddedPb](
      e => DomainEntryPointAddedPb(e.domain),
      m => Ev.DomainEntryPointAddedPb(m),
      e => DomainEntryPointAdded(e.domain),
      _.domainEntryPointAddedPb.get
    ),
    createEventMapTuple[FreeEntryPointAdded, FreeEntryPointAddedPb, Ev.FreeEntryPointAddedPb](
      e => FreeEntryPointAddedPb(e.userName, e.siteName),
      m => Ev.FreeEntryPointAddedPb(m),
      e => FreeEntryPointAdded(e.userName, e.siteName),
      _.freeEntryPointAddedPb.get
    ),
    createEventMapTuple[EntryPointRemoved, EntryPointRemovedPb, Ev.EntryPointRemovedPb](
      e => EntryPointRemovedPb(e.lookupKey),
      m => Ev.EntryPointRemovedPb(m),
      e => EntryPointRemoved(e.lookupKey),
      _.entryPointRemovedPb.get
    ),
    createEventMapTuple[PrimaryEntryPointSet, PrimaryEntryPointSetPb, Ev.PrimaryEntryPointSetPb](
      e => PrimaryEntryPointSetPb(e.lookupKey),
      m => Ev.PrimaryEntryPointSetPb(m),
      e => PrimaryEntryPointSet(e.lookupKey),
      _.primaryEntryPointSetPb.get
    )
  )

  private val toEventMap: Map[Class[_], (GeneratorType, SetterType, Class[_], Companion)] = Map(eventHandlers.map(t => t.siteEventClass ->(t.generator, t.setter, t.messageClass, t.companion)): _*)
  private val fromEventMap: Map[Class[_], ExtractorType] = Map(eventHandlers.map(t => t.messageClass -> t.extractor): _*)
  private val unwrapperByEvClass: Map[Class[_], UnwrapperType] = Map(eventHandlers.map(t => t.evClass -> t.unwrapper): _*)

  private def toEventPb(event: SiteEvent): SiteEventPb.Ev = {
    val (generator, setter, _, _) = toEventMap(event.getClass)
    setter(generator(event))
  }

  private def getCompanionObject(clazz: Class[_]): Any = {
    import scala.reflect.runtime.{currentMirror => cm}
    val classSymbol = cm.classSymbol(clazz)
    val moduleSymbol = classSymbol.companion.asModule
    val moduleMirror = cm.reflectModule(moduleSymbol)
    moduleMirror.instance
  }
}
