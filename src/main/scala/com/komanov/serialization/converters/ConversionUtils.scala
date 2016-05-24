package com.komanov.serialization.converters

import java.nio.ByteBuffer
import java.time.Instant
import java.util.UUID

import com.google.protobuf.ByteString

object ConversionUtils {

  def uuidToBytes(uuid: UUID): ByteString = {
    if (uuid == null) {
      return ByteString.EMPTY
    }

    val buffer = ByteBuffer.allocate(16)
    buffer.putLong(uuid.getMostSignificantBits)
    buffer.putLong(uuid.getLeastSignificantBits)
    buffer.rewind()
    ByteString.copyFrom(buffer)
  }

  def bytesToUuid(bs: ByteString): UUID = {
    if (bs.isEmpty) {
      return null
    }

    require(bs.size() == 16)

    val buffer = bs.asReadOnlyByteBuffer()
    new UUID(buffer.getLong, buffer.getLong)
  }

  def instantToLong(v: Instant) = v.toEpochMilli

  def longToInstance(v: Long) = Instant.ofEpochMilli(v)

}
