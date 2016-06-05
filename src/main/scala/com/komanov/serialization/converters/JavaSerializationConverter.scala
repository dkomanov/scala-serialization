package com.komanov.serialization.converters

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.komanov.serialization.converters.IoUtils.using
import com.komanov.serialization.domain.{Site, SiteEventData}

object JavaSerializationConverter extends MyConverter {

  override def toByteArray(site: Site): Array[Byte] = {
    using(new ByteArrayOutputStream()) { baos =>
      using(new ObjectOutputStream(baos)) { os =>
        os.writeObject(site)
        os.flush()
        baos.toByteArray
      }
    }
  }

  override def fromByteArray(bytes: Array[Byte]): Site = {
    using(new ByteArrayInputStream(bytes)) { bais =>
      using(new ObjectInputStream(bais)) { os =>
        os.readObject().asInstanceOf[Site]
      }
    }
  }

  override def toByteArray(event: SiteEventData): Array[Byte] = {
    using(new ByteArrayOutputStream()) { baos =>
      using(new ObjectOutputStream(baos)) { os =>
        os.writeObject(event)
        os.flush()
        baos.toByteArray
      }
    }
  }

  override def eventFromByteArray(bytes: Array[Byte]): SiteEventData = {
    using(new ByteArrayInputStream(bytes)) { bais =>
      using(new ObjectInputStream(bais)) { os =>
        os.readObject().asInstanceOf[SiteEventData]
      }
    }
  }
}
