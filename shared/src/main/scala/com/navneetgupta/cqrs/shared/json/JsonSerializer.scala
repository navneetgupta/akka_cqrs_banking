package com.navneetgupta.cqrs.shared.json

import akka.serialization.SerializerWithStringManifest
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{ read, write }
import org.json4s.ext.EnumNameSerializer

class JsonSerializer extends SerializerWithStringManifest {
  implicit val formats = Serialization.formats(NoTypeHints)

  override def toBinary(o: AnyRef): Array[Byte] = {
    val json = write(o)
    json.getBytes()
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
    val m = Manifest.classType[AnyRef](Class.forName(manifest))
    val json = new String(bytes, "utf8")
    read[AnyRef](json)(formats, m)
  }

  override def identifier: Int = 999
  override def manifest(o: AnyRef): String = o.getClass.getName

}
