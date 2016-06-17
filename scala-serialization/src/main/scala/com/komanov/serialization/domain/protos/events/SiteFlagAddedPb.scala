// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.komanov.serialization.domain.protos.events



@SerialVersionUID(0L)
final case class SiteFlagAddedPb(
    siteFlag: com.komanov.serialization.domain.protos.site.SiteFlagPb = com.komanov.serialization.domain.protos.site.SiteFlagPb.UnknownSiteFlag
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[SiteFlagAddedPb] with com.trueaccord.lenses.Updatable[SiteFlagAddedPb] {
    @transient
    private[this] var __serializedSizeCachedValue: Int = 0
    private[this] def __computeSerializedValue(): Int = {
      var __size = 0
      if (siteFlag != com.komanov.serialization.domain.protos.site.SiteFlagPb.UnknownSiteFlag) { __size += com.google.protobuf.CodedOutputStream.computeEnumSize(1, siteFlag.value) }
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
        val __v = siteFlag
        if (__v != com.komanov.serialization.domain.protos.site.SiteFlagPb.UnknownSiteFlag) {
          output.writeEnum(1, __v.value)
        }
      };
    }
    def mergeFrom(__input: com.google.protobuf.CodedInputStream): com.komanov.serialization.domain.protos.events.SiteFlagAddedPb = {
      var __siteFlag = this.siteFlag
      var _done__ = false
      while (!_done__) {
        val _tag__ = __input.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 8 =>
            __siteFlag = com.komanov.serialization.domain.protos.site.SiteFlagPb.fromValue(__input.readEnum())
          case tag => __input.skipField(tag)
        }
      }
      com.komanov.serialization.domain.protos.events.SiteFlagAddedPb(
          siteFlag = __siteFlag
      )
    }
    def withSiteFlag(__v: com.komanov.serialization.domain.protos.site.SiteFlagPb): SiteFlagAddedPb = copy(siteFlag = __v)
    def getField(__field: com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = {
      __field.getNumber match {
        case 1 => {
          val __t = siteFlag.valueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
      }
    }
    override def toString: String = com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.komanov.serialization.domain.protos.events.SiteFlagAddedPb
}

object SiteFlagAddedPb extends com.trueaccord.scalapb.GeneratedMessageCompanion[SiteFlagAddedPb] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[SiteFlagAddedPb] = this
  def fromFieldsMap(__fieldsMap: Map[com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.komanov.serialization.domain.protos.events.SiteFlagAddedPb = {
    require(__fieldsMap.keys.forall(_.getContainingType() == descriptor), "FieldDescriptor does not match message type.")
    val __fields = descriptor.getFields
    com.komanov.serialization.domain.protos.events.SiteFlagAddedPb(
      com.komanov.serialization.domain.protos.site.SiteFlagPb.fromValue(__fieldsMap.getOrElse(__fields.get(0), com.komanov.serialization.domain.protos.site.SiteFlagPb.UnknownSiteFlag.valueDescriptor).asInstanceOf[com.google.protobuf.Descriptors.EnumValueDescriptor].getNumber)
    )
  }
  def descriptor: com.google.protobuf.Descriptors.Descriptor = EventsProto.descriptor.getMessageTypes.get(6)
  def messageCompanionForField(__field: com.google.protobuf.Descriptors.FieldDescriptor): com.trueaccord.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__field)
  def enumCompanionForField(__field: com.google.protobuf.Descriptors.FieldDescriptor): com.trueaccord.scalapb.GeneratedEnumCompanion[_] = {
    require(__field.getContainingType() == descriptor, "FieldDescriptor does not match message type.")
    __field.getNumber match {
      case 1 => com.komanov.serialization.domain.protos.site.SiteFlagPb
    }
  }
  lazy val defaultInstance = com.komanov.serialization.domain.protos.events.SiteFlagAddedPb(
  )
  implicit class SiteFlagAddedPbLens[UpperPB](_l: com.trueaccord.lenses.Lens[UpperPB, SiteFlagAddedPb]) extends com.trueaccord.lenses.ObjectLens[UpperPB, SiteFlagAddedPb](_l) {
    def siteFlag: com.trueaccord.lenses.Lens[UpperPB, com.komanov.serialization.domain.protos.site.SiteFlagPb] = field(_.siteFlag)((c_, f_) => c_.copy(siteFlag = f_))
  }
  final val SITEFLAG_FIELD_NUMBER = 1
}
