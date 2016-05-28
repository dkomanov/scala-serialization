package com.komanov.serialization.converters

import java.nio.ByteBuffer
import java.time.Instant
import java.util
import java.util.{Collections, UUID}

import com.komanov.serialization.domain._
import io.protostuff.runtime.{DefaultIdStrategy, Delegate, RuntimeSchema}
import io.protostuff.{Pipe, WireFormat, _}

import scala.collection.JavaConversions._

object ProtostuffConverter extends SiteConverter {

  // 733
  // 716
  // 704
  private val strategy = {
    val s = new DefaultIdStrategy()
    s.registerDelegate(new UuidDelegate)
    s.registerDelegate(new InstantDelegate)
    s
  }
  private val schema = RuntimeSchema.getSchema(classOf[SitePs], strategy)

  override def toByteArray(site: Site): Array[Byte] = {
    val proto = SitePs(
      site.id,
      site.ownerId,
      site.revision,
      site.siteType,
      new util.ArrayList[SiteFlag](site.flags),
      site.name,
      site.description,
      new util.ArrayList(site.domains.map(d => DomainPs(d.name, d.primary))),
      new util.ArrayList(site.defaultMetaTags.map(t => MetaTagPs(t.name, t.value))),
      new util.ArrayList(site.pages.map(p => PagePs(
        p.name,
        p.path,
        p.metaTags.map(t => MetaTagPs(t.name, t.value)),
        new util.ArrayList(p.components.map(c => PageComponentPs(
          c.id,
          c.componentType,
          c.data match {
            case TextComponentData(text) => TextComponentDataPs(text)
            case ButtonComponentData(name, text, action) => ButtonComponentDataPs(name, text, action)
            case BlogComponentData(name, rss, tags) => BlogComponentDataPs(name, rss, tags)
          },
          c.position.map(pcp => PageComponentPositionPs(pcp.x, pcp.y)).orNull,
          c.dateCreated,
          c.dateUpdated
        )))
      ))),
      new util.ArrayList(site.entryPoints.collect {
        case DomainEntryPoint(domain, primary) => DomainEntryPointPs(domain, primary)
        case FreeEntryPoint(userName, siteName, primary) => FreeEntryPointPs(userName, siteName, primary)
      }),
      site.published,
      site.dateCreated,
      site.dateUpdated
    )
    val buffer = LinkedBuffer.allocate()
    ProtostuffIOUtil.toByteArray(proto, schema, buffer)
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    val proto = schema.newMessage()
    ProtostuffIOUtil.mergeFrom(bytes, proto, schema)
    Site(
      proto.id,
      proto.ownerId,
      proto.revision,
      proto.siteType,
      proto.flags,
      proto.name,
      proto.description,
      Option(proto.domains).getOrElse(Collections.emptyList()).map(d => Domain(d.name, d.primary)),
      Option(proto.defaultMetaTags).getOrElse(Collections.emptyList()).map(t => MetaTag(t.name, t.value)),
      Option(proto.pages).getOrElse(Collections.emptyList()).map(p => Page(
        p.name,
        p.path,
        Option(p.metaTags).getOrElse(Collections.emptyList()).map(t => MetaTag(t.name, t.value)),
        p.components.map(c => PageComponent(
          c.id,
          c.componentType,
          c.data match {
            case TextComponentDataPs(text) => TextComponentData(text)
            case ButtonComponentDataPs(name, text, action) => ButtonComponentData(name, text, action)
            case BlogComponentDataPs(name, rss, tags) => BlogComponentData(name, rss, tags)
          },
          Option(c.position).map(pcp => PageComponentPosition(pcp.x, pcp.y)),
          c.dateCreated,
          c.dateUpdated
        ))
      )),
      proto.entryPoints.collect {
        case DomainEntryPointPs(domain, primary) => DomainEntryPoint(domain, primary)
        case FreeEntryPointPs(userName, siteName, primary) => FreeEntryPoint(userName, siteName, primary)
      },
      proto.published,
      proto.dateCreated,
      proto.dateUpdated
    )
  }

  private case class DomainPs(name: String,
                              primary: Boolean)


  private case class MetaTagPs(name: String,
                               value: String)


  private sealed trait EntryPointPs {
    def lookupKey: String

    def primary: Boolean
  }

  private final case class DomainEntryPointPs(domain: String, primary: Boolean) extends EntryPointPs {
    override def lookupKey: String = domain
  }

  private final case class FreeEntryPointPs(userName: String, siteName: String, primary: Boolean) extends EntryPointPs {
    override def lookupKey: String = s"$userName.wix.com/$siteName"
  }


  private sealed trait PageComponentDataPs

  private final case class TextComponentDataPs(text: String) extends PageComponentDataPs

  private final case class ButtonComponentDataPs(name: String,
                                                 text: String,
                                                 action: UUID) extends PageComponentDataPs

  private final case class BlogComponentDataPs(name: String,
                                               rss: Boolean,
                                               tags: Boolean) extends PageComponentDataPs


  private case class PageComponentPositionPs(x: Int,
                                             y: Int)

  private case class PageComponentPs(id: UUID,
                                     componentType: PageComponentType,
                                     data: PageComponentDataPs,
                                     position: PageComponentPositionPs,
                                     dateCreated: Instant,
                                     dateUpdated: Instant)


  private case class PagePs(name: String,
                            path: String,
                            metaTags: util.List[MetaTagPs],
                            components: util.List[PageComponentPs])


  private case class SitePs(id: UUID,
                            ownerId: UUID,
                            revision: Long,
                            siteType: SiteType,
                            flags: util.List[SiteFlag],
                            name: String,
                            description: String,
                            domains: util.List[DomainPs],
                            defaultMetaTags: util.List[MetaTagPs],
                            pages: util.List[PagePs],
                            entryPoints: util.List[EntryPointPs],
                            published: Boolean,
                            dateCreated: Instant,
                            dateUpdated: Instant)

  class UuidDelegate extends Delegate[UUID] {
    override def getFieldType: WireFormat.FieldType = WireFormat.FieldType.BYTES

    override def typeClass: Class[_] = classOf[UUID]

    override def readFrom(input: Input): UUID = {
      ConversionUtils.bytesToUuid(ByteBuffer.wrap(input.readByteArray))
    }

    override def writeTo(output: Output, number: Int, value: UUID, repeated: Boolean) {
      val bb = ConversionUtils.uuidToByteBuffer(value)
      output.writeByteArray(number, bb.array(), repeated)
    }

    override def transfer(pipe: Pipe, input: Input, output: Output, number: Int, repeated: Boolean) {
      input.transferByteRangeTo(output, false, number, repeated)
    }
  }

  class InstantDelegate extends Delegate[Instant] {
    override def getFieldType: WireFormat.FieldType = WireFormat.FieldType.INT64

    override def typeClass: Class[_] = classOf[Instant]

    override def readFrom(input: Input): Instant = {
      ConversionUtils.longToInstance(input.readInt64())
    }

    override def writeTo(output: Output, number: Int, value: Instant, repeated: Boolean) {
      output.writeInt64(number, ConversionUtils.instantToLong(value), repeated)
    }

    override def transfer(pipe: Pipe, input: Input, output: Output, number: Int, repeated: Boolean) {
      input.transferByteRangeTo(output, false, number, repeated)
    }
  }

}
