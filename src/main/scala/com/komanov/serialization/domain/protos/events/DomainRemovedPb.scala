// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package com.komanov.serialization.domain.protos.events



@SerialVersionUID(0L)
final case class DomainRemovedPb(
    name: String = ""
    ) extends com.trueaccord.scalapb.GeneratedMessage with com.trueaccord.scalapb.Message[DomainRemovedPb] with com.trueaccord.lenses.Updatable[DomainRemovedPb] {
    @transient
    lazy val serializedSize: Int = {
      var __size = 0
      if (name != "") { __size += com.google.protobuf.CodedOutputStream.computeStringSize(1, name) }
      __size
    }
    def writeTo(output: com.google.protobuf.CodedOutputStream): Unit = {
      {
        val __v = name
        if (__v != "") {
          output.writeString(1, __v)
        }
      };
    }
    def mergeFrom(__input: com.google.protobuf.CodedInputStream): com.komanov.serialization.domain.protos.events.DomainRemovedPb = {
      var __name = this.name
      var _done__ = false
      while (!_done__) {
        val _tag__ = __input.readTag()
        _tag__ match {
          case 0 => _done__ = true
          case 10 =>
            __name = __input.readString()
          case tag => __input.skipField(tag)
        }
      }
      com.komanov.serialization.domain.protos.events.DomainRemovedPb(
          name = __name
      )
    }
    def withName(__v: String): DomainRemovedPb = copy(name = __v)
    def getField(__field: com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = {
      __field.getNumber match {
        case 1 => {
          val __t = name
          if (__t != "") __t else null
        }
      }
    }
    override def toString: String = com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)
    def companion = com.komanov.serialization.domain.protos.events.DomainRemovedPb
}

object DomainRemovedPb extends com.trueaccord.scalapb.GeneratedMessageCompanion[DomainRemovedPb] {
  implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[DomainRemovedPb] = this
  def fromFieldsMap(__fieldsMap: Map[com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): com.komanov.serialization.domain.protos.events.DomainRemovedPb = {
    require(__fieldsMap.keys.forall(_.getContainingType() == descriptor), "FieldDescriptor does not match message type.")
    val __fields = descriptor.getFields
    com.komanov.serialization.domain.protos.events.DomainRemovedPb(
      __fieldsMap.getOrElse(__fields.get(0), "").asInstanceOf[String]
    )
  }
  def descriptor: com.google.protobuf.Descriptors.Descriptor = SrcMainProtoEventsProto.descriptor.getMessageTypes.get(9)
  def messageCompanionForField(__field: com.google.protobuf.Descriptors.FieldDescriptor): com.trueaccord.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__field)
  def enumCompanionForField(__field: com.google.protobuf.Descriptors.FieldDescriptor): com.trueaccord.scalapb.GeneratedEnumCompanion[_] = throw new MatchError(__field)
  lazy val defaultInstance = com.komanov.serialization.domain.protos.events.DomainRemovedPb(
  )
  implicit class DomainRemovedPbLens[UpperPB](_l: com.trueaccord.lenses.Lens[UpperPB, DomainRemovedPb]) extends com.trueaccord.lenses.ObjectLens[UpperPB, DomainRemovedPb](_l) {
    def name: com.trueaccord.lenses.Lens[UpperPB, String] = field(_.name)((c_, f_) => c_.copy(name = f_))
  }
  final val NAME_FIELD_NUMBER = 1
}
