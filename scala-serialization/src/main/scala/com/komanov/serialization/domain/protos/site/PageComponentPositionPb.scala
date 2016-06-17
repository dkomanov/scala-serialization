// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.komanov.serialization.domain.protos.site



@SerialVersionUID(0L)
final case class PageComponentPositionPb(
    x: Int = 0,
    y: Int = 0
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[PageComponentPositionPb] with com.trueaccord.lenses.Updatable[PageComponentPositionPb] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (x != 0) { __size += com.google.protobuf.CodedOutputStream.computeUInt32Size(1, x) }
      if (y != 0) { __size += com.google.protobuf.CodedOutputStream.computeUInt32Size(2, y) }
      __size
    }
    final override def serializedSize: Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(output: com.google.protobuf.CodedOutputStream): Unit = {
      {
        val __v = x
        if (__v != 0) {
          output.writeUInt32(1, __v)
        }
      };
      {
        val __v = y
        if (__v != 0) {
          output.writeUInt32(2, __v)
        }
      };
    }
    def mergeFrom(__input: com.google.protobuf.CodedInputStream): com.komanov.serialization.domain.protos.site.PageComponentPositionPb = {
      var __x = this.x
      var __y = this.y
      var _done__ = false
      while (!_done__) {
        val _tag__ = __input.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 8 =>
            __x = __input.readUInt32()
          case 16 =>
            __y = __input.readUInt32()
          case tag => __input.skipField(tag)
        }
      }
      com.komanov.serialization.domain.protos.site.PageComponentPositionPb(
          x = __x,
          y = __y
      )
    }
    def withX(__v: Int): PageComponentPositionPb = copy(x = __v)
    def withY(__v: Int): PageComponentPositionPb = copy(y = __v)
    def getField(__field: com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = {
      __field.getNumber match {
        case 1 => {
          val __t = x
          if (__t != 0) __t else null
        }
        case 2 => {
          val __t = y
          if (__t != 0) __t else null
        }
      }
    }
    override def toString: String = com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.komanov.serialization.domain.protos.site.PageComponentPositionPb
}

object PageComponentPositionPb extends com.trueaccord.scalapb.GeneratedMessageCompanion[PageComponentPositionPb] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[PageComponentPositionPb] = this
  def fromFieldsMap(__fieldsMap: Map[com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.komanov.serialization.domain.protos.site.PageComponentPositionPb = {
    require(__fieldsMap.keys.forall(_.getContainingType() == descriptor), "FieldDescriptor does not match message type.")
    val __fields = descriptor.getFields
    com.komanov.serialization.domain.protos.site.PageComponentPositionPb(
      __fieldsMap.getOrElse(__fields.get(0), 0).asInstanceOf[Int],
      __fieldsMap.getOrElse(__fields.get(1), 0).asInstanceOf[Int]
    )
  }
  def descriptor: com.google.protobuf.Descriptors.Descriptor = SiteProto.descriptor.getMessageTypes.get(4)
  def messageCompanionForField(__field: com.google.protobuf.Descriptors.FieldDescriptor): com.trueaccord.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__field)
  def enumCompanionForField(__field: com.google.protobuf.Descriptors.FieldDescriptor): com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__field)
  lazy val defaultInstance = com.komanov.serialization.domain.protos.site.PageComponentPositionPb(
  )
  implicit class PageComponentPositionPbLens[UpperPB](_l: com.trueaccord.lenses.Lens[UpperPB, PageComponentPositionPb]) extends com.trueaccord.lenses.ObjectLens[UpperPB, PageComponentPositionPb](_l) {
    def x: com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.x)((c_, f_) => c_.copy(x = f_))
    def y: com.trueaccord.lenses.Lens[UpperPB, Int] = field(_.y)((c_, f_) => c_.copy(y = f_))
  }
  final val X_FIELD_NUMBER = 1
  final val Y_FIELD_NUMBER = 2
}
