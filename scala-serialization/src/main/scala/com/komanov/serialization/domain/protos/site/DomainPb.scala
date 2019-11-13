// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.komanov.serialization.domain.protos.site



@SerialVersionUID(0L)
final case class DomainPb(
    name: String = "",
    primary: Boolean = false
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[DomainPb] with com.trueaccord.lenses.Updatable[DomainPb] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (name != "") { __size += com.google.protobuf.CodedOutputStream.computeStringSize(1, name) }
      if (primary != false) { __size += com.google.protobuf.CodedOutputStream.computeBoolSize(2, primary) }
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
        val __v = name
        if (__v != "") {
          output.writeString(1, __v)
        }
      };
      {
        val __v = primary
        if (__v != false) {
          output.writeBool(2, __v)
        }
      };
    }
    def mergeFrom(__input: com.google.protobuf.CodedInputStream): com.komanov.serialization.domain.protos.site.DomainPb = {
      var __name = this.name
      var __primary = this.primary
      var _done__ = false
      while (!_done__) {
        val _tag__ = __input.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __name = __input.readString()
          case 16 =>
            __primary = __input.readBool()
          case tag => __input.skipField(tag)
        }
      }
      com.komanov.serialization.domain.protos.site.DomainPb(
          name = __name,
          primary = __primary
      )
    }
    def withName(__v: String): DomainPb = copy(name = __v)
    def withPrimary(__v: Boolean): DomainPb = copy(primary = __v)
    def getField(__field: com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = {
      __field.getNumber match {
        case 1 => {
          val __t = name
          if (__t != "") __t else null
        }
        case 2 => {
          val __t = primary
          if (__t != false) __t else null
        }
      }
    }
    override def toString: String = com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.komanov.serialization.domain.protos.site.DomainPb
}

object DomainPb extends com.trueaccord.scalapb.GeneratedMessageCompanion[DomainPb] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[DomainPb] = this
  def fromFieldsMap(__fieldsMap: Map[com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.komanov.serialization.domain.protos.site.DomainPb = {
    require(__fieldsMap.keys.forall(_.getContainingType() == descriptor), "FieldDescriptor does not match message type.")
    val __fields = descriptor.getFields
    com.komanov.serialization.domain.protos.site.DomainPb(
      __fieldsMap.getOrElse(__fields.get(0), "").asInstanceOf[String],
      __fieldsMap.getOrElse(__fields.get(1), false).asInstanceOf[Boolean]
    )
  }
  def descriptor: com.google.protobuf.Descriptors.Descriptor = SiteProto.descriptor.getMessageTypes.get(0)
  def messageCompanionForField(__field: com.google.protobuf.Descriptors.FieldDescriptor): com.trueaccord.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__field)
  def enumCompanionForField(__field: com.google.protobuf.Descriptors.FieldDescriptor): com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__field)
  lazy val defaultInstance = com.komanov.serialization.domain.protos.site.DomainPb(
  )
  implicit class DomainPbLens[UpperPB](_l: com.trueaccord.lenses.Lens[UpperPB, DomainPb]) extends com.trueaccord.lenses.ObjectLens[UpperPB, DomainPb](_l) {
    def name: com.trueaccord.lenses.Lens[UpperPB, String] = field(_.name)((c_, f_) => c_.copy(name = f_))
    def primary: com.trueaccord.lenses.Lens[UpperPB, Boolean] = field(_.primary)((c_, f_) => c_.copy(primary = f_))
  }
  final val NAME_FIELD_NUMBER = 1
  final val PRIMARY_FIELD_NUMBER = 2
}