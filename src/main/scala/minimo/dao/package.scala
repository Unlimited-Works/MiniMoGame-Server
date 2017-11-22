package minimo

import io.getquill._

/**
  *
  */
package object dao {
  object EscapeAndSnake extends CompositeNamingStrategy {
    override val elements: List[NamingStrategy] = PostgresEscape :: SnakeCase :: Nil
  }

  lazy val ctx = new PostgresJdbcContext(EscapeAndSnake, MinimoConfig.quill)//new SqlMirrorContext(MirrorSqlDialect, Literal)

//  extends field type
  implicit val encodeObjectId: MappedEncoding[ObjectId, Array[Byte]] = MappedEncoding[ObjectId, Array[Byte]](_.toByteArray)
  implicit val decodeObjectId: MappedEncoding[Array[Byte], ObjectId] = MappedEncoding[Array[Byte], ObjectId](x => new ObjectId(x))

}
