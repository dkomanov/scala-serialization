package com.komanov.serialization.converters

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.komanov.serialization.domain.Site

object JavaSerializationConverter extends SiteConverter {

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
    val bais = new ByteArrayInputStream(bytes)
    new ObjectInputStream(bais)
      .readObject()
      .asInstanceOf[Site]
  }

  private def using[T <: AutoCloseable, K](stream: => T)(f: T => K): K = {
    var s = null.asInstanceOf[T]
    try {
      s = stream
      f(s)
    } finally {
      if (s != null) {
        s.close()
      }
    }
  }

}
