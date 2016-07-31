package com.komanov.serialization.converters

import com.komanov.serialization.domain._
import ms.webmaster.macroserialization.MsgPack

/** https://bitbucket.org/jaroslav/scala-macro-serialization/src */
object MsgpackMacrosConverter extends MyConverter {

  implicit object xxa extends MsgPack.AuxPacker[java.util.UUID] {
    def packToOutputStream(os: java.io.OutputStream, t: java.util.UUID): Unit = MsgPack.packToOutputStream(os, t.toString)
  }
  implicit object xxb extends MsgPack.AuxUnpacker[java.util.UUID] {
    def unpackFromDataInputStream(in: java.io.DataInputStream): java.util.UUID = java.util.UUID.fromString(MsgPack.unpackFromDataInputStream[String](in))
  }

  /* generic code for all java.lang.Enum */
  implicit def xxc[T <: java.lang.Enum[T]]: MsgPack.AuxPacker[T] = new MsgPack.AuxPacker[T] {
    def packToOutputStream(os: java.io.OutputStream, t: T): Unit = MsgPack.packToOutputStream(os, t.name)
  }
  implicit def xxd[T <: java.lang.Enum[T] : reflect.ClassTag]: MsgPack.AuxUnpacker[T] = new MsgPack.AuxUnpacker[T] {
    def unpackFromDataInputStream(in: java.io.DataInputStream): T = java.lang.Enum.valueOf(reflect.classTag[T].runtimeClass.asInstanceOf[Class[T]], MsgPack.unpackFromDataInputStream[String](in))
  }

  implicit object xxe extends MsgPack.AuxPacker[PageComponentData] {
    def packToOutputStream(os: java.io.OutputStream, t: PageComponentData): Unit = t match {
      case t: TextComponentData   => MsgPack.packToOutputStream(os, (1 -> t))
      case t: ButtonComponentData => MsgPack.packToOutputStream(os, (2 -> t))
      case t: BlogComponentData   => MsgPack.packToOutputStream(os, (3 -> t))
    }
  }
  implicit object xxf extends MsgPack.AuxUnpacker[PageComponentData] {
    def unpackFromDataInputStream(in: java.io.DataInputStream): PageComponentData = {
      MsgPack.unpackFromDataInputStream[(Int, MsgPack.Keep)](in) match {
        case (1, keep) => keep.unpack[TextComponentData  ]
        case (2, keep) => keep.unpack[ButtonComponentData]
        case (3, keep) => keep.unpack[BlogComponentData  ]
      }
    }
  }

  implicit object xxg extends MsgPack.AuxPacker[EntryPoint] {
    def packToOutputStream(os: java.io.OutputStream, t: EntryPoint): Unit = t match {
      case t: DomainEntryPoint => MsgPack.packToOutputStream(os, (1 -> t))
      case t: FreeEntryPoint   => MsgPack.packToOutputStream(os, (2 -> t))
    }
  }
  implicit object xxh extends MsgPack.AuxUnpacker[EntryPoint] {
    def unpackFromDataInputStream(in: java.io.DataInputStream): EntryPoint = {
      MsgPack.unpackFromDataInputStream[(Int, MsgPack.Keep)](in) match {
        case (1, keep) => keep.unpack[DomainEntryPoint]
        case (2, keep) => keep.unpack[FreeEntryPoint  ]
      }
    }
  }

  implicit object xxi extends MsgPack.AuxPacker[java.time.Instant] {
    def packToOutputStream(os: java.io.OutputStream, t: java.time.Instant): Unit = MsgPack.packToOutputStream(os, (t.getEpochSecond, t.getNano))
  }
  implicit object xxj extends MsgPack.AuxUnpacker[java.time.Instant] {
    def unpackFromDataInputStream(in: java.io.DataInputStream): java.time.Instant = {
      val (sec, nano) = MsgPack.unpackFromDataInputStream[(Long, Int)](in)
      java.time.Instant.ofEpochSecond(sec, nano)
    }
  }

