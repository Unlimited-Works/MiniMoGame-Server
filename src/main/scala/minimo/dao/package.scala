package minimo

import io.getquill._

/**
  *
  */
package object dao {
  lazy val ctx = new PostgresJdbcContext(
    NamingStrategy(SnakeCase, PostgresEscape),
    MinimoConfig.quill
  )

//  extends field type
  implicit val encodeObjectId: MappedEncoding[ObjectId, String] = MappedEncoding[ObjectId, String](_.toString)
  implicit val decodeObjectId: MappedEncoding[String, ObjectId] = MappedEncoding[String, ObjectId](new ObjectId(_))

}
