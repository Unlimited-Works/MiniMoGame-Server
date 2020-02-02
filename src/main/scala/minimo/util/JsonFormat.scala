package minimo.util

import org.json4s.JsonAST.{JString, JValue}
import org.json4s.{CustomSerializer, DefaultFormats, Formats, JObject}

object JsonFormat {
  class ObjectIdJsonSerializer extends CustomSerializer[ObjectId](format => ( {
    case jsonValue: JValue =>
      val JString(oid) = jsonValue
      new ObjectId(oid)
  }, {
    case oid: ObjectId =>
      JString(oid.toString)
  }
  ))

  val objectIdJsonSerializer = new ObjectIdJsonSerializer

  implicit val minimoFormats: Formats = DefaultFormats + JsonFormat.objectIdJsonSerializer

}