  implicit object xxk extends MsgPack.AuxPacker[SiteEvent] {
    def packToOutputStream(os: java.io.OutputStream, t: SiteEvent): Unit = t match {
      case t: SiteCreated               => MsgPack.packToOutputStream(os, ( 1 -> t))
      case t: SiteNameSet               => MsgPack.packToOutputStream(os, ( 2 -> t))
      case t: SiteDescriptionSet        => MsgPack.packToOutputStream(os, ( 3 -> t))
      case t: SiteRevisionSet           => MsgPack.packToOutputStream(os, ( 4 -> t))
      case t: SitePublished             => MsgPack.packToOutputStream(os, ( 5 -> t))
      case t: SiteUnpublished           => MsgPack.packToOutputStream(os, ( 6 -> t))
      case t: SiteFlagAdded             => MsgPack.packToOutputStream(os, ( 7 -> t))
      case t: SiteFlagRemoved           => MsgPack.packToOutputStream(os, ( 8 -> t))
      case t: DomainAdded               => MsgPack.packToOutputStream(os, ( 9 -> t))
      case t: DomainRemoved             => MsgPack.packToOutputStream(os, (10 -> t))
      case t: PrimaryDomainSet          => MsgPack.packToOutputStream(os, (11 -> t))
      case t: DefaultMetaTagAdded       => MsgPack.packToOutputStream(os, (12 -> t))
      case t: DefaultMetaTagRemoved     => MsgPack.packToOutputStream(os, (13 -> t))
      case t: PageAdded                 => MsgPack.packToOutputStream(os, (14 -> t))
      case t: PageRemoved               => MsgPack.packToOutputStream(os, (15 -> t))
      case t: PageNameSet               => MsgPack.packToOutputStream(os, (16 -> t))
      case t: PageMetaTagAdded          => MsgPack.packToOutputStream(os, (17 -> t))
      case t: PageMetaTagRemoved        => MsgPack.packToOutputStream(os, (18 -> t))
      case t: PageComponentAdded        => MsgPack.packToOutputStream(os, (19 -> t))
      case t: PageComponentRemoved      => MsgPack.packToOutputStream(os, (20 -> t))
      case t: PageComponentPositionSet  => MsgPack.packToOutputStream(os, (21 -> t))
      case t: PageComponentPositionReset=> MsgPack.packToOutputStream(os, (22 -> t))
      case t: TextComponentDataSet      => MsgPack.packToOutputStream(os, (23 -> t))
      case t: ButtonComponentDataSet    => MsgPack.packToOutputStream(os, (24 -> t))
      case t: BlogComponentDataSet      => MsgPack.packToOutputStream(os, (25 -> t))
      case t: DomainEntryPointAdded     => MsgPack.packToOutputStream(os, (26 -> t))
      case t: FreeEntryPointAdded       => MsgPack.packToOutputStream(os, (27 -> t))
      case t: EntryPointRemoved         => MsgPack.packToOutputStream(os, (28 -> t))
      case t: PrimaryEntryPointSet      => MsgPack.packToOutputStream(os, (29 -> t))
    }
  }
  implicit object xxl extends MsgPack.AuxUnpacker[SiteEvent] {
    def unpackFromDataInputStream(in: java.io.DataInputStream): SiteEvent = {
      MsgPack.unpackFromDataInputStream[(Int, MsgPack.Keep)](in) match {
        case ( 1, keep) => keep.unpack[SiteCreated               ]
        case ( 2, keep) => keep.unpack[SiteNameSet               ]
        case ( 3, keep) => keep.unpack[SiteDescriptionSet        ]
        case ( 4, keep) => keep.unpack[SiteRevisionSet           ]
        case ( 5, keep) => keep.unpack[SitePublished             ]
        case ( 6, keep) => keep.unpack[SiteUnpublished           ]
        case ( 7, keep) => keep.unpack[SiteFlagAdded             ]
        case ( 8, keep) => keep.unpack[SiteFlagRemoved           ]
        case ( 9, keep) => keep.unpack[DomainAdded               ]
        case (10, keep) => keep.unpack[DomainRemoved             ]
        case (11, keep) => keep.unpack[PrimaryDomainSet          ]
        case (12, keep) => keep.unpack[DefaultMetaTagAdded       ]
        case (13, keep) => keep.unpack[DefaultMetaTagRemoved     ]
        case (14, keep) => keep.unpack[PageAdded                 ]
        case (15, keep) => keep.unpack[PageRemoved               ]
        case (16, keep) => keep.unpack[PageNameSet               ]
        case (17, keep) => keep.unpack[PageMetaTagAdded          ]
        case (18, keep) => keep.unpack[PageMetaTagRemoved        ]
        case (19, keep) => keep.unpack[PageComponentAdded        ]
        case (20, keep) => keep.unpack[PageComponentRemoved      ]
        case (21, keep) => keep.unpack[PageComponentPositionSet  ]
        case (22, keep) => keep.unpack[PageComponentPositionReset]
        case (23, keep) => keep.unpack[TextComponentDataSet      ]
        case (24, keep) => keep.unpack[ButtonComponentDataSet    ]
        case (25, keep) => keep.unpack[BlogComponentDataSet      ]
        case (26, keep) => keep.unpack[DomainEntryPointAdded     ]
        case (27, keep) => keep.unpack[FreeEntryPointAdded       ]
        case (28, keep) => keep.unpack[EntryPointRemoved         ]
        case (29, keep) => keep.unpack[PrimaryEntryPointSet      ]
      }
    }
  }


  override def toByteArray(site: Site): Array[Byte] = {
    MsgPack.packToByteArray(site)
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    MsgPack.unpackFromByteArray[Site](bytes)
  }

  override def toByteArray(event: SiteEvent): Array[Byte] = {
    MsgPack.packToByteArray(event)
  }

  override def siteEventFromByteArray(clazz: Class[_], bytes: Array[Byte]): SiteEvent = {
    MsgPack.unpackFromByteArray[SiteEvent](bytes)
  }
}
